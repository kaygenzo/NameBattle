package com.telen.namebattle.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.telen.namebattle.presentation.theme.NameBattleTheme

// ── Buttons ──────────────────────────────────────────────────────────────────

@Preview(name = "Buttons – Dark", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun ButtonsPreviewDark() {
    NameBattleTheme {
        Column(Modifier.padding(16.dp)) {
            PrimaryButton("⚔️  Lancer la bataille", onClick = {})
            NbDivider()
            SecondaryButton("Gérer les listes", onClick = {})
            NbDivider()
            DangerOutlineButton("🗑  Supprimer", onClick = {})
            NbDivider()
            PrimaryButton("Désactivé", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "Buttons – Light", showBackground = true, backgroundColor = 0xFFF5F5FA)
@Composable
private fun ButtonsPreviewLight() {
    NameBattleTheme {
        Column(Modifier.padding(16.dp)) {
            PrimaryButton("⚔️  Lancer la bataille", onClick = {})
            NbDivider()
            SecondaryButton("Gérer les listes", onClick = {})
            NbDivider()
            DangerOutlineButton("🗑  Supprimer", onClick = {})
        }
    }
}

// ── Pill ─────────────────────────────────────────────────────────────────────

@Preview(name = "Pills – Dark", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PillsPreviewDark() {
    NameBattleTheme {
        Column(Modifier.padding(16.dp)) {
            Pill("Actif", tone = PillTone.ACCENT)
            NbDivider()
            Pill("Round 3", tone = PillTone.WARNING)
            NbDivider()
            Pill("Qualifié", tone = PillTone.SUCCESS)
            NbDivider()
            Pill("Parent 2", tone = PillTone.PRO)
        }
    }
}

// ── NbTopBar ─────────────────────────────────────────────────────────────────

@Preview(name = "NbTopBar – with back", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun NbTopBarPreviewDark() {
    NameBattleTheme {
        NbTopBar(title = "Connexion", onBack = {})
    }
}

@Preview(name = "NbTopBar – no back", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun NbTopBarNoBackPreview() {
    NameBattleTheme {
        NbTopBar(title = "PrénomBattle")
    }
}

// ── NbTextField ──────────────────────────────────────────────────────────────

@Preview(name = "NbTextField – Dark", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun NbTextFieldPreviewDark() {
    NameBattleTheme {
        Column(Modifier.padding(16.dp)) {
            NbTextField("Sofia", onValueChange = {}, label = "Prénom")
            NbDivider()
            NbTextField("", onValueChange = {}, label = "Mot de passe", isPassword = true)
            NbDivider()
            NbTextField("abc", onValueChange = {}, label = "Confirmer", isPassword = true, isError = true)
        }
    }
}

// ── SegmentedTabs ────────────────────────────────────────────────────────────

@Preview(name = "SegmentedTabs – Dark", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun SegmentedTabsPreviewDark() {
    NameBattleTheme {
        Column(Modifier.padding(16.dp)) {
            SegmentedTabs(
                options = listOf("A→Z", "Top 100", "Libre"),
                selectedIndex = 0,
                onSelect = {}
            )
        }
    }
}

// ── StatTile ─────────────────────────────────────────────────────────────────

@Preview(name = "StatTile – Dark", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun StatTilePreviewDark() {
    NameBattleTheme {
        val c = com.telen.namebattle.presentation.theme.NbTheme.colors
        Column(Modifier.padding(16.dp)) {
            StatTile(
                label = "Sofia",
                value = "8",
                accent = c.accent,
                bg = c.accentBg,
                border = c.accentBorder,
                sub = "prénoms"
            )
        }
    }
}

// ── ConfirmDialog ─────────────────────────────────────────────────────────────

@Preview(name = "ConfirmDialog – Dark", showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun ConfirmDialogPreviewDark() {
    NameBattleTheme {
        ConfirmDialog(
            title = "Supprimer la session ?",
            message = "La session et toutes les listes seront définitivement effacées.",
            confirmLabel = "Tout effacer",
            dismissLabel = "Annuler",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
