package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.game.GamePawn
import com.example.game.LudoPlayerState
import com.example.game.LudoTrackGeometry
import com.example.game.TurnPhase
import com.example.model.PlayerColor
import com.example.ui.components.DiceIcon
import com.example.ui.components.DiceRollingAnimation
import com.example.ui.components.LudoBoard
import com.example.ui.components.Pawn3D
import com.example.ui.components.PawnBlue
import com.example.ui.components.PawnGreen
import com.example.ui.components.PawnRed
import com.example.ui.components.PawnYellow
import com.example.model.ChatMessage
import com.example.ui.components.ChatBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ColorDarkGreen = Color(0xFF166534)
private val ColorGrey = Color(0xFF6B7280)
private val ColorDarkGrey = Color(0xFF374151)
private val ColorLimeGreen = Color(0xFF84CC16)
private val ColorTimerBg = Color(0xFFDCFCE7)
private val ColorWaitingBg = Color(0xFFF3F4F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoardScreen(
    roomCode: String,
    currentUserId: String = "",
    activeRoom: com.example.model.LudoRoom? = null,
    chatMessages: List<ChatMessage> = emptyList(),
    onRollDice: (Int) -> Unit = {},
    onPassTurn: (String) -> Unit = {},
    onUpdatePawnPositions: (String, List<Int>) -> Unit = { _, _ -> },
    onSendChatMessage: (String) -> Unit = {},
    onStartChatObservation: () -> Unit = {},
    onLeaveGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onLeaveGame() }

    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var showChatSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(roomCode) {
        onStartChatObservation()
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    // Build real-time player list from activeRoom (or default 4 colors if null)
    val roomPlayers = activeRoom?.players?.values?.toList() ?: emptyList()
    val players = remember(roomPlayers, currentUserId) {
        if (roomPlayers.isEmpty()) {
            listOf(
                LudoPlayerState(
                    id = if (currentUserId.isNotBlank()) currentUserId else "local_user",
                    name = "You",
                    color = PlayerColor.GREEN,
                    pawns = (0..3).map { GamePawn(it, PlayerColor.GREEN, step = -1, yardIndex = it) }
                )
            )
        } else {
            roomPlayers.map { p ->
                val offset = when (p.color) {
                    PlayerColor.GREEN -> 0
                    PlayerColor.RED -> 4
                    PlayerColor.BLUE -> 8
                    PlayerColor.YELLOW -> 12
                }
                LudoPlayerState(
                    id = p.uid,
                    name = if (p.uid == currentUserId) "${p.name} (You)" else p.name,
                    color = p.color,
                    pawns = p.pieces.mapIndexed { idx, step ->
                        GamePawn(id = offset + idx, color = p.color, step = step, yardIndex = idx)
                    },
                    isOnline = p.isOnline,
                    isHost = p.isHost
                )
            }
        }
    }

    val currentTurnUid = activeRoom?.currentTurn ?: ""
    val currentTurnIndex = remember(currentTurnUid, players) {
        val idx = players.indexOfFirst { it.id == currentTurnUid }
        if (idx >= 0) idx else 0
    }

    val currentPlayer = players.getOrElse(currentTurnIndex) { players.first() }
    val isUserTurn = (currentUserId.isNotBlank() && currentPlayer.id == currentUserId) || (currentUserId.isBlank() && currentTurnIndex == 0)

    var diceValue by remember { mutableIntStateOf(activeRoom?.diceValue ?: 6) }
    var isRolling by remember { mutableStateOf(false) }
    var turnPhase by remember { mutableStateOf(TurnPhase.WAITING_FOR_ROLL) }
    var statusText by remember { mutableStateOf(if (isUserTurn) "Your Turn: Roll the dice" else "${currentPlayer.name}'s Turn") }
    var movablePawnIds by remember { mutableStateOf(emptySet<Int>()) }
    var winnerPlayer by remember { mutableStateOf<LudoPlayerState?>(null) }
    var secondsRemaining by remember { mutableIntStateOf(40) }

    // Sync Firebase diceValue changes locally
    val firebaseDiceVal = activeRoom?.diceValue
    LaunchedEffect(firebaseDiceVal) {
        if (firebaseDiceVal != null && firebaseDiceVal != diceValue) {
            isRolling = true
            soundManager.playDiceRoll()
            delay(1200L)
            diceValue = firebaseDiceVal
            isRolling = false

            val candidatePawns = mutableSetOf<Int>()
            currentPlayer.pawns.forEach { pawn ->
                if (pawn.isInYard && firebaseDiceVal == 6) {
                    candidatePawns.add(pawn.id)
                } else if (pawn.isOnBoard) {
                    if (pawn.step + firebaseDiceVal <= 56) {
                        candidatePawns.add(pawn.id)
                    }
                }
            }
            movablePawnIds = candidatePawns

            if (candidatePawns.isEmpty()) {
                statusText = "No moves possible! Passing turn..."
                coroutineScope.launch {
                    delay(1000L)
                    val nextIdx = (currentTurnIndex + 1) % players.size
                    val nextUid = players[nextIdx].id
                    onPassTurn(nextUid)
                }
            } else {
                turnPhase = TurnPhase.WAITING_FOR_PIECE_SELECTION
                statusText = if (isUserTurn) "Rolled $firebaseDiceVal! Tap glowing pawn to move" else "${currentPlayer.name} is choosing a move..."
            }
        }
    }

    // Timer countdown
    LaunchedEffect(currentTurnIndex, turnPhase) {
        secondsRemaining = 40
        while (secondsRemaining > 0 && winnerPlayer == null) {
            delay(1000L)
            secondsRemaining--
        }
    }

    // All active pawns on board
    val allPawns = players.flatMap { it.pawns }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---------------------------------------------------------
        // 1. TOP BAR (Height 56.dp, Room Code, Timer)
        // ---------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .testTag("top_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onLeaveGame,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = ColorDarkGrey,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("room_title_row")
            ) {
                Text(
                    text = "LUDO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorDarkGreen
                )
                Text(
                    text = " • ROOM: $roomCode",
                    fontSize = 14.sp,
                    color = ColorGrey
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ColorTimerBg,
                    modifier = Modifier.testTag("timer_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Clock",
                            tint = ColorDarkGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "00:${secondsRemaining.toString().padStart(2, '0')}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ColorDarkGreen
                        )
                    }
                }
                Text(
                    text = "TIMER",
                    fontSize = 10.sp,
                    color = ColorDarkGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // ---------------------------------------------------------
        // 2. STATUS BANNER
        // ---------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("status_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("status_title_row")
                ) {
                    Text(
                        text = if (isUserTurn) "Your Turn • ${currentPlayer.color.name.lowercase().replaceFirstChar { it.uppercase() }}" else "${currentPlayer.name}'s Turn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ColorDarkGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Pawn3D(
                        color = LudoTrackGeometry.getColor(currentPlayer.color),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = ColorGrey
                )
            }
        }

        // ---------------------------------------------------------
        // 3. LUDO BOARD with interactive pawns & sound effects
        // ---------------------------------------------------------
        LudoBoard(
            pawns = allPawns,
            movablePawnIds = if (isUserTurn) movablePawnIds else emptySet(),
            onPawnClick = { clickedPawn ->
                if (isUserTurn && movablePawnIds.contains(clickedPawn.id)) {
                    movablePawnIds = emptySet()
                    executeMove(clickedPawn, diceValue, players, currentTurnIndex, soundManager) { updatedPlayers, bonusTurn, winner ->
                        val activeP = updatedPlayers.firstOrNull { it.id == currentUserId } ?: updatedPlayers[currentTurnIndex]
                        val myPieces = activeP.pawns.map { it.step }
                        onUpdatePawnPositions(currentUserId, myPieces)

                        // Sync captured opponent pieces if any
                        updatedPlayers.filter { it.id != currentUserId }.forEach { opp ->
                            val oppPieces = opp.pawns.map { it.step }
                            onUpdatePawnPositions(opp.id, oppPieces)
                        }

                        if (winner != null) {
                            winnerPlayer = winner
                            soundManager.playGameWin()
                        } else if (bonusTurn) {
                            turnPhase = TurnPhase.WAITING_FOR_ROLL
                            statusText = "Rolled a 6 / Captured! Roll again!"
                        } else {
                            val nextIdx = (currentTurnIndex + 1) % players.size
                            val nextUid = players[nextIdx].id
                            onPassTurn(nextUid)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ---------------------------------------------------------
        // 4. PLAYERS CARD with live turn statuses & completed pawn scores
        // ---------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("players_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Players",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorDarkGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    players.forEachIndexed { index, player ->
                        val isTurn = currentTurnIndex == index
                        PlayerStatusItem(
                            dotColor = LudoTrackGeometry.getColor(player.color),
                            name = player.name,
                            statusText = if (isTurn) "Turn" else "${player.completedCount}/4 Home",
                            statusBg = if (isTurn) ColorTimerBg else ColorWaitingBg,
                            statusTextColor = if (isTurn) ColorDarkGreen else ColorGrey
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // 5. BOTTOM CONTROL PANEL (Sound-Integrated Dice & Actions)
        // ---------------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .testTag("bottom_control_panel"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: Lottie Dice Rolling Animation with sound
                    DiceRollingAnimation(
                        isRolling = isRolling,
                        currentValue = diceValue,
                        onRollFinished = { result ->
                            diceValue = result
                            isRolling = false
                        },
                        modifier = Modifier.size(64.dp),
                        onClick = {
                            if (isUserTurn && turnPhase == TurnPhase.WAITING_FOR_ROLL && !isRolling) {
                                val rolled = (1..6).random()
                                onRollDice(rolled)
                                statusText = "Rolling dice..."
                            }
                        }
                    )

                    // Center: Single-line 56.dp "ROLL DICE" Button
                    Button(
                        onClick = {
                            if (isUserTurn && turnPhase == TurnPhase.WAITING_FOR_ROLL && !isRolling) {
                                val rolled = (1..6).random()
                                onRollDice(rolled)
                                statusText = "Rolling dice..."
                            }
                        },
                        enabled = isUserTurn && turnPhase == TurnPhase.WAITING_FOR_ROLL && !isRolling,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("roll_dice_button"),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorLimeGreen,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            DiceIcon(
                                size = 22.dp,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROLL DICE",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Right: 56.dp Chat OutlinedButton
                    OutlinedButton(
                        onClick = { showChatSheet = true },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("chat_button"),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, ColorGrey),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorDarkGreen
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat",
                                tint = ColorDarkGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chat",
                                fontWeight = FontWeight.Bold,
                                color = ColorDarkGreen,
                                fontSize = 14.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Text(
                    text = if (isUserTurn) "Tap dice to roll • Your turn" else "${currentPlayer.name} is rolling...",
                    fontSize = 12.sp,
                    color = ColorGrey,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }

    // Winner Victory Celebration Dialog
    winnerPlayer?.let { winner ->
        AlertDialog(
            onDismissRequest = { /* Modal stays */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🏆 ${winner.name} Wins!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ColorDarkGreen
                    )
                }
            },
            text = {
                Text(
                    text = "Incredible match! All 4 pawns safely navigated to the Home triangle.",
                    fontSize = 14.sp,
                    color = ColorDarkGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        winnerPlayer = null
                        players.forEach { p ->
                            onUpdatePawnPositions(p.id, listOf(-1, -1, -1, -1))
                        }
                        onPassTurn(players.firstOrNull()?.id ?: "")
                        turnPhase = TurnPhase.WAITING_FOR_ROLL
                        statusText = "New match started! Roll the dice."
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorLimeGreen)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rematch", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onLeaveGame,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Exit to Lobby", color = ColorDarkGrey)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showChatSheet) {
        ChatBottomSheet(
            sheetState = sheetState,
            currentUserId = currentUserId,
            messages = chatMessages,
            onSendMessage = { text ->
                onSendChatMessage(text)
            },
            onDismissRequest = {
                showChatSheet = false
            }
        )
    }
}

/**
 * Handles pawn advancement, audio effects, captures, and victory detection.
 */
private fun executeMove(
    pawn: GamePawn,
    diceVal: Int,
    players: List<LudoPlayerState>,
    playerIdx: Int,
    soundManager: SoundManager,
    onComplete: (List<LudoPlayerState>, Boolean, LudoPlayerState?) -> Unit
) {
    var captured = false

    // Calculate new step
    val newStep = if (pawn.isInYard) {
        0 // Bring out to starting square
    } else {
        pawn.step + diceVal
    }

    // Play crisp pawn placement sound
    soundManager.playPawnMove()

    val movingColor = pawn.color
    val updatedPlayers = players.map { player ->
        if (player.color == movingColor) {
            val updatedPawns = player.pawns.map { p ->
                if (p.id == pawn.id) p.copy(step = newStep) else p
            }
            player.copy(pawns = updatedPawns)
        } else {
            // Check if this move captures an opponent's pawn
            if (newStep in 0..50) {
                val myTrackIdx = LudoTrackGeometry.getTrackIndex(movingColor, newStep)
                if (myTrackIdx != null && !LudoTrackGeometry.safeTrackIndices.contains(myTrackIdx)) {
                    val updatedOppPawns = player.pawns.map { oppPawn ->
                        val oppTrackIdx = LudoTrackGeometry.getTrackIndex(oppPawn.color, oppPawn.step)
                        if (oppTrackIdx == myTrackIdx) {
                            captured = true
                            oppPawn.copy(step = -1) // Sent back to yard
                        } else oppPawn
                    }
                    player.copy(pawns = updatedOppPawns)
                } else player
            } else player
        }
    }

    if (captured) {
        soundManager.playPawnCapture()
    }

    // Check winner
    val activePlayer = updatedPlayers[playerIdx]
    val winner = if (activePlayer.hasWon) activePlayer else null

    // Bonus turn granted on 6 or successful capture
    val bonusTurn = (diceVal == 6 || captured)

    onComplete(updatedPlayers, bonusTurn, winner)
}

@Composable
private fun PlayerStatusItem(
    dotColor: Color,
    name: String,
    statusText: String,
    statusBg: Color,
    statusTextColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name.substringBefore(" •"),
                fontSize = 12.sp,
                color = ColorDarkGrey,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusBg
        ) {
            Text(
                text = statusText,
                fontSize = 10.sp,
                color = statusTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
