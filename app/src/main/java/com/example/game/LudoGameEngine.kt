package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.model.PlayerColor
import com.example.ui.components.PawnBlue
import com.example.ui.components.PawnGreen
import com.example.ui.components.PawnRed
import com.example.ui.components.PawnYellow

data class BoardCoord(val row: Float, val col: Float)

data class GamePawn(
    val id: Int,
    val color: PlayerColor,
    val step: Int = -1, // -1 = yard, 0..50 = track, 51..55 = home lane, 56 = home
    val yardIndex: Int = ((id % 4) + 4) % 4
) {
    val isHome: Boolean get() = step == 56
    val isInYard: Boolean get() = step == -1
    val isOnBoard: Boolean get() = step in 0..55
}

data class LudoPlayerState(
    val id: String,
    val name: String,
    val color: PlayerColor,
    val pawns: List<GamePawn>,
    val isOnline: Boolean = true,
    val isHost: Boolean = false
) {
    val completedCount: Int get() = pawns.count { it.isHome }
    val hasWon: Boolean get() = completedCount == 4
}

enum class TurnPhase {
    WAITING_FOR_ROLL,
    ROLLING,
    WAITING_FOR_PIECE_SELECTION,
    MOVING_PIECE,
    GAME_OVER
}

data class LudoGameState(
    val players: List<LudoPlayerState>,
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 6,
    val consecutiveSixes: Int = 0,
    val turnPhase: TurnPhase = TurnPhase.WAITING_FOR_ROLL,
    val activePawnMoving: GamePawn? = null,
    val movablePawnIds: Set<Int> = emptySet(),
    val statusMessage: String = "Your Turn • Green",
    val winner: LudoPlayerState? = null
) {
    val currentPlayer: LudoPlayerState get() = players[currentTurnIndex]
}

object LudoTrackGeometry {

    // 52 Perimeter track cells
    val trackCells = listOf(
        BoardCoord(6f, 1f),   // 0: Red Start
        BoardCoord(6f, 2f),   // 1
        BoardCoord(6f, 3f),   // 2
        BoardCoord(6f, 4f),   // 3
        BoardCoord(6f, 5f),   // 4
        BoardCoord(5f, 6f),   // 5
        BoardCoord(4f, 6f),   // 6
        BoardCoord(3f, 6f),   // 7
        BoardCoord(2f, 6f),   // 8
        BoardCoord(1f, 6f),   // 9
        BoardCoord(0f, 6f),   // 10
        BoardCoord(0f, 7f),   // 11
        BoardCoord(0f, 8f),   // 12
        BoardCoord(1f, 8f),   // 13: Green Start
        BoardCoord(2f, 8f),   // 14: Star
        BoardCoord(3f, 8f),   // 15
        BoardCoord(4f, 8f),   // 16
        BoardCoord(5f, 8f),   // 17
        BoardCoord(6f, 9f),   // 18
        BoardCoord(6f, 10f),  // 19
        BoardCoord(6f, 11f),  // 20
        BoardCoord(6f, 12f),  // 21: Star
        BoardCoord(6f, 13f),  // 22
        BoardCoord(6f, 14f),  // 23
        BoardCoord(7f, 14f),  // 24
        BoardCoord(8f, 14f),  // 25
        BoardCoord(8f, 13f),  // 26: Yellow Start
        BoardCoord(8f, 12f),  // 27
        BoardCoord(8f, 11f),  // 28
        BoardCoord(8f, 10f),  // 29
        BoardCoord(8f, 9f),   // 30
        BoardCoord(9f, 8f),   // 31
        BoardCoord(10f, 8f),  // 32
        BoardCoord(11f, 8f),  // 33
        BoardCoord(12f, 8f),  // 34
        BoardCoord(13f, 8f),  // 35
        BoardCoord(14f, 8f),  // 36
        BoardCoord(14f, 7f),  // 37
        BoardCoord(14f, 6f),  // 38
        BoardCoord(13f, 6f),  // 39: Blue Start
        BoardCoord(12f, 6f),  // 40: Star
        BoardCoord(11f, 6f),  // 41
        BoardCoord(10f, 6f),  // 42
        BoardCoord(9f, 6f),   // 43
        BoardCoord(8f, 5f),   // 44
        BoardCoord(8f, 4f),   // 45
        BoardCoord(8f, 3f),   // 46
        BoardCoord(8f, 2f),   // 47: Star
        BoardCoord(8f, 1f),   // 48
        BoardCoord(8f, 0f),   // 49
        BoardCoord(7f, 0f),   // 50
        BoardCoord(6f, 0f)    // 51
    )

