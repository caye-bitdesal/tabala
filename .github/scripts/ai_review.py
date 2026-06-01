#!/usr/bin/env python3
"""
AI-powered PR code review for the Tabala Android project.

Runs on every PR open/update. Uses Gemini to review changed Kotlin, Gradle, and
XML files, then posts a structured GitHub PR review covering:
  - Bugs
  - Android platform correctness
  - Code understandability
  - Reusability

On subsequent pushes to the same PR the agent re-reads previous review comments
and notes which concerns have been addressed vs. which remain open.
"""

import json
import os
import subprocess
import sys
import textwrap

from google import genai
from google.genai import types
from github import Github, Auth

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

# Update to the latest available model when Google releases new ones.
# See: https://ai.google.dev/gemini-api/docs/models
GEMINI_MODEL = "gemini-2.5-pro"

# Only review these file types (keeps the diff focused and tokens down)
REVIEWED_EXTENSIONS = ("*.kt", "*.kts", "*.xml", "*.toml")

# Hard cap on diff size sent to the model (characters, not tokens)
MAX_DIFF_CHARS = 90_000

# ---------------------------------------------------------------------------
# Prompt
# ---------------------------------------------------------------------------

SYSTEM_PROMPT = textwrap.dedent("""\
    You are a senior Android engineer performing a thorough code review for Tabala,
    an Android application. The project's tech stack is:

    - Language: Kotlin (idiomatic style required)
    - UI: 100% Jetpack Compose with Material 3
    - Architecture: MVVM + Clean Architecture, single Activity
    - Dependency Injection: Koin only (Hilt and Dagger are banned)
    - Navigation: Jetpack Navigation 3 — NavKey, NavBackStack, NavDisplay, entryProvider
    - Reactivity: Kotlin Coroutines, StateFlow / SharedFlow, lifecycle-aware collection
    - Persistence: Room (SQLite)
    - Networking: Retrofit + OkHttp
    - Resources: only strings.xml, themes.xml, and colors.xml may be XML; everything else is Compose

    ─────────────────────────────────────────
    REVIEW CRITERIA — comment only on these:
    ─────────────────────────────────────────
    1. BUGS
       Logic errors, null/crash risks, incorrect API usage, resource or memory leaks,
       threading violations, broken edge cases, improper lifecycle handling.

    2. ANDROID_CORRECTNESS
       Deviations from Android and Jetpack documentation:
       • Wrong Compose side-effect (LaunchedEffect vs SideEffect vs DisposableEffect)
       • Incorrect coroutine scope (viewModelScope vs lifecycleScope vs rememberCoroutineScope)
       • Missing lifecycle-awareness (collectAsStateWithLifecycle vs collectAsState)
       • Improper state hoisting or broken unidirectional data flow
       • Misuse of Navigation 3 APIs (e.g. navigating inside composition instead of callbacks)
       • Koin injection outside composable or outside the Koin context

    3. UNDERSTANDABILITY
       Unclear or misleading names, functions that do more than one thing, missing
       explanation for non-obvious logic, magic literals that should be named constants,
       deeply nested code that should be extracted.

    4. REUSABILITY
       Duplicated logic that already exists (or could be abstracted), new utilities that
       duplicate platform APIs, components that should be extracted to ui/components/,
       missed opportunity to generalise an existing function.

    ─────────────────────────────────────────
    PREVIOUS REVIEW COMMENTS
    ─────────────────────────────────────────
    If previous review comments are included in the prompt, determine for each one
    whether the latest commits appear to have addressed the concern. List clearly
    resolved concerns in `addressed_comments`.

    ─────────────────────────────────────────
    OUTPUT FORMAT — strict JSON, no extra text
    ─────────────────────────────────────────
    Return ONLY a JSON object matching the schema below. All arrays may be empty [].

    {
      "summary": "2–3 sentence overall assessment of the PR",
      "verdict": "approve" | "request_changes" | "comment",
      "issues": [
        {
          "file": "relative/path/to/File.kt",
          "line": 42,
          "severity": "error" | "warning" | "suggestion",
          "category": "bug" | "android_correctness" | "understandability" | "reusability",
          "title": "Short imperative title",
          "body": "Detailed explanation and a concrete suggestion or code snippet"
        }
      ],
      "open_questions": [
        "A specific question for the PR author that needs an answer before merging?"
      ],
      "addressed_comments": [
        "Brief description of a previous review concern that the new commits resolve"
      ],
      "praise": [
        "One specific thing done well in this PR"
      ]
    }

    Use verdict="request_changes" when there are error-severity issues.
    Use verdict="comment" when there are only warnings, suggestions, or questions.
    Use verdict="approve" only when the code is clean with no issues.
""")

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

