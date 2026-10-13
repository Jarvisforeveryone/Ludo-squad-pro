package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.GamePawn
import com.example.game.LudoTrackGeometry

// Exact color tokens for players & pawns
val PawnRed = Color(0xFFEF4444)
val PawnGreen = Color(0xFF22C55E)
val PawnBlue = Color(0xFF3B82F6)
val PawnYellow = Color(0xFFEAB308)

/**
 * Ludo Board Composable rendering the uploaded background illustration image
 * with pure Compose flat Carrom-style circular pawns placed accurately on the 15x15 overlay.
 */
@Composable
fun LudoBoard(
    pawns: List<GamePawn>,
    movablePawnIds: Set<Int> = emptySet(),
    onPawnClick: (GamePawn) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .testTag("ludo_board_container"),
        contentAlignment = Alignment.Center
    ) {
        // Main Board Background Image (1:1 aspect ratio)
        Image(
            painter = painterResource(id = R.drawable.ludo_board_bg),
            contentDescription = "Ludo Board Game Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // 15x15 Overlay Grid with Perfectly Centered Pawns
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boardWidth = maxWidth
            val cellSize = boardWidth / 15f

            pawns.forEach { pawn ->
                key(pawn.id) {
                    val targetCoord = LudoTrackGeometry.getCoordinates(pawn)
                    val animatedRow by animateFloatAsState(
                        targetValue = targetCoord.row,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                        label = "pawn_row_${pawn.id}_${pawn.color}"
                    )
                    val animatedCol by animateFloatAsState(
                        targetValue = targetCoord.col,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                        label = "pawn_col_${pawn.id}_${pawn.color}"
                    )

                    val isMovable = movablePawnIds.contains(pawn.id)
                    val xOffset = cellSize * animatedCol
                    val yOffset = cellSize * animatedRow

                    // Cell Container: Centers the pawn in its coordinate
                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .size(cellSize),
                        contentAlignment = Alignment.Center
                    ) {
                        CarromPawnItem(
                            pawn = pawn,
                            isMovable = isMovable,
                            onClick = { onPawnClick(pawn) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive wrapper handling size (85% of grid cell) and selectable pulse state.
 */
@Composable
private fun CarromPawnItem(
    pawn: GamePawn,
    isMovable: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isMovable) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize(0.85f)
            .scale(if (isMovable) pulseScale else 1f)
            .then(
                if (isMovable) {
                    Modifier
                        .border(2.5.dp, Color(0xFFFDE047), CircleShape)
                        .clickable { onClick() }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        CarromPawn(
            color = LudoTrackGeometry.getColor(pawn.color),
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Pure Jetpack Compose Carrom-style Pawn:
 * Flat, solid circular shape with a slightly darker 2.dp border and soft 3.dp drop shadow.
 */
@Composable
fun CarromPawn(
    color: Color,
    modifier: Modifier = Modifier
) {
    // 2.dp border of a slightly darker shade of the player's color
    val borderColor = when (color) {
        PawnRed -> Color(0xFFB91C1C)
        PawnGreen -> Color(0xFF15803D)
        PawnBlue -> Color(0xFF1D4ED8)
        PawnYellow -> Color(0xFFCA8A04)
        else -> color.copy(alpha = 0.85f)
    }

    // Concentric engraved groove for a classic, clean carrom coin look
    val innerRingColor = when (color) {
        PawnRed -> Color(0xFF991B1B)
        PawnGreen -> Color(0xFF166534)
        PawnBlue -> Color(0xFF1E40AF)
        PawnYellow -> Color(0xFFA16207)
        else -> Color.Black.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Inner engraved circular ring of the carrom piece
        Box(
            modifier = Modifier
                .fillMaxSize(0.55f)
                .border(1.dp, innerRingColor.copy(alpha = 0.45f), CircleShape)
        )
    }
}

/**
 * Backward-compatible alias for callers expecting Pawn3D.
 */
@Composable
fun Pawn3D(
    color: Color,
    modifier: Modifier = Modifier
) {
    CarromPawn(color = color, modifier = modifier)
}
