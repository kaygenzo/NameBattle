package com.telen.namebattle.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.telen.namebattle.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.pluralStringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telen.namebattle.presentation.components.PrimaryButton
import com.telen.namebattle.presentation.components.SegmentedTabs
import com.telen.namebattle.presentation.theme.NbTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val ALPHABET = ('A'..'Z').toList()
private val TOP_YEARS = listOf(1900, 1980, 2000, 2010)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    sessionId: Long,
    parentIndex: Int,
    onListValidated: () -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel { parametersOf(sessionId, parentIndex) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            if (it is SearchUiEvent.ListValidated) onListValidated()
        }
    }

    SearchScreenContent(
        state = state,
        onTabChange = viewModel::onTabChange,
        onPaneChange = viewModel::onPaneChange,
        onQueryChange = viewModel::onQueryChange,
        onLetterChange = viewModel::onLetterChange,
        onTopYearChange = viewModel::onTopYearChange,
        onAdd = viewModel::add,
        onAddFree = viewModel::addFree,
        onRemove = viewModel::remove,
        onOpenDetail = viewModel::openDetail,
        onCloseDetail = viewModel::closeDetail,
        onValidate = viewModel::validate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreenContent(
    state: SearchUiState,
    onTabChange: (SearchTab) -> Unit = {},
    onPaneChange: (SearchPane) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onLetterChange: (Char) -> Unit = {},
    onTopYearChange: (Int) -> Unit = {},
    onAdd: (Long) -> Unit = {},
    onAddFree: (String) -> Unit = {},
    onRemove: (Long) -> Unit = {},
    onOpenDetail: (Long) -> Unit = {},
    onCloseDetail: () -> Unit = {},
    onValidate: () -> Unit = {},
) {
    val c = NbTheme.colors

    Scaffold(containerColor = c.page) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (state.pane == SearchPane.SEARCH) {
                            stringResource(R.string.label_search_pane)
                        } else {
                            stringResource(R.string.label_my_list_pane)
                        },
                        style = MaterialTheme.typography.headlineSmall, color = c.textHi,
                    )
                    Text(
                        "${state.parentName} · ${
                            pluralStringResource(
                                R.plurals.label_total_prenoms,
                                state.shortlistCount,
                                state.shortlistCount,
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textLo
                    )
                }
                TextButton(onClick = onValidate) {
                    Text(
                        stringResource(R.string.btn_finish),
                        color = c.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (state.pane == SearchPane.SEARCH) {
                SegmentedTabs(
                    options = listOf(
                        stringResource(R.string.tab_az),
                        stringResource(R.string.tab_top100),
                        stringResource(R.string.tab_free),
                    ),
                    selectedIndex = state.tab.ordinal,
                    onSelect = { onTabChange(SearchTab.entries[it]) },
                )
                Spacer(Modifier.height(12.dp))
                SearchBody(
                    state, onQueryChange, onLetterChange, onTopYearChange,
                    onAdd, onAddFree, onRemove, onOpenDetail, Modifier.weight(1f),
                )
            } else {
                MyListBody(state, onRemove, onValidate, Modifier.weight(1f))
            }

            HorizontalDivider(thickness = 1.dp, color = c.border)
            Row(Modifier.fillMaxWidth()) {
                BottomItem(
                    "☰", stringResource(R.string.label_search_pane),
                    state.pane == SearchPane.SEARCH, Modifier.weight(1f),
                ) {
                    onPaneChange(SearchPane.SEARCH)
                }
                BottomItem(
                    "♥", stringResource(R.string.label_my_list_pane),
                    state.pane == SearchPane.MY_LIST, Modifier.weight(1f),
                ) {
                    onPaneChange(SearchPane.MY_LIST)
                }
            }
        }
    }

    state.detail?.let { detail ->
        ModalBottomSheet(
            onDismissRequest = onCloseDetail,
            sheetState = rememberModalBottomSheetState(),
            containerColor = c.bg,
        ) {
            NameDetailContent(
                name = detail,
                onAdd = { onAdd(detail.id); onCloseDetail() },
            )
        }
    }
}

