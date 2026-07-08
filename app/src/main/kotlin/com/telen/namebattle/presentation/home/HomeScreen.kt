package com.telen.namebattle.presentation.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.R
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.components.ConfirmDialog
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.components.SecondaryButton
import com.telen.namebattle.presentation.theme.NbTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onCreateSession: () -> Unit,
    onManageLists: (sessionId: Long) -> Unit,
    onLaunchBattle: (sessionId: Long) -> Unit,
    onResumeBattle: (sessionId: Long) -> Unit,
    onViewResults: (sessionId: Long) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeUiEvent.NavigateToSetup -> onCreateSession()
                is HomeUiEvent.SharePdf -> {
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", event.file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val title = context.getString(R.string.share_pdf_chooser_title)
                    context.startActivity(Intent.createChooser(intent, title))
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeScreenContent(
        state = state,
        onCreateSession = viewModel::onCreateSession,
        onManageLists = onManageLists,
        onStartBattle = onLaunchBattle,
        onResumeBattle = onResumeBattle,
        onViewResults = onViewResults,
        onRestartBattle = { sessionId ->
            viewModel.onRestartBattle(sessionId) { onLaunchBattle(sessionId) }
        },
        onExportPdf = viewModel::onExportPdf,
        onDeleteSession = viewModel::onDeleteSession,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDeleteDismissed = viewModel::onDeleteDismissed,
    )
}

@Composable
internal fun HomeScreenContent(
    state: HomeUiState,
    onCreateSession: () -> Unit,
    onManageLists: (sessionId: Long) -> Unit,
    onStartBattle: (sessionId: Long) -> Unit,
    onResumeBattle: (sessionId: Long) -> Unit,
    onViewResults: (sessionId: Long) -> Unit,
    onRestartBattle: (sessionId: Long) -> Unit,
    onExportPdf: (sessionId: Long) -> Unit = {},
    onDeleteSession: (sessionId: Long) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteDismissed: () -> Unit,
) {
    val c = NbTheme.colors

    Scaffold(containerColor = c.page) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = c.textHi,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = c.textLo,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = c.accent)
            } else {
                state.sessions.forEach { summary ->
                    SessionCard(
                        summary = summary,
                        isExporting = state.isExportingSessionId == summary.sessionId,
                        onStartBattle = { onStartBattle(summary.sessionId) },
                        onResumeBattle = { onResumeBattle(summary.sessionId) },
                        onViewResults = { onViewResults(summary.sessionId) },
                        onRestartBattle = { onRestartBattle(summary.sessionId) },
                        onExportPdf = { onExportPdf(summary.sessionId) },
                        onManage = { onManageLists(summary.sessionId) },
                        onDelete = { onDeleteSession(summary.sessionId) },
                        modifier = Modifier.widthIn(max = 420.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                }
                PrimaryButton(
                    text = stringResource(R.string.btn_new_session),
                    onClick = onCreateSession,
                    modifier = Modifier.widthIn(max = 420.dp),
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    if (state.pendingDeleteSessionId != null) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_session_title),
            message = stringResource(R.string.dialog_delete_session_message),
            confirmLabel = stringResource(R.string.btn_delete_all),
            dismissLabel = stringResource(R.string.btn_cancel),
            onConfirm = onDeleteConfirmed,
            onDismiss = onDeleteDismissed,
        )
    }
}

private val GenderBoy = Color(0xFF7C9CFF)
private val GenderGirl = Color(0xFFFF79C6)

@Composable
private fun SessionCard(
    summary: SessionSummary,
    isExporting: Boolean,
    onStartBattle: () -> Unit,
    onResumeBattle: () -> Unit,
    onViewResults: () -> Unit,
    onRestartBattle: () -> Unit,
    onExportPdf: () -> Unit,
    onManage: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = NbTheme.colors
    val genderColor = when (summary.gender) {
        Gender.BOY -> GenderBoy
        Gender.GIRL -> GenderGirl
    }
    val prenomLabel = if (summary.totalNames > 1)
        stringResource(R.string.label_prenoms) else stringResource(R.string.label_prenom)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = c.bg3,
        border = BorderStroke(1.dp, c.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(genderColor)
            )
            Column(Modifier.padding(14.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            summary.parentNames,
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHi,
                        )
                        Text(
                            buildString {
                                append(summary.genderLabel)
                                append(" · ")
                                append(summary.totalNames)
                                append(" ")
                                append(prenomLabel)
                                if (summary.allListsValidated) {
                                    append(" ${stringResource(R.string.label_lists_validated)}")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textLo,
                        )
                    }
                    Text(
                        "🗑",
                        fontSize = 18.sp,
                        color = c.danger,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable(onClick = onDelete),
                    )
                }
                Spacer(Modifier.height(12.dp))

                when (summary.battleStatus) {
                    BattleStatus.NOT_STARTED -> {
                        PrimaryButton(
                            stringResource(R.string.btn_start_battle),
                            onClick = onStartBattle,
                            enabled = summary.canStartBattle,
                        )
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(
                            stringResource(R.string.btn_manage_lists),
                            onClick = onManage,
                        )
                    }
                    BattleStatus.IN_PROGRESS -> {
                        PrimaryButton(
                            stringResource(R.string.btn_resume_battle),
                            onClick = onResumeBattle,
                        )
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(
                            stringResource(R.string.btn_restart_battle),
                            onClick = onRestartBattle,
                        )
                    }
                    BattleStatus.COMPLETED -> {
                        PrimaryButton(
                            stringResource(R.string.btn_view_results),
                            onClick = onViewResults,
                        )
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(
                            text = stringResource(
                                if (isExporting) {
                                    R.string.btn_export_pdf_generating
                                } else {
                                    R.string.btn_export_pdf
                                }
                            ),
                            onClick = onExportPdf,
                        )
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(
                            stringResource(R.string.btn_restart_battle),
                            onClick = onRestartBattle,
                        )
                    }
                }
            }
        }
    }
}