    // Safe track indices where captures cannot occur
    val safeTrackIndices = setOf(0, 13, 26, 39, 14, 21, 40, 47)

    fun startIndexFor(color: PlayerColor): Int = when (color) {
        PlayerColor.RED -> 0
        PlayerColor.GREEN -> 13
        PlayerColor.YELLOW -> 26
        PlayerColor.BLUE -> 39
    }

    val homeRunways = mapOf(
        PlayerColor.RED to listOf(
            BoardCoord(7f, 1f), BoardCoord(7f, 2f), BoardCoord(7f, 3f), BoardCoord(7f, 4f), BoardCoord(7f, 5f), BoardCoord(7f, 6f)
        ),
        PlayerColor.GREEN to listOf(
            BoardCoord(1f, 7f), BoardCoord(2f, 7f), BoardCoord(3f, 7f), BoardCoord(4f, 7f), BoardCoord(5f, 7f), BoardCoord(6f, 7f)
        ),
        PlayerColor.YELLOW to listOf(
            BoardCoord(7f, 13f), BoardCoord(7f, 12f), BoardCoord(7f, 11f), BoardCoord(7f, 10f), BoardCoord(7f, 9f), BoardCoord(7f, 8f)
        ),
        PlayerColor.BLUE to listOf(
            BoardCoord(13f, 7f), BoardCoord(12f, 7f), BoardCoord(11f, 7f), BoardCoord(10f, 7f), BoardCoord(9f, 7f), BoardCoord(8f, 7f)
        )
    )

    val yardPockets = mapOf(
        PlayerColor.RED to listOf(
            BoardCoord(1.616f, 1.616f), // Top-Left circle
            BoardCoord(1.616f, 3.337f), // Top-Right circle
            BoardCoord(3.337f, 1.616f), // Bottom-Left circle
            BoardCoord(3.337f, 3.337f)  // Bottom-Right circle
        ),
        PlayerColor.GREEN to listOf(
            BoardCoord(1.616f, 10.646f), // Top-Left circle
            BoardCoord(1.616f, 12.363f), // Top-Right circle
            BoardCoord(3.337f, 10.646f), // Bottom-Left circle
            BoardCoord(3.337f, 12.363f)  // Bottom-Right circle
        ),
        PlayerColor.BLUE to listOf(
            BoardCoord(10.646f, 1.616f), // Top-Left circle
            BoardCoord(10.646f, 3.337f), // Top-Right circle
            BoardCoord(12.365f, 1.616f), // Bottom-Left circle
            BoardCoord(12.365f, 3.337f)  // Bottom-Right circle
        ),
        PlayerColor.YELLOW to listOf(
            BoardCoord(10.646f, 10.646f), // Top-Left circle
            BoardCoord(10.646f, 12.363f), // Top-Right circle
            BoardCoord(12.365f, 10.646f), // Bottom-Left circle
            BoardCoord(12.365f, 12.363f)  // Bottom-Right circle
        )
    )

    fun getCoordinates(pawn: GamePawn): BoardCoord {
        if (pawn.isInYard) {
            val list = yardPockets[pawn.color] ?: yardPockets.values.first()
            val pocketIndex = pawn.yardIndex.coerceIn(0, 3)
            return list[pocketIndex]
        }
        if (pawn.step in 0..50) {
            val startIdx = startIndexFor(pawn.color)
            val trackIdx = (startIdx + pawn.step) % 52
            return trackCells[trackIdx]
        }
        val runway = homeRunways[pawn.color] ?: homeRunways.values.first()
        val index = (pawn.step - 51).coerceIn(0, runway.lastIndex)
        return runway[index]
    }

    fun getTrackIndex(color: PlayerColor, step: Int): Int? {
        if (step in 0..50) {
            return (startIndexFor(color) + step) % 52
        }
        return null
    }

    fun getColor(color: PlayerColor): Color = when (color) {
        PlayerColor.RED -> PawnRed
        PlayerColor.GREEN -> PawnGreen
        PlayerColor.BLUE -> PawnBlue
        PlayerColor.YELLOW -> PawnYellow
    }
}
