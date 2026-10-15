package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicNeonPurple
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 24,
    isActive: Boolean = false,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("voice_waveform")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val totalWidth = size.width
            val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(3f)
            val spacing = (totalWidth - (barWidth * barCount)) / (barCount + 1)
            val midY = size.height / 2f
            val maxBarHeight = size.height * 0.9f

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    StonicCyanBright,
                    StonicCyan,
                    StonicNeonPurple
                )
            )

            for (i in 0 until barCount) {
                val normalizedIndex = i.toFloat() / barCount
                // Bell curve envelope so center bars are higher
                val envelope = sin(normalizedIndex * Math.PI).toFloat()

                val wave = if (isActive || audioLevel > 0.05f) {
                    val sine = sin(phase + (i * 0.4f)).toFloat()
                    val dynamicAmp = 0.25f + (audioLevel * 0.75f)
                    (0.3f + (0.7f * dynamicAmp * (0.5f + 0.5f * sine))) * envelope
                } else {
                    0.12f * envelope
                }

                val currentBarHeight = (maxBarHeight * wave).coerceAtLeast(4f)
                val x = spacing + (i * (barWidth + spacing))
                val y = midY - (currentBarHeight / 2f)

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, currentBarHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
