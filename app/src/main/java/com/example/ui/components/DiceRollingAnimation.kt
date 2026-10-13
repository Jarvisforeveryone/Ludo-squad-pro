package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DiceRollingAnimation using Lottie with rememberLottieComposition.
 *
 * When triggered, plays the Lottie rolling physics animation, cycles through
 * randomized faces, displays the final result, and invokes onRollFinished.
 */
@Composable
fun DiceRollingAnimation(
    isRolling: Boolean,
    currentValue: Int,
    onRollFinished: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    onClick: (() -> Unit)? = null
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.dice_roll))
    var displayValue by remember(currentValue) { mutableIntStateOf(currentValue) }
    var isAnimating by remember { mutableStateOf(false) }

    // Physical bounce & rotation scale for 3D tumbling feel
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    val lottieAnim = animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isRolling || isAnimating,
        iterations = 1,
        speed = 1.5f
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            isAnimating = true

            // Launch parallel rotation & scale bounce
            launch {
                scaleAnim.animateTo(1.25f, tween(150, easing = FastOutSlowInEasing))
                scaleAnim.animateTo(0.85f, tween(200, easing = FastOutSlowInEasing))
                scaleAnim.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            }
            launch {
                rotationAnim.animateTo(720f, tween(700, easing = FastOutSlowInEasing))
                rotationAnim.snapTo(0f)
            }

            // Rapidly cycle randomized faces during the roll
            val rollDurationMs = 700L
            val stepIntervalMs = 60L
            val steps = (rollDurationMs / stepIntervalMs).toInt()

            for (i in 0 until steps) {
                displayValue = (1..6).random()
                delay(stepIntervalMs)
            }

            // Final randomized result
            val finalResult = (1..6).random()
            displayValue = finalResult
            isAnimating = false
            onRollFinished(finalResult)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim.value)
            .rotate(rotationAnim.value)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF3F4F6),
                        Color(0xFFE5E7EB)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            )
            .border(1.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(14.dp))
            .then(if (onClick != null && !isRolling && !isAnimating) Modifier.clickable { onClick() } else Modifier)
            .testTag("dice_rolling_animation"),
        contentAlignment = Alignment.Center
    ) {
        if (isAnimating && composition != null) {
            // Lottie Composition Active Tumbling Layer
            LottieAnimation(
                composition = composition,
                progress = { lottieAnim.progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Crisp 3D Dice Face Rendering
            DiceFaceCanvas(value = displayValue)
        }
    }
}

@Composable
private fun DiceFaceCanvas(value: Int) {
    Canvas(modifier = Modifier.size(46.dp)) {
        val w = size.width
        val h = size.height
        val r = w * 0.10f
        val dotColor = if (value == 1) Color(0xFFEF4444) else Color(0xFF1F2937)

        fun dot(cx: Float, cy: Float) {
            // 3D Pip Drop Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                radius = r * 1.05f,
                center = Offset(cx, cy + 1.5f)
            )
            // Main Pip
            drawCircle(
                color = dotColor,
                radius = r,
                center = Offset(cx, cy)
            )
        }

        when (value) {
            1 -> {
                dot(w * 0.5f, h * 0.5f)
            }
            2 -> {
                dot(w * 0.28f, h * 0.28f)
                dot(w * 0.72f, h * 0.72f)
            }
            3 -> {
                dot(w * 0.25f, h * 0.25f)
                dot(w * 0.50f, h * 0.50f)
                dot(w * 0.75f, h * 0.75f)
            }
            4 -> {
                dot(w * 0.28f, h * 0.28f)
                dot(w * 0.72f, h * 0.28f)
                dot(w * 0.28f, h * 0.72f)
                dot(w * 0.72f, h * 0.72f)
            }
            5 -> {
                dot(w * 0.26f, h * 0.26f)
                dot(w * 0.74f, h * 0.26f)
                dot(w * 0.50f, h * 0.50f)
                dot(w * 0.26f, h * 0.74f)
                dot(w * 0.74f, h * 0.74f)
            }
            else -> { // 6
                dot(w * 0.28f, h * 0.22f)
                dot(w * 0.72f, h * 0.22f)
                dot(w * 0.28f, h * 0.50f)
                dot(w * 0.72f, h * 0.50f)
                dot(w * 0.28f, h * 0.78f)
                dot(w * 0.72f, h * 0.78f)
            }
        }
    }
}
