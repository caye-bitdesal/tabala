# BitDesal Evaluate — Reference

## AI review issue shape (Tabala / `ai_review.py`)

CI reviews emit JSON like:

```json
{
  "issues": [
    {
      "file": "app/shared/src/commonMain/kotlin/.../Feature.kt",
      "line": 42,
      "severity": "error",
      "category": "bug",
      "title": "Short imperative title",
      "body": "Explanation and suggested fix"
    }
  ]
}
```

`severity`: `error` | `warning` | `suggestion`  
`category`: `bug` | `platform_correctness` | `understandability` | `reusability`

(Legacy bot runs may still say `android_correctness` — treat as `platform_correctness`.)

Markdown PR comments often use emoji prefixes (🔴 🟡 🔵) — map them to the same
fields when parsing.

## Loading issues from GitHub

When the user gives a PR number (repo must be current):

```bash
gh pr view <number> --comments
gh api repos/{owner}/{repo}/pulls/<number>/comments
gh api repos/{owner}/{repo}/pulls/<number>/reviews
```

Prefer **inline review comments** and the latest **AI Code Review** bot comment.
Ignore dismissed or superseded threads when a newer bot run exists.

## Triage rubric

### Apply when

- The claim matches the code at the cited location (or obvious nearby code if
  line drifted).
- The fix is consistent with **AGENTS.md** (KMP MVVM, Koin, Compose MP, Ktor,
  server auth/RGPD rules, no banned frameworks).
- The fix is **minimal** and scoped to the PR/feature under review.
- For `platform_correctness`, the bot cites a real lifecycle, Koin, Ktor, or
  source-set mistake.

### Reject when

- **False positive** — e.g. web-only `collectAsState` flagged like Android lifecycle.
- **Stale** — issue refers to code removed or rewritten on the branch.
- **Violates project choices** — Hilt, Dagger, Metro, Room, Retrofit on new code.
- **Over-engineering** — extract/helper requested with no duplication or spec.
- **Nit without rule** — style the repo does not enforce.
- **Spec conflict** — fix would break `specs/<feature>/` unless user confirms.

### Ask user when

- Product or UX ambiguity.
- Two valid designs (bot prefers one not chosen in spec).
- Severity `suggestion` that **changes behavior** beyond the PR scope.
- Security/privacy/RGPD tradeoff.

### Already fixed when

- Diff or file clearly contains the suggested fix.
- A prior commit on the branch addresses the same root cause — cite it.

## Fix quality bar (same as normal agent work)

| Change type | Tests |
|-------------|--------|
| ViewModel / repository / `:core` logic | `:app:shared:jvmTest` or `commonTest` |
| Server routes / SQL / auth | `:server:test` |
| Android-only wiring | `:app:androidApp:testDebugUnitTest` when configured |
| Compose behavior (non-trivial) | JVM/Android UI tests when the project has them |

Do not add tests that only assert obvious wiring.

## Common bot themes in this repo

| Theme | Often valid? | Notes |
|-------|----------------|-------|
| `collectAsStateWithLifecycle` on Android production UI | Often yes | Web/desktop may use `collectAsState` |
| Navigation in `LaunchedEffect` without user intent | Often yes | Prefer callbacks / ViewModel |
| Missing Koin test module / fake repository | Sometimes | Only if PR added untested logic |
| Extract shared logic to `:core` or `commonMain` | Sometimes | AGENTS.md: second caller or clear reuse |
| Manual `StateFlow` equality guard before `update` | Usually no | `update` already dedupes |
| Missing server role check on new route | Often yes | Match Firebase role model |

## Minimal fix examples

**Apply — lifecycle collection (Android)**

Bot: use `collectAsStateWithLifecycle` in a screen composable.  
Action: import lifecycle compose artifact, swap collector on Android source set.

**Reject — DI framework**

Bot: inject repository with Hilt or Metro.  
Action: reject; document Koin modules in summary.

**Ask — behavior change**

Bot: also expose internal contract amounts on public API.  
Action: ask; RGPD / product scope.

## Commits (one per resolved issue)

When verdict is **apply**, commit immediately after that issue’s fix (before
starting the next issue).

### Message format

Use the repo prefix style (`fix:`, `care:`, `test:`, etc.). Prefer **`fix:`**
for bugs and **platform_correctness**; **`care:`** for clarity/reuse-only changes.

Subject line (≤ ~72 chars):

```text
fix: [AI review #N] Imperative summary matching the bot title
```

Body (HEREDOC for `git commit`):

```text
Resolves AI PR review issue #N: <title>

Category: <bug|platform_correctness|understandability|reusability>
Severity: <error|warning|suggestion>
Location: <file>:<line> (or "see diff")

<One or two sentences: what was wrong and what we changed.>
```

### Git steps (per issue)

1. `git status` / `git diff` — stage only files for this issue.
2. `git commit` with the message above.
3. Note the short SHA for the Step 6 summary.
4. Never amend a failed hook run; fix and commit again.

### Local last-commit review

Each evaluate commit uses `[AI review #N]` in the subject. If the project
installed **bitdesal-review-last-commit**, the post-commit hook **always**
runs a local Fable review of that commit in chat (even when other commits would
use `[skip review]` or `WIP`).
