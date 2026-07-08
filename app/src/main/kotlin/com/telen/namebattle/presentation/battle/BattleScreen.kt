package com.telen.namebattle.presentation.battle

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.R
import com.telen.namebattle.presentation.components.Pill
import com.telen.namebattle.presentation.components.PillTone
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.theme.NbTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BattleScreen(
    sessionId: Long,
    onBattleComplete: () -> Unit,
    viewModel: BattleViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { if (it is BattleUiEvent.Complete) onBattleComplete() }
    }

    BattleScreenContent(
        state = state,
        onChoose = viewModel::choose,
        onContinue = viewModel::continueAfterSummary,
    )
}

@Composable
internal fun BattleScreenContent(
    state: BattleUiState,
    onChoose: (Long) -> Unit = {},
    onContinue: () -> Unit = {},
) {
    val c = NbTheme.colors
    Scaffold(containerColor = c.page) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(
                    stringResource(R.string.label_round_n, state.roundNumber),
                    tone = PillTone.WARNING,
                )
                Spacer(Modifier.width(9.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(c.bg2),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(state.progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c.accent),
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(state.position, style = MaterialTheme.typography.bodySmall, color = c.textMid)
            }
            Spacer(Modifier.height(16.dp))

            when (state.mode) {
                BattleMode.DUEL -> DuelView(state, onChoose)
                BattleMode.AUTO_PASS -> AutoPassView(state, onChoose)
                BattleMode.ROUND_SUMMARY -> state.summary?.let { RoundSummaryView(it, onContinue) }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun DuelView(state: BattleUiState, onChoose: (Long) -> Unit) {
    val c = NbTheme.colors
    val context = LocalContext.current

    Text(
        stringResource(R.string.label_which_name_survives),
        style = MaterialTheme.typography.bodySmall,
        color = c.textMid, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val density = LocalDensity.current
        val stageWidth = maxWidth   // capture hors du scope graphicsLayer

        // ── Animatables ───────────────────────────────────────────────────────
        // Slide: values in px, positive = rightward
        val slideL = remember { Animatable(0f) }
        val slideR = remember { Animatable(0f) }
        // Screen shake: values in dp, converted to px inside graphicsLayer
        val shakeX   = remember { Animatable(0f) }
        val shakeY   = remember { Animatable(0f) }
        val shakeRot = remember { Animatable(0f) }
        // Flash and VS badge
        val flash    = remember { Animatable(0f) }
        val vsScale  = remember { Animatable(0f) }
        val vsAlpha  = remember { Animatable(0f) }

        // Card width in px (≈ 50% of parent − gap)
        val cardWidthPx = with(density) { (stageWidth / 2 - 12.dp).toPx() }

        LaunchedEffect(state.duelKey) {
            // ── Reset ─────────────────────────────────────────────────────────
            slideL.snapTo(-cardWidthPx * 1.6f)
            slideR.snapTo(cardWidthPx * 1.6f)
            shakeX.snapTo(0f); shakeY.snapTo(0f); shakeRot.snapTo(0f)
            flash.snapTo(0f); vsScale.snapTo(0f); vsAlpha.snapTo(0f)

            // ── Crash (0 → 550ms) ─────────────────────────────────────────────
            // Damped bounce: 0% off-screen / 65% +7% / 78% -3% / 88% +2% / 100% 0
            launch {
                slideL.animateTo(0f, keyframes {
                    durationMillis = 550
                    -cardWidthPx * 1.6f at 0
                     cardWidthPx * 0.07f at 357
                    -cardWidthPx * 0.03f at 429
                     cardWidthPx * 0.02f at 484
                    0f at 550
                })
            }
            launch {
                slideR.animateTo(0f, keyframes {
                    durationMillis = 550
                     cardWidthPx * 1.6f at 0
                    -cardWidthPx * 0.07f at 357
                     cardWidthPx * 0.03f at 429
                    -cardWidthPx * 0.02f at 484
                    0f at 550
                })
            }

            // ── Impact at 340ms ───────────────────────────────────────────────
            delay(340)

            // Vibration waveform [0, 60, 30, 40]
            @Suppress("DEPRECATION")
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(Vibrator::class.java)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 30, 40), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(130)
            }

            // Screen shake — 280ms
            launch {
                shakeX.animateTo(0f, keyframes {
                    durationMillis = 280
                    -5f at 42;  5f at 84; -4f at 126; 4f at 168; -2f at 210; 0f at 280
                })
            }
            launch {
                shakeY.animateTo(0f, keyframes {
                    durationMillis = 280
                    2f at 42; -2f at 84; 1f at 126; -1f at 168; 1f at 210; 0f at 280
                })
            }
            launch {
                shakeRot.animateTo(0f, keyframes {
                    durationMillis = 280
                    -0.4f at 42; 0.4f at 84; -0.3f at 126; 0.2f at 168; -0.1f at 210; 0f at 280
                })
            }

            // White flash — 300ms
            launch {
                flash.snapTo(0.55f)
                flash.animateTo(0f, tween(durationMillis = 300))
            }

            // VS badge pops in — 250ms (10ms after impact)
            delay(10)
            launch {
                vsScale.animateTo(1f, keyframes {
                    durationMillis = 250
                    1.35f at 150
                    1f at 250
                })
            }
            vsAlpha.animateTo(1f, tween(durationMillis = 150))
        }

        // ── Stage (clipped + shake) ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(c.bg)
                .graphicsLayer {
                    val dpToPx = density.density
                    translationX = shakeX.value * dpToPx
                    translationY = shakeY.value * dpToPx
                    rotationZ = shakeRot.value
                }
        ) {
            // Left card
            DuelCard(
                name = state.leftName,
                accent = c.pro, bg = c.proBg, border = c.proBorder,
                onChoose = { onChoose(state.leftId) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp, top = 4.dp, bottom = 4.dp)
                    .width(stageWidth / 2 - 20.dp)
                    .fillMaxHeight()
                    .graphicsLayer { translationX = slideL.value }
            )

            // Right card
            DuelCard(
                name = state.rightName,
                accent = c.accent, bg = c.accentBg, border = c.accentBorder,
                onChoose = { onChoose(state.rightId) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp, top = 4.dp, bottom = 4.dp)
                    .width(stageWidth / 2 - 20.dp)
                    .fillMaxHeight()
                    .graphicsLayer { translationX = slideR.value }
            )

            // VS badge — pops in at impact
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
                    .graphicsLayer {
                        scaleX = vsScale.value
                        scaleY = vsScale.value
                        alpha = vsAlpha.value
                    }
                    .clip(CircleShape)
                    .background(c.bg3)
                    .border(2.dp, c.border2, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "VS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                )
            }

            // White flash overlay
            if (flash.value > 0f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flash.value))
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.label_other_eliminated),
        style = MaterialTheme.typography.bodySmall,
        color = c.textLo, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DuelCard(
    name: String,
    accent: Color,
    bg: Color,
    border: Color,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = NbTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.clickable(onClick = onChoose),
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                name,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(11.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(accent)
                    .padding(horizontal = 15.dp, vertical = 6.dp),
            ) {
                Text(
                    stringResource(R.string.btn_choose),
                    color = c.page,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Auto-pass & round summary ─────────────────────────────────────────────────

@Composable
private fun AutoPassView(state: BattleUiState, onChoose: (Long) -> Unit) {
    val c = NbTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp), color = c.successBg,
        border = BorderStroke(1.dp, c.successBorder), modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(c.success)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    stringResource(R.string.label_auto_qualified),
                    color = c.page,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(state.autoName, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = c.textHi)
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(R.string.label_auto_pass_desc),
        style = MaterialTheme.typography.bodySmall, color = c.textLo,
        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(14.dp))
    PrimaryButton(stringResource(R.string.btn_continue), onClick = { onChoose(state.autoId) })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoundSummaryView(s: RoundSummary, onContinue: () -> Unit) {
    val c = NbTheme.colors
    val survivorsLabel = pluralStringResource(
        R.plurals.label_survivors, s.survivors.size, s.survivors.size
    )
    val eliminatedLabel = pluralStringResource(
        R.plurals.label_eliminated_count, s.eliminated.size, s.eliminated.size
    )

    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Pill(
                stringResource(R.string.label_round_done, s.finishedRound),
                tone = PillTone.WARNING,
            )
            Spacer(Modifier.height(13.dp))
            Text(survivorsLabel, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.textHi)
            Text(
                eliminatedLabel,
                style = MaterialTheme.typography.bodySmall,
                color = c.textLo,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            stringResource(R.string.label_advance_to_round, s.nextRound),
            style = MaterialTheme.typography.titleMedium,
            color = c.textMid,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            s.survivors.forEach { ChipTag(it, c.success, c.successBg, c.successBorder) }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.label_eliminated_section),
            style = MaterialTheme.typography.titleMedium,
            color = c.textMid,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            s.eliminated.forEach { StrikeTag(it) }
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            stringResource(R.string.btn_start_next_round, s.nextRound),
            onClick = onContinue,
        )
    }
}

@Composable
private fun ChipTag(text: String, fg: Color, bg: Color, border: Color) {
    Text(
        text, fontSize = 12.sp, color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 4.dp)
    )
}

@Composable
private fun StrikeTag(text: String) {
    val c = NbTheme.colors
    Text(
        text, fontSize = 12.sp, color = c.textLo, textDecoration = TextDecoration.LineThrough,
        modifier = Modifier
            .alpha(0.6f)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, c.border, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 4.dp)
    )
}
