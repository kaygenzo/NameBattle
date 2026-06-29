package com.telen.namebattle.presentation.results

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.R
import com.telen.namebattle.presentation.components.NbCard
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.components.SecondaryButton
import com.telen.namebattle.presentation.theme.NbTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ResultsScreen(
    sessionId: Long,
    onNewSession: () -> Unit,
    onReplay: () -> Unit,
    viewModel: ResultsViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ResultsScreenContent(state = state, onNewSession = onNewSession, onReplay = onReplay)
}

@Composable
internal fun ResultsScreenContent(
    state: ResultsUiState,
    onNewSession: () -> Unit = {},
    onReplay: () -> Unit = {},
) {
    val c = NbTheme.colors
    val rounds = maxOf(state.roundsPlayed, 1)
    val survivedLabel = if (state.finalists.size > 1)
        stringResource(R.string.label_finalists_survived, state.finalists.size, rounds)
    else
        stringResource(R.string.label_finalists_survived_one, rounds)

    Scaffold(containerColor = c.page) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.title_finalists),
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.textHi
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    survivedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLo,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(18.dp))
            NbCard {
                Text(
                    stringResource(R.string.label_your_turn_to_choose),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textMid
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.label_names_tied),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLo,
                )
                Spacer(Modifier.height(10.dp))
                state.finalists.forEachIndexed { i, name ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 11.dp)
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(c.accent),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textHi)
                    }
                    if (i < state.finalists.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = c.border)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            PrimaryButton(stringResource(R.string.btn_replay), onClick = onReplay)
            Spacer(Modifier.height(8.dp))
            SecondaryButton(stringResource(R.string.btn_back_to_home), onClick = onNewSession)

            Spacer(Modifier.height(28.dp))
        }
    }
}
