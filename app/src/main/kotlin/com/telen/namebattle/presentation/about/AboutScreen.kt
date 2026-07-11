package com.telen.namebattle.presentation.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.telen.namebattle.BuildConfig
import com.telen.namebattle.R
import com.telen.namebattle.presentation.components.NbDivider
import com.telen.namebattle.presentation.components.NbTopBar
import com.telen.namebattle.presentation.theme.NbTheme

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val c = NbTheme.colors
    Scaffold(containerColor = c.page) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            NbTopBar(
                title = stringResource(R.string.title_about),
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                text = stringResource(R.string.label_app_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textLo,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            NbDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Text(
                text = stringResource(R.string.label_data_sources),
                style = MaterialTheme.typography.titleSmall,
                color = c.textHi,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            DataSourceEntry(
                country = stringResource(R.string.label_country_france),
                source = stringResource(R.string.label_source_insee),
            )
            NbDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Text(
                text = stringResource(R.string.label_open_source_licenses),
                style = MaterialTheme.typography.titleSmall,
                color = c.textHi,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LibrariesContainer(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DataSourceEntry(country: String, source: String) {
    val c = NbTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = country,
            style = MaterialTheme.typography.labelLarge,
            color = c.textMid,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = source,
            style = MaterialTheme.typography.bodySmall,
            color = c.textLo,
        )
    }
}
