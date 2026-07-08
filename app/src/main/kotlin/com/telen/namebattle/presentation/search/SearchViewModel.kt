package com.telen.namebattle.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.usecase.firstname.AddCustomNameToShortlistUseCase
import com.telen.namebattle.domain.usecase.firstname.AddNameToShortlistUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNameDetailUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesWithMeaningUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsFlowUseCase
import com.telen.namebattle.domain.usecase.firstname.GetTopNamesUseCase
import com.telen.namebattle.domain.usecase.firstname.RemoveNameFromShortlistUseCase
import com.telen.namebattle.domain.usecase.firstname.SearchFreeTextUseCase
import com.telen.namebattle.domain.usecase.firstname.SearchNamesUseCase
import com.telen.namebattle.domain.usecase.firstname.ValidateParentListUseCase
import com.telen.namebattle.domain.usecase.session.GetParentUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchUiEvent {
    data object ListValidated : SearchUiEvent
}

private data class Query(
    val tab: SearchTab,
    val letter: Char,
    val text: String,
    val topYear: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val sessionId: Long,
    private val parentIndex: Int,
    private val getSession: GetSessionUseCase,
    private val getParent: GetParentUseCase,
    private val getShortlistIdsFlow: GetShortlistIdsFlowUseCase,
    private val searchNames: SearchNamesUseCase,
    private val getTopNames: GetTopNamesUseCase,
    private val searchFreeText: SearchFreeTextUseCase,
    private val addName: AddNameToShortlistUseCase,
    private val removeName: RemoveNameFromShortlistUseCase,
    private val validateList: ValidateParentListUseCase,
    private val getNamesByIds: GetNamesByIdsUseCase,
    private val getNameDetail: GetNameDetailUseCase,
    private val getNamesWithMeaning: GetNamesWithMeaningUseCase,
    private val addCustomName: AddCustomNameToShortlistUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _events = Channel<SearchUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val query = MutableStateFlow(Query(SearchTab.AZ, 'A', "", 1900))
    private var parentId: Long = -1
    private var gender: Gender = Gender.BOY
    private var meaningNames: Set<String> = emptySet()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val parent = getParent(sessionId, parentIndex) ?: return@launch
            val session = getSession(sessionId)
            parentId = parent.id
            gender = session?.gender ?: Gender.BOY
            _state.update {
                it.copy(
                    isLoading = false,
                    parentName = parent.name,
                    hasParent2 = session?.parent2 != null,
                )
            }
            meaningNames = runCatching { getNamesWithMeaning() }.getOrDefault(emptySet())
            observeResults()
            observeShortlist()
        }
    }

    private fun observeResults() {
        viewModelScope.launch {
            query.flatMapLatest { q -> activeFlow(q) }
                .combine(getShortlistIdsFlow(parentId)) { names, ids ->
                    val set = ids.toSet()
                    names.map {
                        NameRow(it.id, it.name, it.id in set, it.name.uppercase() in meaningNames)
                    }
                }
                .collect { rows -> _state.update { it.copy(results = rows) } }
        }
    }

    private fun observeShortlist() {
        viewModelScope.launch {
            getShortlistIdsFlow(parentId).collect { ids ->
                val rows = getNamesByIds(ids)
                    .map { NameRow(it.id, it.name, true, it.name.uppercase() in meaningNames) }
                _state.update { s ->
                    val count = ids.size
                    val subtitle =
                        "${s.parentName} · $count ${if (count > 1) "prénoms" else "prénom"}"
                    s.copy(shortlist = rows, shortlistCount = count, subtitle = subtitle)
                }
            }
        }
    }

    private fun activeFlow(q: Query) = when (q.tab) {
        SearchTab.AZ -> searchNames(q.letter, gender).map { names ->
            if (q.text.isBlank()) names
            else names.filter { it.name.lowercase().contains(q.text.lowercase()) }
        }
        SearchTab.TOP -> getTopNames(gender, q.topYear)
        SearchTab.FREE ->
            if (q.text.length < 2) flowOf(emptyList()) else searchFreeText(q.text, gender)
    }

    fun onTabChange(tab: SearchTab) {
        _state.update { it.copy(tab = tab, query = "") }
        query.update { it.copy(tab = tab, text = "") }
    }

    fun onPaneChange(pane: SearchPane) = _state.update { it.copy(pane = pane) }

    fun onQueryChange(text: String) {
        _state.update { it.copy(query = text) }
        query.update { it.copy(text = text) }
    }

    fun onLetterChange(letter: Char) {
        _state.update { it.copy(selectedLetter = letter, query = "") }
        query.update { it.copy(letter = letter, text = "") }
    }

    fun onTopYearChange(year: Int) {
        _state.update { it.copy(topYear = year) }
        query.update { it.copy(topYear = year) }
    }

    fun add(id: Long) = viewModelScope.launch { addName(parentId, id) }

    fun addFree(name: String) {
        val text = name.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            addCustomName(parentId, text, gender)
            onQueryChange("")
        }
    }

    fun remove(id: Long) = viewModelScope.launch { removeName(parentId, id) }

    fun openDetail(id: Long) = viewModelScope.launch {
        _state.update { it.copy(detail = getNameDetail(id)) }
    }

    fun closeDetail() = _state.update { it.copy(detail = null) }

    fun validate() {
        if (_state.value.isValidating) return
        _state.update { it.copy(isValidating = true) }
        viewModelScope.launch {
            validateList(parentId)
            _events.send(SearchUiEvent.ListValidated)
        }
    }
}
