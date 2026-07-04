package com.telen.namebattle.presentation.search

import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.model.Session
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
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getSession = mockk<GetSessionUseCase>()
    private val getParent = mockk<GetParentUseCase>()
    private val getShortlistIdsFlow = mockk<GetShortlistIdsFlowUseCase>()
    private val searchNames = mockk<SearchNamesUseCase>()
    private val getTopNames = mockk<GetTopNamesUseCase>()
    private val searchFreeText = mockk<SearchFreeTextUseCase>()
    private val addName = mockk<AddNameToShortlistUseCase>()
    private val removeName = mockk<RemoveNameFromShortlistUseCase>()
    private val validateList = mockk<ValidateParentListUseCase>()
    private val getNamesByIds = mockk<GetNamesByIdsUseCase>()
    private val getNameDetail = mockk<GetNameDetailUseCase>()
    private val getNamesWithMeaning = mockk<GetNamesWithMeaningUseCase>()
    private val addCustomName = mockk<AddCustomNameToShortlistUseCase>()

    private val sessionId = 1L
    private val parentIndex = 0
    private val parentId = 10L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun parent() = Parent(
        id = parentId, sessionId = sessionId, name = "Alice", passwordHash = "hash", parentIndex = 0
    )

    private fun session() = Session(
        id = sessionId, gender = Gender.BOY, parent1 = parent(), parent2 = null
    )

    private fun firstName(id: Long, name: String) = FirstName(
        id = id, name = name, gender = Gender.BOY,
        birthsSince1900 = 0, birthsSince1980 = 0, birthsSince2000 = 0, birthsSince2010 = 0
    )

    // Sets up all mandatory mocks for SearchViewModel init block
    private fun setupDefaults() {
        coEvery { getParent(sessionId, parentIndex) } returns parent()
        coEvery { getSession(sessionId) } returns session()
        coEvery { getNamesWithMeaning() } returns emptySet()
        coEvery { getNamesByIds(any()) } returns emptyList()
        every { getShortlistIdsFlow(parentId) } returns flowOf(emptyList())
        every { searchNames(any(), any()) } returns flowOf(emptyList())
        every { getTopNames(any(), any()) } returns flowOf(emptyList())
        every { searchFreeText(any(), any()) } returns flowOf(emptyList())
    }

    private fun makeViewModel() = SearchViewModel(
        sessionId, parentIndex, getSession, getParent, getShortlistIdsFlow,
        searchNames, getTopNames, searchFreeText, addName, removeName,
        validateList, getNamesByIds, getNameDetail, getNamesWithMeaning, addCustomName
    )

    @Test
    fun `load populates parentName from parent`() = runTest {
        // given
        setupDefaults()

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        assertEquals("Alice", vm.state.value.parentName)
    }

    @Test
    fun `load sets hasParent2 true when session has two parents`() = runTest {
        // given
        setupDefaults()
        val p2 = Parent(id = 11L, sessionId = sessionId, name = "Bob", passwordHash = "hash2", parentIndex = 1)
        coEvery { getSession(sessionId) } returns session().copy(parent2 = p2)
        every { getShortlistIdsFlow(11L) } returns flowOf(emptyList())

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        assertTrue(vm.state.value.hasParent2)
    }

    @Test
    fun `onTabChange updates tab and resets query`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()
        vm.onQueryChange("test query")

        // when
        vm.onTabChange(SearchTab.TOP)

        // then
        assertEquals(SearchTab.TOP, vm.state.value.tab)
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun `onPaneChange updates pane`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.onPaneChange(SearchPane.MY_LIST)

        // then
        assertEquals(SearchPane.MY_LIST, vm.state.value.pane)
    }

    @Test
    fun `onQueryChange updates query state`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.onQueryChange("emma")

        // then
        assertEquals("emma", vm.state.value.query)
    }

    @Test
    fun `onLetterChange updates selected letter and resets query`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()
        vm.onQueryChange("some text")

        // when
        vm.onLetterChange('M')

        // then
        assertEquals('M', vm.state.value.selectedLetter)
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun `onTopYearChange updates topYear`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.onTopYearChange(2000)

        // then
        assertEquals(2000, vm.state.value.topYear)
    }

    @Test
    fun `add delegates to addName use case`() = runTest {
        // given
        setupDefaults()
        coJustRun { addName(any(), any()) }
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.add(99L)
        advanceUntilIdle()

        // then
        coVerify { addName(parentId, 99L) }
    }

    @Test
    fun `remove delegates to removeName use case`() = runTest {
        // given
        setupDefaults()
        coJustRun { removeName(any(), any()) }
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.remove(55L)
        advanceUntilIdle()

        // then
        coVerify { removeName(parentId, 55L) }
    }

    @Test
    fun `addFree does nothing when name is blank`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.addFree("   ")
        advanceUntilIdle()

        // then
        coVerify(exactly = 0) { addCustomName(any(), any(), any()) }
    }

    @Test
    fun `addFree adds custom name and resets query`() = runTest {
        // given
        setupDefaults()
        coJustRun { addCustomName(any(), any(), any()) }
        val vm = makeViewModel()
        advanceUntilIdle()
        vm.onQueryChange("Mahault")

        // when
        vm.addFree("Mahault")
        advanceUntilIdle()

        // then
        coVerify { addCustomName(parentId, "Mahault", Gender.BOY) }
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun `openDetail sets detail in state`() = runTest {
        // given
        setupDefaults()
        val detail = firstName(42L, "Emma")
        coEvery { getNameDetail(42L) } returns detail
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.openDetail(42L)
        advanceUntilIdle()

        // then
        assertNotNull(vm.state.value.detail)
        assertEquals("Emma", vm.state.value.detail?.name)
    }

    @Test
    fun `closeDetail sets detail to null`() = runTest {
        // given
        setupDefaults()
        coEvery { getNameDetail(any()) } returns firstName(1L, "Emma")
        val vm = makeViewModel()
        advanceUntilIdle()
        vm.openDetail(1L)
        advanceUntilIdle()

        // when
        vm.closeDetail()

        // then
        assertNull(vm.state.value.detail)
    }

    @Test
    fun `validate emits ListValidated event`() = runTest {
        // given
        setupDefaults()
        coJustRun { validateList(any()) }
        val vm = makeViewModel()
        advanceUntilIdle()
        val events = mutableListOf<SearchUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.validate()
        advanceUntilIdle()

        // then
        assertEquals(SearchUiEvent.ListValidated, events.firstOrNull())
        job.cancel()
    }

    @Test
    fun `validate is idempotent when already validating`() = runTest {
        // given
        setupDefaults()
        coJustRun { validateList(any()) }
        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.validate()
        vm.validate() // second call while isValidating=true should be ignored
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) { validateList(parentId) }
    }

    @Test
    fun `subtitle reflects shortlist count`() = runTest {
        // given
        setupDefaults()
        val vm = makeViewModel()
        advanceUntilIdle()

        // then — default state: 0 prénoms, subtitle starts with parent name
        assertTrue(vm.state.value.subtitle.startsWith("Alice"))
    }
}
