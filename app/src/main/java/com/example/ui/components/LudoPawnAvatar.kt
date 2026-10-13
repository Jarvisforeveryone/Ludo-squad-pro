package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LudoPawnAvatarBg
import com.example.ui.theme.LudoPawnYellow

/**
 * Renders the cute signature Ludo pawn avatar shown in the design screenshot.
 * Features a circular mint background, golden yellow pawn token, and subtle friendly details.
 */
@Composable
fun LudoPawnAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = LudoPawnAvatarBg,
    pawnColor: Color = LudoPawnYellow
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .testTag("ludo_pawn_avatar"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // Base oval of pawn
            val baseOvalWidth = w * 0.8f
            val baseOvalHeight = h * 0.28f
            drawOval(
                color = pawnColor,
                topLeft = Offset((w - baseOvalWidth) / 2f, h - baseOvalHeight),
                size = Size(baseOvalWidth, baseOvalHeight)
            )

            // Body conic path
            val bodyPath = Path().apply {
                moveTo(w * 0.32f, h * 0.42f)
                lineTo(w * 0.68f, h * 0.42f)
                lineTo(w * 0.82f, h * 0.85f)
                lineTo(w * 0.18f, h * 0.85f)
                close()
            }
            drawPath(path = bodyPath, color = pawnColor)

            // Collar ring
            drawOval(
                color = pawnColor.copy(alpha = 0.9f),
                topLeft = Offset(w * 0.30f, h * 0.38f),
                size = Size(w * 0.40f, h * 0.12f)
            )

            // Head circle
            val headRadius = w * 0.22f
            val headCenter = Offset(w * 0.5f, h * 0.26f)
            drawCircle(
                color = pawnColor,
                radius = headRadius,
                center = headCenter
            )

            // Friendly cartoon eyes (black dots)
            val eyeRadius = w * 0.035f
            val eyeY = h * 0.24f
            drawCircle(
                color = Color(0xFF1F2937),
                radius = eyeRadius,
                center = Offset(w * 0.43f, eyeY)
            )
            drawCircle(
                color = Color(0xFF1F2937),
                radius = eyeRadius,
                center = Offset(w * 0.57f, eyeY)
            )

            // Friendly smile
            val smilePath = Path().apply {
                moveTo(w * 0.45f, h * 0.29f)
                quadraticTo(w * 0.5f, h * 0.33f, w * 0.55f, h * 0.29f)
            }
            drawPath(
                path = smilePath,
                color = Color(0xFF1F2937),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.03f)
            )
        }
    }
}
