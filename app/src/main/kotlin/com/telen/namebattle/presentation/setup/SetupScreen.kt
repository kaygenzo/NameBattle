package com.telen.namebattle.presentation.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.R
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.components.NbTextField
import com.telen.namebattle.presentation.components.NbTopBar
import com.telen.namebattle.presentation.components.SecondaryButton
import com.telen.namebattle.presentation.components.SegmentedTabs
import com.telen.namebattle.presentation.theme.NbTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupScreen(
    onSessionCreated: (sessionId: Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SetupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { effect ->
            if (effect is SetupUiEvent.SessionCreated) onSessionCreated(effect.sessionId)
        }
    }

    SetupScreenContent(
        state = state,
        onBack = onBack,
        onGenderChange = viewModel::onGenderChange,
        onSoloToggle = viewModel::onSoloToggle,
        onParent1Name = viewModel::onParent1Name,
        onParent1Password = viewModel::onParent1Password,
        onParent1Confirm = viewModel::onParent1Confirm,
        onLockParent1 = viewModel::lockParent1,
        onParent2Name = viewModel::onParent2Name,
        onParent2Password = viewModel::onParent2Password,
        onParent2Confirm = viewModel::onParent2Confirm,
        onLockParent2 = viewModel::lockParent2,
    )
}

@Composable
internal fun SetupScreenContent(
    state: SetupUiState,
    onBack: () -> Unit = {},
    onGenderChange: (Gender) -> Unit = {},
    onSoloToggle: (Boolean) -> Unit = {},
    onParent1Name: (String) -> Unit = {},
    onParent1Password: (String) -> Unit = {},
    onParent1Confirm: (String) -> Unit = {},
    onLockParent1: () -> Unit = {},
    onParent2Name: (String) -> Unit = {},
    onParent2Password: (String) -> Unit = {},
    onParent2Confirm: (String) -> Unit = {},
    onLockParent2: () -> Unit = {},
) {
    val c = NbTheme.colors

    val genders = listOf(Gender.BOY, Gender.GIRL)
    val genderLabels = listOf(
        stringResource(R.string.label_gender_boy),
        stringResource(R.string.label_gender_girl),
    )

    Scaffold(containerColor = c.page) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            NbTopBar(title = stringResource(R.string.title_new_session), onBack = onBack)

            SectionLabel(stringResource(R.string.label_gender_section))
            SegmentedTabs(
                options = genderLabels,
                selectedIndex = genders.indexOf(state.gender),
                onSelect = { onGenderChange(genders[it]) },
            )

            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.label_parent_1))
            ParentBlock(
                name = state.parent1Name,
                password = state.parent1Password,
                confirm = state.parent1Confirm,
                locked = state.parent1Locked,
                canLock = state.parent1CanLock,
                passwordsMatch = state.parent1PasswordsMatch,
                namePlaceholder = stringResource(R.string.placeholder_name_p1),
                onName = onParent1Name,
                onPassword = onParent1Password,
                onConfirm = onParent1Confirm,
                onLock = onLockParent1,
            )

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.label_solo_mode),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textHi
                    )
                    Text(
                        stringResource(R.string.label_solo_mode_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textLo
                    )
                }
                Switch(
                    checked = state.soloMode,
                    onCheckedChange = onSoloToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = c.page,
                        checkedTrackColor = c.accent,
                        uncheckedThumbColor = c.textMid,
                        uncheckedTrackColor = c.bg2,
                        uncheckedBorderColor = c.border2,
                    ),
                )
            }

            if (!state.soloMode) {
                Spacer(Modifier.height(18.dp))
                SectionLabel(stringResource(R.string.label_parent_2))
                ParentBlock(
                    name = state.parent2Name,
                    password = state.parent2Password,
                    confirm = state.parent2Confirm,
                    locked = state.parent2Locked,
                    canLock = state.parent2CanLock,
                    passwordsMatch = state.parent2PasswordsMatch,
                    namePlaceholder = stringResource(R.string.placeholder_name_p2),
                    onName = onParent2Name,
                    onPassword = onParent2Password,
                    onConfirm = onParent2Confirm,
                    onLock = onLockParent2,
                    enabled = state.parent1Locked,
                )
            }

            if (state.isCreating) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = NbTheme.colors.accent,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ParentBlock(
    name: String,
    password: String,
    confirm: String,
    locked: Boolean,
    canLock: Boolean,
    passwordsMatch: Boolean,
    namePlaceholder: String,
    onName: (String) -> Unit,
    onPassword: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onLock: () -> Unit,
    enabled: Boolean = true,
) {
    val c = NbTheme.colors
    val alpha = if (enabled) 1f else 0.4f

    AnimatedVisibility(
        visible = locked,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(c.accentBg)
                .border(1.dp, c.accentBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(c.accent),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    name.take(1).uppercase(),
                    color = c.page,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            Spacer(Modifier.padding(horizontal = 8.dp))
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                color = c.textHi,
                modifier = Modifier.weight(1f),
            )
            Text("✅", fontSize = 18.sp)
        }
    }

    AnimatedVisibility(
        visible = !locked,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(Modifier.alpha(alpha)) {
            NbTextField(
                name,
                onName,
                stringResource(R.string.label_first_name),
                placeholder = namePlaceholder,
                enabled = enabled,
            )
            Spacer(Modifier.height(10.dp))
            NbTextField(
                password,
                onPassword,
                stringResource(R.string.label_password),
                isPassword = true,
                enabled = enabled,
            )
            Spacer(Modifier.height(10.dp))
            NbTextField(
                confirm,
                onConfirm,
                stringResource(R.string.label_confirm_password),
                isPassword = true,
                isError = confirm.isNotEmpty() && !passwordsMatch,
                enabled = enabled,
            )
            if (confirm.isNotEmpty() && !passwordsMatch) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.error_passwords_mismatch),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.danger,
                )
            }
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(R.string.btn_validate),
                onClick = onLock,
                enabled = canLock && enabled,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val c = NbTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = c.textMid,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
