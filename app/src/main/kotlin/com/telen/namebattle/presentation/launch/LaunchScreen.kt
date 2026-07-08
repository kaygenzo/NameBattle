package com.telen.namebattle.presentation.launch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.R
import com.telen.namebattle.presentation.components.NbDivider
import com.telen.namebattle.presentation.components.NbTopBar
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.components.StatTile
import com.telen.namebattle.presentation.theme.NbTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LaunchScreen(
    sessionId: Long,
    onBattleStarted: () -> Unit,
    onBack: () -> Unit,
    viewModel: LaunchViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = NbTheme.colors

    LaunchedEffect(Unit) {
        viewModel.events.collect { if (it is LaunchUiEvent.BattleStarted) onBattleStarted() }
    }

    LaunchScreenContent(state = state, onBack = onBack, onStart = viewModel::start)
}

@Composable
internal fun LaunchScreenContent(
    state: LaunchUiState,
    onBack: () -> Unit = {},
    onStart: () -> Unit = {},
) {
    val c = NbTheme.colors
    Scaffold(containerColor = c.page) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            NbTopBar(title = stringResource(R.string.title_launch), onBack = onBack)

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("⚔️", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.label_ready_for_battle),
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.textHi
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.label_lists_closed),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLo
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.parent2Name != null) {
                    StatTile(
                        label = state.parent2Name!!,
                        value = state.parent2Count.toString(),
                        accent = c.pro, bg = c.proBg, border = c.proBorder,
                        sub = stringResource(R.string.label_prenoms),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "+",
                        color = c.textLo,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
                StatTile(
                    label = state.parent1Name,
                    value = state.parent1Count.toString(),
                    accent = c.accent, bg = c.accentBg, border = c.accentBorder,
                    sub = stringResource(R.string.label_prenoms),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(13.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = c.bg3,
                border = BorderStroke(1.dp, c.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.label_total_in_arena),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )
                    Text(
                        pluralStringResource(
                        R.plurals.label_total_prenoms, state.total, state.total
                    ),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textHi,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.label_objective, state.targetFinalists, state.roundsEstimate
                ),
                style = MaterialTheme.typography.bodySmall,
                color = c.textLo,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            NbDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.warningBg)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔒", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.label_secret_order),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.warning,
                )
            }

            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = if (state.isStarting) {
                    stringResource(R.string.btn_preparing)
                } else {
                    stringResource(R.string.btn_launch_battle)
                },
                onClick = onStart,
                enabled = state.canStart && !state.isStarting,
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}