SEVERITY_EMOJI = {"error": "🔴", "warning": "🟡", "suggestion": "🔵"}
CATEGORY_LABEL = {
    "bug": "Bug",
    "android_correctness": "Android Correctness",
    "understandability": "Understandability",
    "reusability": "Reusability",
}

# Gemini finish_reason codes (finish_reason=2 → MAX_TOKENS)
FINISH_REASON_LABELS = {1: "STOP", 2: "MAX_TOKENS", 3: "SAFETY", 4: "RECITATION", 5: "OTHER"}


def run_git(cmd: str) -> str:
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return result.stdout


def get_diff(base_sha: str, head_sha: str) -> str:
    extensions = " ".join(f"'{e}'" for e in REVIEWED_EXTENSIONS)
    diff = run_git(f"git diff {base_sha}...{head_sha} -- {extensions}")
    if len(diff) > MAX_DIFF_CHARS:
        diff = diff[:MAX_DIFF_CHARS] + "\n\n[diff truncated — exceeds size limit]"
    return diff


def get_previous_review_comments(pr) -> list[dict]:
    """Collect inline review comments from previous review rounds (human only)."""
    comments = []
    for comment in pr.get_review_comments():
        if "github-actions" in (comment.user.login or ""):
            continue
        comments.append({
            "file": comment.path,
            "line": comment.original_line,
            "body": comment.body,
            "author": comment.user.login,
        })
    return comments


def get_previous_issue_comments(pr) -> list[dict]:
    """Collect general (non-review) PR comments from humans."""
    comments = []
    for comment in pr.get_issue_comments():
        if "github-actions" in (comment.user.login or ""):
            continue
        comments.append({"author": comment.user.login, "body": comment.body})
    return comments


def build_user_prompt(
    diff: str,
    pr_title: str,
    pr_body: str,
    review_comments: list[dict],
    issue_comments: list[dict],
) -> str:
    parts = [
        f"## PR Title\n{pr_title}\n",
        f"## PR Description\n{pr_body or '*(no description provided)*'}\n",
        f"## Diff\n```diff\n{diff}\n```\n",
    ]

    if review_comments:
        parts.append("## Previous Inline Review Comments\n")
        for c in review_comments:
            parts.append(
                f"- **{c['file']}:{c['line']}** — {c['author']}: {c['body']}\n"
            )

    if issue_comments:
        parts.append("## Previous PR Discussion Comments\n")
        for c in issue_comments:
            parts.append(f"- **{c['author']}**: {c['body']}\n")

    return "\n".join(parts)


def call_gemini(user_prompt: str) -> dict:
    client = genai.Client(api_key=os.environ["GEMINI_API_KEY"])

    response = client.models.generate_content(
        model=GEMINI_MODEL,
        contents=user_prompt,
        config=types.GenerateContentConfig(
            system_instruction=SYSTEM_PROMPT,
            temperature=0.2,
            max_output_tokens=8192,
            response_mime_type="application/json",
        ),
    )

    # Check for non-STOP finish reasons before accessing text
    candidate = response.candidates[0] if response.candidates else None
    if candidate is None:
        raise RuntimeError("Gemini returned no candidates.")

    finish_reason = candidate.finish_reason
    # finish_reason 1 = STOP (normal), anything else needs attention
    if finish_reason not in (1, None):
        label = FINISH_REASON_LABELS.get(finish_reason, str(finish_reason))
        raise RuntimeError(
            f"Gemini stopped with finish_reason={finish_reason} ({label}). "
            "If this is MAX_TOKENS, reduce MAX_DIFF_CHARS or increase max_output_tokens."
        )

    raw = response.text.strip()
    # Strip accidental markdown code fences (shouldn't happen with response_mime_type=json)
    if raw.startswith("```"):
        raw = raw.split("\n", 1)[1]
        raw = raw.rsplit("```", 1)[0]
    return json.loads(raw)


