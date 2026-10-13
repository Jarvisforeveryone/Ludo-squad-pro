package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.Dimens
import com.example.ui.theme.LudoBackground
import com.example.ui.theme.LudoPillTabActive
import com.example.ui.theme.LudoPrimaryLime
import com.example.ui.theme.LudoSurface
import com.example.ui.theme.LudoTextHeadingsDark
import com.example.ui.theme.LudoTextPrimary
import com.example.ui.theme.LudoTextSecondary
import com.example.ui.theme.LudoTextTitle
import com.example.ui.theme.Shapes

data class LudoRuleItem(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val rules = listOf(
        LudoRuleItem(
            id = "rule_start",
            titleRes = R.string.rule_1_title,
            descRes = R.string.rule_1_desc,
            icon = Icons.Outlined.PlayArrow,
            iconTint = LudoPrimaryLime
        ),
        LudoRuleItem(
            id = "rule_six",
            titleRes = R.string.rule_2_title,
            descRes = R.string.rule_2_desc,
            icon = Icons.Outlined.CheckCircle,
            iconTint = Color(0xFF2563EB)
        ),
        LudoRuleItem(
            id = "rule_capture",
            titleRes = R.string.rule_3_title,
            descRes = R.string.rule_3_desc,
            icon = Icons.Outlined.Lock,
            iconTint = Color(0xFFDC2626)
        ),
        LudoRuleItem(
            id = "rule_safe",
            titleRes = R.string.rule_4_title,
            descRes = R.string.rule_4_desc,
            icon = Icons.Outlined.Shield,
            iconTint = Color(0xFF16A34A)
        ),
        LudoRuleItem(
            id = "rule_win",
            titleRes = R.string.rule_5_title,
            descRes = R.string.rule_5_desc,
            icon = Icons.Outlined.EmojiEvents,
            iconTint = Color(0xFFEAB308)
        )
    )

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
                        text = stringResource(id = R.string.rules_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LudoTextTitle
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("rules_back_button")
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
            items(
                items = rules,
                key = { it.id },
                contentType = { "rule_card" }
            ) { rule ->
                RuleCard(rule = rule)
            }
        }
    }
}

@Composable
private fun RuleCard(rule: LudoRuleItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_${rule.id}"),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = LudoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardInternalPadding),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LudoPillTabActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rule.icon,
                    contentDescription = null,
                    tint = rule.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpacingLarge))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = rule.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LudoTextHeadingsDark
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingXSmall))

                Text(
                    text = stringResource(id = rule.descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LudoTextSecondary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}
