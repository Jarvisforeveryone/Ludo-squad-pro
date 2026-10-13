package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LudoQuadrantBlue
import com.example.ui.theme.LudoQuadrantGreen
import com.example.ui.theme.LudoQuadrantRed
import com.example.ui.theme.LudoQuadrantYellow
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerColor(val displayName: String, val hexCode: String) {
    GREEN("Green", "#16A34A"),
    RED("Red", "#EF4444"),
    BLUE("Blue", "#2563EB"),
    YELLOW("Yellow", "#EAB308");

    fun toComposeColor(): Color {
        return when (this) {
            GREEN -> LudoQuadrantGreen
            RED -> LudoQuadrantRed
            BLUE -> LudoQuadrantBlue
            YELLOW -> LudoQuadrantYellow
        }
    }
}
