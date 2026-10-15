package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicDarkBg
import com.example.ui.theme.StonicNeonPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isActive: Boolean = false,
    audioLevel: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

    // Slow clockwise rotation
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 6000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    // Counter-clockwise rotation
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 4000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    // Breathing glow
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 700 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_avatar")
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.minDimension / 2
            val audioBoost = (audioLevel * 0.25f)

            // Outer energy aura
            val auraRadius = radius * 0.95f * (pulseGlow + audioBoost)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StonicCyan.copy(alpha = 0.35f + (audioLevel * 0.3f)),
                        StonicNeonPurple.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = center
            )

            // Outer dashed bracket ring (Rotating Clockwise)
            rotate(rotationOuter, pivot = center) {
                drawCircle(
                    color = StonicCyan.copy(alpha = 0.7f),
                    radius = radius * 0.88f,
                    center = center,
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 18f, 10f, 18f), 0f)
                    )
                )

                // 4 HUD Corner Ticks
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90).toDouble())
                    val tickStart = Offset(
                        center.x + (radius * 0.82f * cos(angle)).toFloat(),
                        center.y + (radius * 0.82f * sin(angle)).toFloat()
                    )
                    val tickEnd = Offset(
                        center.x + (radius * 0.95f * cos(angle)).toFloat(),
                        center.y + (radius * 0.95f * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = StonicCyanBright,
                        start = tickStart,
                        end = tickEnd,
                        strokeWidth = 3f
                    )
                }
            }

            // Inner Segmented Ring (Rotating Counter-Clockwise)
            rotate(rotationInner, pivot = center) {
                drawCircle(
                    color = StonicNeonPurple.copy(alpha = 0.85f),
                    radius = radius * 0.68f,
                    center = center,
                    style = Stroke(
                        width = 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(45f, 25f), 0f)
                    )
                )

                // 8 Arc segments
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val dot = Offset(
                        center.x + (radius * 0.52f * cos(angle)).toFloat(),
                        center.y + (radius * 0.52f * sin(angle)).toFloat()
                    )
                    drawCircle(
                        color = StonicCyanBright,
                        radius = 2.5f,
                        center = dot
                    )
                }
            }

            // Core Housing Ring
            drawCircle(
                color = StonicCyan,
                radius = radius * 0.42f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Inner Reactor Dark Well
            drawCircle(
                color = StonicDarkBg,
                radius = radius * 0.38f,
                center = center
            )

            // Glowing Pulsing Plasma Core
            val coreRadius = radius * 0.32f * (pulseGlow * 0.9f + audioBoost)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        StonicCyanBright,
                        StonicCyan,
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )
        }
    }
}
