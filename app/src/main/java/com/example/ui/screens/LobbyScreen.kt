package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Player
import com.example.ui.components.CrownIcon
import com.example.ui.components.DiceIcon
import com.example.ui.components.LudoPawnAvatar
import com.example.ui.theme.Dimens
import com.example.ui.theme.LudoBackground
import com.example.ui.theme.LudoBorder
import com.example.ui.theme.LudoHostBadgeBg
import com.example.ui.theme.LudoHostBadgeText
import com.example.ui.theme.LudoPillTabActive
import com.example.ui.theme.LudoPrimaryLime
import com.example.ui.theme.LudoSurface
import com.example.ui.theme.LudoSurfaceVariant
import com.example.ui.theme.LudoTextHeadingsDark
import com.example.ui.theme.LudoTextMuted
import com.example.ui.theme.LudoTextPrimary
import com.example.ui.theme.LudoTextSecondary
import com.example.ui.theme.LudoTextTitle
import com.example.ui.theme.PillShape
import com.example.ui.theme.Shapes
import com.example.viewmodel.LobbyUiState

@Composable
fun LobbyScreen(
    state: LobbyUiState,
    onCreateRoom: () -> Unit,
    onOtpDigitChange: (Int, String) -> Unit,
    onJoinRoom: () -> Unit,
    onStartGame: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onDismissError()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LudoBackground)
            .statusBarsPadding(),
        containerColor = LudoBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LobbyTopBar(
                onProfileClick = onNavigateToSettings
            )
        },
        bottomBar = {
            LobbyBottomNavigation(
                selectedTab = 0,
                onSelectTab = { index ->
                    when (index) {
                        1 -> onNavigateToRules()
                        2 -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.ScreenHorizontalPadding),
            contentPadding = PaddingValues(top = Dimens.SpacingSmall, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Greeting Card
            item(key = "greeting_card", contentType = "card") {
                GreetingCard(playerName = state.currentUserName)
            }

            // 2. Create Private Room Card
            item(key = "create_room_card", contentType = "card") {
                CreateRoomCard(onCreateRoom = onCreateRoom)
            }

            // 3. Join Room Card
            item(key = "join_room_card", contentType = "card") {
                JoinRoomCard(
                    otpDigits = state.otpDigits,
                    onOtpDigitChange = onOtpDigitChange,
                    onJoinRoom = onJoinRoom
                )
            }

            // 4. Players Count Header
            item(key = "players_header", contentType = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpacingSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.players_count_format, state.players.size),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LudoTextTitle,
                        modifier = Modifier.testTag("players_count_header")
                    )
                }
            }

            // 5. Host Card
            val hostPlayer = state.players.firstOrNull { it.isHost } ?: state.players.firstOrNull()
            if (hostPlayer != null) {
                item(key = "host_player_${hostPlayer.uid}", contentType = "player_item") {
                    HostPlayerCard(player = hostPlayer)
                }
            }

            // 6. Empty Waiting Player Slots (3 slots)
            val emptySlotsCount = (4 - state.players.size).coerceAtLeast(0)
            items(
                count = emptySlotsCount,
                key = { "empty_slot_$it" },
                contentType = { "empty_slot" }
            ) {
                EmptyPlayerSlotCard()
            }

            // 7. Start Game Button (Host only)
            if (state.isHost) {
                item(key = "start_game_button", contentType = "action_button") {
                    Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                    Button(
                        onClick = onStartGame,
                        enabled = state.canStartGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight)
                            .testTag("start_game_button"),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LudoPrimaryLime,
                            contentColor = Color(0xFF111827),
                            disabledContainerColor = LudoPrimaryLime.copy(alpha = 0.4f),
                            disabledContentColor = Color(0xFF111827).copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (state.players.size < 2) "Waiting for players (Min 2)..." else stringResource(id = R.string.start_game),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top App Bar matching Screenshot 1:
 * Hamburger menu on left, "Ludo Lobby" title, Notification bell with dot, Profile avatar.
 */
@Composable
private fun LobbyTopBar(
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenHorizontalPadding, vertical = Dimens.SpacingSmall)
            .testTag("lobby_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { /* Menu drawer affordance */ },
            modifier = Modifier.testTag("menu_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = stringResource(id = R.string.cd_menu),
                tint = Color(0xFF1F2937),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = stringResource(id = R.string.lobby_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = LudoTextTitle
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Notification Bell with Badge Dot
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = { /* Notifications */ },
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(id = R.string.cd_notifications),
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, end = 10.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpacingSmall))

            // Profile Avatar Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB))
                    .clickable { onProfileClick() }
                    .testTag("profile_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = stringResource(id = R.string.cd_profile),
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Greeting Card with smiling Ludo Pawn avatar and player name.
 */
@Composable
private fun GreetingCard(playerName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("greeting_card"),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardInternalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LudoPawnAvatar(
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(Dimens.SpacingLarge))

            Column {
                Text(
                    text = stringResource(id = R.string.greeting_hello, playerName),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LudoTextTitle
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(id = R.string.greeting_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LudoTextSecondary
                )
            }
        }
    }
}

/**
 * Create Private Room Card with prominent Lime Green Pill Button.
 */
@Composable
private fun CreateRoomCard(onCreateRoom: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("create_room_card"),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardInternalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onCreateRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight)
                    .testTag("create_room_button"),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LudoPrimaryLime,
                    contentColor = Color(0xFF111827)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.create_private_room).replace("+ ", ""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

            Text(
                text = stringResource(id = R.string.create_room_desc),
                style = MaterialTheme.typography.bodySmall,
                color = LudoTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Join Room Card featuring 6 separate square digits input boxes.
 */
@Composable
private fun JoinRoomCard(
    otpDigits: List<String>,
    onOtpDigitChange: (Int, String) -> Unit,
    onJoinRoom: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("join_room_card"),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardInternalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.join_room_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LudoTextTitle
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.join_room_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = LudoTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            // 6 Separate Square Digit Input Boxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_code_inputs_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until 6) {
                    val digit = otpDigits.getOrElse(index) { "" }
                    DigitInputBox(
                        digit = digit,
                        index = index,
                        onValueChange = { onOtpDigitChange(index, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            Button(
                onClick = onJoinRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight)
                    .testTag("join_room_button"),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LudoPrimaryLime,
                    contentColor = Color(0xFF111827)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.join_room_button),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}

/**
 * Individual 48x48dp rounded square digit box matching Screenshot 1.
 */
@Composable
private fun DigitInputBox(
    digit: String,
    index: Int,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(Dimens.OtpBoxSize)
            .clip(RoundedCornerShape(Dimens.OtpBoxCorner))
            .background(LudoSurfaceVariant)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (digit.isNotEmpty()) LudoPrimaryLime else LudoBorder
                ),
                shape = RoundedCornerShape(Dimens.OtpBoxCorner)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = digit,
            onValueChange = { input ->
                val clean = input.filter { it.isDigit() }
                if (clean.length <= 1) {
                    onValueChange(clean)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = LudoTextTitle
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
                .testTag("digit_input_$index"),
            decorationBox = { innerTextField ->
                if (digit.isEmpty()) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.titleLarge,
                        color = LudoTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        )
    }
}

/**
 * Host Player Card with Avatar, HOST badge, Crown icon, and Player Name.
 */
@Composable
private fun HostPlayerCard(player: Player) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("host_player_card"),
        shape = Shapes.medium,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.CardInternalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LudoPawnAvatar(
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(Dimens.SpacingLarge))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LudoHostBadgeBg,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.host_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LudoHostBadgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    CrownIcon(
                        size = 14.dp,
                        tint = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (player.name == "Player") stringResource(id = R.string.you_label) else player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LudoTextTitle
                )
            }
        }
    }
}

/**
 * Empty Waiting Player Slot Card matching Screenshot 1.
 */
@Composable
private fun EmptyPlayerSlotCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_player_slot_card"),
        shape = Shapes.medium,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        border = BorderStroke(1.dp, LudoBorder.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.CardInternalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Dashed circular plus icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            BorderStroke(1.5.dp, Color(0xFFD1D5DB)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.cd_add_player),
                        tint = LudoPrimaryLime,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.SpacingLarge))

                Text(
                    text = stringResource(id = R.string.waiting_player),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LudoTextMuted
                )
            }

            Text(
                text = "—",
                style = MaterialTheme.typography.titleMedium,
                color = LudoTextMuted
            )
        }
    }
}

/**
 * Bottom Navigation Bar with 3 tabs (Lobby, Rules, Settings).
 * The active tab displays a soft lime pill indicator background.
 */
@Composable
private fun LobbyBottomNavigation(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("lobby_bottom_navigation"),
        color = LudoSurface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Lobby
            BottomNavItem(
                label = stringResource(id = R.string.tab_lobby),
                selected = selectedTab == 0,
                onClick = { onSelectTab(0) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(id = R.string.tab_lobby),
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            // Tab 1: Rules
            BottomNavItem(
                label = stringResource(id = R.string.tab_rules),
                selected = selectedTab == 1,
                onClick = { onSelectTab(1) },
                icon = { tint ->
                    DiceIcon(
                        tint = tint,
                        size = 22.dp
                    )
                }
            )

            // Tab 2: Settings
            BottomNavItem(
                label = stringResource(id = R.string.tab_settings),
                selected = selectedTab == 2,
                onClick = { onSelectTab(2) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.tab_settings),
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val activeColor = LudoTextPrimary
    val inactiveColor = Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(if (selected) LudoPillTabActive else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(if (selected) activeColor else inactiveColor)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) activeColor else inactiveColor
        )
    }
}
