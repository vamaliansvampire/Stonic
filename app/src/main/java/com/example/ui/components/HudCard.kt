package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StonicCardBorder
import com.example.ui.theme.StonicCardBorderGlow
import com.example.ui.theme.StonicDarkSurface
import com.example.ui.theme.StonicDarkSurfaceVariant

@Composable
fun HudCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = StonicCardBorder,
    isGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val finalBorderColor = if (isGlow) StonicCardBorderGlow else borderColor
    val shape = RoundedCornerShape(cornerRadius)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            StonicDarkSurface,
            StonicDarkSurfaceVariant.copy(alpha = 0.85f)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush, shape)
            .border(
                border = BorderStroke(
                    width = if (isGlow) 1.5.dp else 1.dp,
                    color = finalBorderColor
                ),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}