def format_review_body(review: dict) -> str:
    """Convert the structured review dict into a GitHub-flavoured Markdown body."""
    lines = ["## 🤖 AI Code Review\n", review["summary"], ""]

    issues = review.get("issues", [])
    if issues:
        lines.append("---\n### Issues\n")
        for issue in issues:
            emoji = SEVERITY_EMOJI.get(issue["severity"], "⚪")
            cat = CATEGORY_LABEL.get(issue["category"], issue["category"])
            location = f"`{issue['file']}`"
            if issue.get("line"):
                location += f" line {issue['line']}"
            lines.append(f"#### {emoji} {issue['title']}")
            lines.append(f"**Category:** {cat} &nbsp;|&nbsp; **Location:** {location}\n")
            lines.append(f"{issue['body']}\n")

    questions = review.get("open_questions", [])
    if questions:
        lines.append("---\n### ❓ Open Questions\n")
        for q in questions:
            lines.append(f"- {q}")
        lines.append("")

    addressed = review.get("addressed_comments", [])
    if addressed:
        lines.append("---\n### ✅ Addressed from Previous Review\n")
        for item in addressed:
            lines.append(f"- {item}")
        lines.append("")

    praise = review.get("praise", [])
    if praise:
        lines.append("---\n### 👍 Well Done\n")
        for item in praise:
            lines.append(f"- {item}")
        lines.append("")

    lines.append(
        "---\n*Generated automatically by the AI Code Review workflow using "
        f"**{GEMINI_MODEL}**. A human reviewer must still approve before merging.*"
    )
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------


def main() -> None:
    token = os.environ["GITHUB_TOKEN"]
    repo_name = os.environ["REPO_NAME"]
    pr_number = int(os.environ["PR_NUMBER"])
    base_sha = os.environ["BASE_SHA"]
    head_sha = os.environ["HEAD_SHA"]

    print(f"[review] PR #{pr_number} — {os.environ.get('PR_TITLE', '')}")

    # ── 1. Get the diff ──────────────────────────────────────────────────────
    diff = get_diff(base_sha, head_sha)
    if not diff.strip():
        print("[review] No relevant files changed — skipping.")
        sys.exit(0)
    print(f"[review] Diff size: {len(diff):,} chars")

    # ── 2. Fetch PR context from GitHub ─────────────────────────────────────
    g = Github(auth=Auth.Token(token))
    repo = g.get_repo(repo_name)
    pr = repo.get_pull(pr_number)

    review_comments = get_previous_review_comments(pr)
    issue_comments = get_previous_issue_comments(pr)
    print(
        f"[review] Context: {len(review_comments)} review comment(s), "
        f"{len(issue_comments)} issue comment(s)"
    )

    # ── 3. Call Gemini ───────────────────────────────────────────────────────
    prompt = build_user_prompt(
        diff, pr.title, pr.body or "", review_comments, issue_comments
    )
    print(f"[review] Sending prompt ({len(prompt):,} chars) to {GEMINI_MODEL}…")
    review = call_gemini(prompt)
    print(
        f"[review] Received verdict='{review.get('verdict')}', "
        f"{len(review.get('issues', []))} issue(s), "
        f"{len(review.get('open_questions', []))} question(s)"
    )

    # ── 4. Post the GitHub PR review ─────────────────────────────────────────
    body = format_review_body(review)
    raw_verdict = review.get("verdict", "comment").upper()
    if raw_verdict not in ("APPROVE", "REQUEST_CHANGES", "COMMENT"):
        raw_verdict = "COMMENT"

    pr.create_review(body=body, event=raw_verdict)
    print(f"[review] Review posted (event={raw_verdict})")

    # ── 5. Post open questions as a separate comment so they are highly visible
    questions = review.get("open_questions", [])
    if questions:
        q_body = "### ❓ Questions for the author\n\n"
        q_body += "\n".join(f"- [ ] {q}" for q in questions)
        q_body += (
            "\n\n*Please address these questions in the PR description or "
            "as reply comments before requesting re-review.*"
        )
        pr.create_issue_comment(q_body)
        print(f"[review] Posted {len(questions)} open question(s) as issue comment")


if __name__ == "__main__":
    main()
