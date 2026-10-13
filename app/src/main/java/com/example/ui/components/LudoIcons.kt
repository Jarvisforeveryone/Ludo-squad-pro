package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object LudoIcons

/**
 * Thin outlined Lucide-style Crown icon for the Host badge.
 */
@Composable
fun CrownIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = Color(0xFF166534)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.90f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.55f)
            lineTo(w * 0.50f, h * 0.20f)
            lineTo(w * 0.35f, h * 0.55f)
            lineTo(w * 0.10f, h * 0.35f)
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Thin outlined Lucide-style 3D Dice icon for Rules & Board controls.
 */
@Composable
fun DiceIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color(0xFF4B5563)
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = 2.dp.toPx()
        val corner = 4.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        // Outer rounded cube face
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.76f, h * 0.76f),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dice dots (representing 5)
        val dotRadius = w * 0.05f
        drawCircle(color = tint, radius = dotRadius, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(color = tint, radius = dotRadius, center = Offset(w * 0.3f, h * 0.3f))
        drawCircle(color = tint, radius = dotRadius, center = Offset(w * 0.7f, h * 0.3f))
        drawCircle(color = tint, radius = dotRadius, center = Offset(w * 0.3f, h * 0.7f))
        drawCircle(color = tint, radius = dotRadius, center = Offset(w * 0.7f, h * 0.7f))
    }
}
