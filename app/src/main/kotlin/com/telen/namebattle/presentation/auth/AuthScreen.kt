package com.telen.namebattle.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.R
import com.telen.namebattle.presentation.components.NbDivider
import com.telen.namebattle.presentation.components.NbTextField
import com.telen.namebattle.presentation.components.NbTopBar
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.theme.NbTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AuthScreen(
    sessionId: Long,
    onAuthenticated: (parentIndex: Int) -> Unit,
    onLaunchBattle: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = NbTheme.colors

    LaunchedEffect(Unit) {
        viewModel.events.collect { effect ->
            when (effect) {
                is AuthUiEvent.Authenticated -> onAuthenticated(effect.parentIndex)
                AuthUiEvent.LaunchBattle -> onLaunchBattle()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.resetSelection()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AuthScreenContent(
        state = state,
        onBack = onBack,
        onSelectParent = viewModel::onSelectParent,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onLaunchBattle = viewModel::onLaunchBattle,
    )
}

@Composable
internal fun AuthScreenContent(
    state: AuthUiState,
    onBack: () -> Unit = {},
    onSelectParent: (Int) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onLaunchBattle: () -> Unit = {},
) {
    val c = NbTheme.colors
    Scaffold(containerColor = c.page) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            NbTopBar(title = stringResource(R.string.title_connexion), onBack = onBack)
            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = c.accent)
            } else {
                Text(
                    stringResource(R.string.label_who_connects),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textMid,
                )
                Spacer(Modifier.height(12.dp))

                state.parents.forEach { parent ->
                    val selected = state.selectedParentIndex == parent.parentIndex
                    ParentCard(
                        parent = parent,
                        selected = selected,
                        onClick = { onSelectParent(parent.parentIndex) },
                    )
                    Spacer(Modifier.height(8.dp))
                }

                AnimatedVisibility(
                    visible = state.selectedParentIndex != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        NbTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = stringResource(R.string.label_password),
                            isPassword = true,
                            isError = state.error != null,
                        )
                        state.error?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = c.danger)
                        }
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = if (state.isChecking) {
                                stringResource(R.string.btn_checking)
                            } else {
                                stringResource(R.string.btn_sign_in)
                            },
                            onClick = onSubmit,
                            enabled = state.password.isNotBlank() && !state.isChecking,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.canStartBattle && state.selectedParentIndex == null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        NbDivider()
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(
                            text = stringResource(R.string.btn_launch_battle),
                            onClick = onLaunchBattle,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentCard(
    parent: ParentOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = NbTheme.colors
    val borderColor = if (selected) c.accent else c.border
    val bgColor = if (selected) c.accentBg else c.bg3
    val prenomLabel = if (parent.shortlistCount > 1)
        stringResource(R.string.label_prenoms) else stringResource(R.string.label_prenom)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) c.accent else c.accentBg)
                .border(1.dp, c.accentBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                parent.name.take(1).uppercase(),
                color = if (selected) c.page else c.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                parent.name,
                style = MaterialTheme.typography.titleMedium,
                color = c.textHi,
            )
            Text(
                buildString {
                    append(parent.shortlistCount)
                    append(" ")
                    append(prenomLabel)
                    if (parent.listValidated) {
                        append(" ${stringResource(R.string.label_list_validated)}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = c.textLo,
            )
        }
        if (selected) {
            Text("›", fontSize = 20.sp, color = c.accent, fontWeight = FontWeight.Bold)
        }
    }
}
