package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.Dimens
import com.example.ui.theme.LudoBackground
import com.example.ui.theme.LudoBorder
import com.example.ui.theme.LudoErrorRed
import com.example.ui.theme.LudoPillTabActive
import com.example.ui.theme.LudoPrimaryLime
import com.example.ui.theme.LudoSurface
import com.example.ui.theme.LudoTextHeadingsDark
import com.example.ui.theme.LudoTextMuted
import com.example.ui.theme.LudoTextPrimary
import com.example.ui.theme.LudoTextSecondary
import com.example.ui.theme.LudoTextTitle
import com.example.ui.theme.PillShape
import com.example.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentName: String,
    soundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onUpdateName: (String) -> Unit,
    onNavigateToRules: () -> Unit,
    onClearData: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var editedName by remember(currentName) { mutableStateOf(currentName) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LudoBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = LudoBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LudoTextTitle
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back),
                            tint = LudoTextTitle
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LudoBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.ScreenHorizontalPadding),
            contentPadding = PaddingValues(top = Dimens.SpacingSmall, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
        ) {
            // 1. Sound Effects Toggle Card
            item(key = "setting_sound", contentType = "setting_card") {
                SettingCard(
                    icon = Icons.Outlined.VolumeUp,
                    title = stringResource(id = R.string.setting_sound),
                    subtitle = stringResource(id = R.string.setting_sound_desc),
                    trailing = {
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = onSoundToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LudoSurface,
                                checkedTrackColor = LudoPrimaryLime,
                                uncheckedThumbColor = LudoSurface,
                                uncheckedTrackColor = LudoBorder
                            ),
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }
                )
            }

            // 2. Change Profile Card
            item(key = "setting_profile", contentType = "setting_card") {
                SettingCard(
                    icon = Icons.Outlined.Person,
                    title = stringResource(id = R.string.setting_profile),
                    subtitle = currentName,
                    onClick = { showEditProfileDialog = true }
                )
            }

            // 3. How to Play Rules Card
            item(key = "setting_rules", contentType = "setting_card") {
                SettingCard(
                    icon = Icons.Outlined.HelpOutline,
                    title = stringResource(id = R.string.setting_rules),
                    subtitle = stringResource(id = R.string.rules_title),
                    onClick = onNavigateToRules
                )
            }

            // 4. WhatsApp Voice Chat Deep Link Card
            item(key = "setting_whatsapp", contentType = "setting_card") {
                SettingCard(
                    icon = Icons.Outlined.Chat,
                    title = stringResource(id = R.string.setting_whatsapp),
                    subtitle = stringResource(id = R.string.setting_whatsapp_desc),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/"))
                        context.startActivity(intent)
                    }
                )
            }

            // 5. About & Version Card
            item(key = "setting_about", contentType = "setting_card") {
                SettingCard(
                    icon = Icons.Outlined.Info,
                    title = stringResource(id = R.string.setting_about),
                    subtitle = stringResource(id = R.string.setting_version)
                )
            }

            // 6. Clear Data & Log Out Card
            item(key = "setting_clear_data", contentType = "setting_card") {
                SettingCard(
                    icon = Icons.Outlined.Delete,
                    iconTint = LudoErrorRed,
                    title = stringResource(id = R.string.setting_clear_data),
                    subtitle = stringResource(id = R.string.confirm),
                    onClick = { showClearDataDialog = true }
                )
            }
        }
    }

    // Profile Edit Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.setting_profile),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text(text = stringResource(id = R.string.enter_name_hint)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LudoPrimaryLime,
                        focusedLabelColor = LudoTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateName(editedName)
                        showEditProfileDialog = false
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = LudoPrimaryLime)
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm),
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            containerColor = LudoSurface,
            shape = Shapes.large
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.setting_clear_data),
                    fontWeight = FontWeight.Bold,
                    color = LudoErrorRed
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.leave_game_confirm),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        onClearData()
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = LudoErrorRed)
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDataDialog = false },
                    shape = PillShape
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            containerColor = LudoSurface,
            shape = Shapes.large
        )
    }
}

@Composable
private fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = LudoTextPrimary,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("setting_card_$title"),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardInternalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LudoPillTabActive),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.SpacingLarge))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LudoTextHeadingsDark
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = LudoTextMuted
                    )
                }
            }

            if (trailing != null) {
                trailing()
            }
        }
    }
}