@Composable
private fun SearchBody(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onLetterChange: (Char) -> Unit,
    onTopYearChange: (Int) -> Unit,
    onAdd: (Long) -> Unit,
    onAddFree: (String) -> Unit,
    onRemove: (Long) -> Unit,
    onOpenDetail: (Long) -> Unit,
    modifier: Modifier,
) {
    val c = NbTheme.colors
    when (state.tab) {
        SearchTab.AZ -> Column(modifier) {
            NbInput(state.query, onQueryChange, stringResource(R.string.placeholder_filter))
            Spacer(Modifier.height(8.dp))
            LetterChips(state, onLetterChange)
            NameList(state.results, onAdd, onRemove, onOpenDetail, modifier = Modifier.weight(1f))
        }

        SearchTab.TOP -> Column(modifier) {
            YearSelector(state, onTopYearChange)
            NameList(
                state.results, onAdd, onRemove, onOpenDetail,
                ranked = true, modifier = Modifier.weight(1f),
            )
        }

        SearchTab.FREE -> Column(modifier) {
            Text(
                stringResource(R.string.label_enter_free_name),
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid
            )
            Spacer(Modifier.height(6.dp))
            NbInput(state.query, onQueryChange, stringResource(R.string.placeholder_free_name))
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                stringResource(R.string.btn_add_to_list),
                onClick = { onAddFree(state.query) },
            )
            Spacer(Modifier.height(12.dp))
            if (state.results.isNotEmpty()) {
                Text(
                    stringResource(R.string.label_insee_suggestions),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textMid
                )
                Spacer(Modifier.height(6.dp))
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(state.results, key = { it.id }) { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onQueryChange(r.name) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                r.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textHi,
                                modifier = Modifier.weight(1f)
                            )
                            if (r.inList) Text("✓", color = c.accent)
                        }
                        HorizontalDivider(thickness = 1.dp, color = c.border)
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            Text(
                stringResource(R.string.label_free_spelling_note),
                style = MaterialTheme.typography.bodySmall, color = c.textLo,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun LetterChips(state: SearchUiState, onLetterChange: (Char) -> Unit) {
    val c = NbTheme.colors
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        items(ALPHABET) { l ->
            val on = state.selectedLetter == l && state.query.isBlank()
            Text(
                l.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (on) c.accent else c.textMid,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (on) c.accentBg else c.bg2)
                    .border(1.dp, if (on) c.accentBorder else c.border, RoundedCornerShape(8.dp))
                    .clickable { onLetterChange(l) }
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun YearSelector(state: SearchUiState, onTopYearChange: (Int) -> Unit) {
    val c = NbTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.label_since),
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid,
        )
        TOP_YEARS.forEach { y ->
            val on = state.topYear == y
            Text(
                y.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (on) c.accent else c.textMid,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (on) c.accentBg else c.bg2)
                    .border(1.dp, if (on) c.accentBorder else c.border, RoundedCornerShape(8.dp))
                    .clickable { onTopYearChange(y) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun NameList(
    rows: List<NameRow>,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onOpenDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    ranked: Boolean = false,
) {
    val c = NbTheme.colors
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(rows, key = if (ranked) null else ({ _, r -> r.id })) { index, r ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .alpha(if (r.inList) 0.45f else 1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (ranked) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(c.accentBg)
                            .border(1.dp, c.accentBorder, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            color = c.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                }
                Text(
                    r.name + if (r.inList) " ✓" else "",
                    style = MaterialTheme.typography.titleMedium, color = c.textHi,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = if (r.hasMeaning) {
                        stringResource(R.string.cd_view_detail_with_meaning)
                    } else {
                        stringResource(R.string.cd_view_detail)
                    },
                    tint = if (r.hasMeaning) c.pro else c.textLo,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onOpenDetail(r.id) },
                )
                Spacer(Modifier.width(14.dp))
                if (r.inList) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.cd_remove),
                        tint = c.danger,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRemove(r.id) },
                    )
                } else {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cd_add),
                        tint = c.success,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onAdd(r.id) },
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp, color = c.border)
        }
    }
}

@Composable
private fun MyListBody(
    state: SearchUiState,
    onRemove: (Long) -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier,
) {
    val c = NbTheme.colors
    if (state.shortlist.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.label_empty_list),
                style = MaterialTheme.typography.bodySmall, color = c.textLo,
            )
        }
        return
    }
    Column(modifier) {
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(state.shortlist, key = { _, r -> r.id }) { index, r ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(c.accentBg)
                            .border(1.dp, c.accentBorder, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            color = c.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Text(
                        r.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textHi,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.cd_remove),
                        tint = c.danger,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRemove(r.id) },
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = c.border)
            }
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton(stringResource(R.string.btn_finish), onClick = onValidate)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun NbInput(value: String, onChange: (String) -> Unit, placeholder: String) {
    com.telen.namebattle.presentation.components.NbTextField(
        value = value, onValueChange = onChange, label = placeholder,
    )
}

@Composable
private fun BottomItem(
    icon: String,
    label: String,
    on: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val c = NbTheme.colors
    Column(
        modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 18.sp, color = if (on) c.accent else c.textLo)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (on) c.accent else c.textLo
        )
    }
}
