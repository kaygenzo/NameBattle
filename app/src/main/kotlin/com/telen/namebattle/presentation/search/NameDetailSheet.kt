package com.telen.namebattle.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.namebattle.R
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.theme.NbTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

@Composable
fun NameDetailContent(
    name: FirstName,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = NbTheme.colors
    Column(modifier
        .padding(horizontal = 18.dp)
        .padding(bottom = 26.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = c.textHi)
            name.origin?.takeIf { it.isNotBlank() }?.let { origin ->
                Spacer(Modifier.width(10.dp))
                Text(
                    origin,
                    style = MaterialTheme.typography.labelSmall,
                    color = c.pro,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(c.proBg)
                        .border(1.dp, c.proBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }

        if (!name.meaning.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(name.meaning!!, style = MaterialTheme.typography.bodyMedium, color = c.textMid)
        }

        if (name.totalBirths > 0 || name.peakYear > 0) {
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.label_popularity), style = MaterialTheme.typography.titleMedium, color = c.textMid)
            Spacer(Modifier.height(9.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatTileBox(
                    stringResource(R.string.label_peak),
                    name.peakYear.takeIf { it > 0 }?.toString() ?: "—",
                    Modifier.weight(1f)
                )
                StatTileBox(stringResource(R.string.label_births), formatThousands(name.totalBirths), Modifier.weight(1f))
                StatTileBox(
                    stringResource(R.string.label_since),
                    name.firstYear.takeIf { it > 0 }?.toString() ?: "—",
                    Modifier.weight(1f)
                )
            }

            if (name.firstYear in 1900..2024 && name.peakYear > 0) {
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.label_evolution, name.firstYear),
                    style = MaterialTheme.typography.bodySmall, color = c.textLo,
                )
                Spacer(Modifier.height(8.dp))
                Sparkline(firstYear = name.firstYear, peakYear = name.peakYear)
            }
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton(stringResource(R.string.btn_add_to_list), onClick = onAdd)
    }
}

@Composable
private fun StatTileBox(label: String, value: String, modifier: Modifier = Modifier) {
    val c = NbTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = c.bg3,
        border = BorderStroke(1.dp, c.border),
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(vertical = 11.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textHi)
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = c.textMid)
        }
    }
}

@Composable
private fun Sparkline(firstYear: Int, peakYear: Int, modifier: Modifier = Modifier) {
    val c = NbTheme.colors
    val steps = 14
    val span = (2024 - firstYear).coerceAtLeast(1)
    val spread = max(peakYear - firstYear, 2024 - peakYear).coerceAtLeast(1)

    val labelIndices = setOf(0, steps / 2, steps - 1)

    // Bar whose year is closest to the peak year
    val peakBarIndex = (0 until steps).minByOrNull { i ->
        abs((firstYear + span * i / steps) - peakYear)
    } ?: 0

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (i in 0 until steps) {
                val year = firstYear + span * i / steps
                val dist = abs(year - peakYear)
                val h = max(5.0, 100.0 - (dist.toDouble() / spread).pow(1.5) * 90.0)
                val isPeak = i == peakBarIndex
                Box(
                    Modifier
                        .weight(1f)
                        .height((h / 100.0 * 56).dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(if (isPeak) c.accent else c.accentBg),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Axe des abscisses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (i in 0 until steps) {
                val year = if (i == steps - 1) 2024 else firstYear + span * i / steps
                Box(Modifier.weight(1f)) {
                    if (i in labelIndices) {
                        Text(
                            year.toString(),
                            fontSize = 8.sp,
                            color = c.textLo,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private fun formatThousands(n: Int): String =
    n.toString().reversed().chunked(3).joinToString(" ").reversed()
