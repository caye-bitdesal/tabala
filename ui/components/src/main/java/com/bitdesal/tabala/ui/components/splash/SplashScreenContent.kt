package com.bitdesal.tabala.ui.components.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bitdesal.tabala.ui.components.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen animada con dos capas:
 *   1. Figuras (VD)  → aparecen con scale spring + fade-in.
 *   2. Anillo (Canvas) → se dibuja solo girando en sentido horario.
 *
 * Duración total: ~1 500 ms antes de llamar a [onFinished].
 *
 * No requiere dependencias externas (ni Lottie, ni Accompanist).
 * Solo usa el Compose BOM estándar + core-splashscreen.
 */
@Composable
fun SplashScreenContent(onFinished: () -> Unit) {

    // ── Anillo: ángulo de barrido 0° → 316° ─────────────────────────
    val sweepAngle = remember { Animatable(0f) }

    // ── Figuras: opacidad 0 → 1 y escala 0.55 → 1 ───────────────────
    val figuresAlpha = remember { Animatable(0f) }
    val figuresScale = remember { Animatable(0.55f) }

    LaunchedEffect(Unit) {
        // Anillo se dibuja durante los primeros 900 ms
        launch {
            sweepAngle.animateTo(
                targetValue = 316f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // Figuras aparecen 150 ms después con efecto spring + fade
        launch {
            delay(150)
            launch {
                figuresAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = LinearEasing)
                )
            }
            launch {
                figuresScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy, // rebote suave
                        stiffness    = Spring.StiffnessLow
                    )
                )
            }
        }

        // Una vez terminadas las animaciones, notificamos al llamador
        delay(1_500)
        onFinished()
    }

    // ── Layout ───────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),          // cambia al color de fondo de tu app
        contentAlignment = Alignment.Center
    ) {
        // Tamaño de presentación del icono (más grande que los 108 dp del viewport)
        val iconSizeDp = 180.dp

        Box(
            modifier = Modifier.size(iconSizeDp),
            contentAlignment = Alignment.Center
        ) {

            // ── Capa 1: figuras (Vector Drawable) ────────────────────
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(figuresAlpha.value)
                    .scale(figuresScale.value)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_splash_figures),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Capa 2: anillo animado (Canvas) ──────────────────────
            //
            // Geometría del arco (proporciones relativas al viewport 108×108):
            //   Centro  : (51.53 / 108, 51.44 / 108) del tamaño del canvas
            //   Radio   : 28.49 / 108  del ancho del canvas
            //   Grosor  :  5.40 / 108  del ancho del canvas
            //   StartAngle = 292°  (borde superior-derecho del hueco)
            //   SweepAngle = 0 → 316°  (sentido horario hasta borde superior-izquierdo)
            //
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cxRatio = 51.53f / 108f
                val cyRatio = 51.44f / 108f
                val rRatio  = 28.49f / 108f
                val swRatio =  5.40f / 108f

                val cx = size.width  * cxRatio
                val cy = size.height * cyRatio
                val r  = size.width  * rRatio
                val sw = size.width  * swRatio

                drawArc(
                    color      = Color(0xFFBA0E0D),
                    startAngle = 292f,                // upper-right, sentido horario
                    sweepAngle = sweepAngle.value,
                    useCenter  = false,
                    topLeft    = Offset(cx - r, cy - r),
                    size       = Size(r * 2f, r * 2f),
                    style      = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
        }
    }
}