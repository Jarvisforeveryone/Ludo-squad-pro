package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.LudoPawnAvatar
import com.example.ui.theme.Dimens
import com.example.ui.theme.LudoBackground
import com.example.ui.theme.LudoBorder
import com.example.ui.theme.LudoPawnAvatarBg
import com.example.ui.theme.LudoPrimaryLime
import com.example.ui.theme.LudoSurface
import com.example.ui.theme.LudoTextHeadingsDark
import com.example.ui.theme.LudoTextPrimary
import com.example.ui.theme.LudoTextSecondary
import com.example.ui.theme.PillShape
import com.example.ui.theme.Shapes

@Composable
fun SplashScreen(
    initialName: String,
    initialAvatarUrl: String?,
    onContinue: (String, String?) -> Unit
) {
    var nameInput by remember(initialName) { mutableStateOf(initialName) }
    var selectedAvatarUri by remember(initialAvatarUrl) {
        mutableStateOf(initialAvatarUrl?.let { Uri.parse(it) })
    }

    // Photo picker launcher (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAvatarUri = uri
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoBackground)
            .statusBarsPadding(),
        color = LudoBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenHorizontalPadding)
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero App Pawn Mascot
            LudoPawnAvatar(
                size = 110.dp,
                backgroundColor = LudoPawnAvatarBg
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing2XLarge))

            Text(
                text = stringResource(id = R.string.splash_welcome),
                style = MaterialTheme.typography.headlineLarge,
                color = LudoTextHeadingsDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("splash_welcome_title")
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

            Text(
                text = stringResource(id = R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = LudoTextSecondary,
                modifier = Modifier.testTag("splash_tagline_text")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Profile Picture Picker Box with Coil AsyncImage
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(LudoSurface)
                    .border(2.dp, LudoPrimaryLime, CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("profile_avatar_picker"),
                contentAlignment = Alignment.Center
            ) {
                if (selectedAvatarUri != null) {
                    AsyncImage(
                        model = selectedAvatarUri,
                        contentDescription = "User Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = stringResource(id = R.string.add_photo),
                            tint = LudoTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = LudoPrimaryLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedAvatarUri != null) "Tap to change photo" else stringResource(id = R.string.add_photo),
                style = MaterialTheme.typography.bodyMedium,
                color = LudoTextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(text = stringResource(id = R.string.enter_name_hint)) },
                singleLine = true,
                shape = Shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LudoPrimaryLime,
                    unfocusedBorderColor = LudoBorder,
                    focusedLabelColor = LudoTextPrimary,
                    unfocusedContainerColor = LudoSurface,
                    focusedContainerColor = LudoSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val finalName = nameInput.trim().ifEmpty { "Player" }
                    onContinue(finalName, selectedAvatarUri?.toString())
                },
                enabled = nameInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight)
                    .testTag("lets_play_button"),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LudoPrimaryLime,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.lets_play),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
