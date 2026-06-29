package com.telen.namebattle.di

import com.telen.namebattle.presentation.auth.AuthViewModel
import com.telen.namebattle.presentation.battle.BattleViewModel
import com.telen.namebattle.presentation.home.HomeViewModel
import com.telen.namebattle.presentation.launch.LaunchViewModel
import com.telen.namebattle.presentation.results.ResultsViewModel
import com.telen.namebattle.presentation.search.SearchViewModel
import com.telen.namebattle.presentation.setup.SetupViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            getAllSessions = get(),
            deleteSession = get(),
            getShortlistIds = get(),
            getBattleState = get(),
            clearBattleState = get(),
            seeder = get(),
        )
    }

    viewModel { SetupViewModel(createSession = get()) }

    viewModel { (sessionId: Long) ->
        AuthViewModel(
            sessionId = sessionId,
            getSession = get(),
            getParent = get(),
            getShortlistIds = get(),
            authenticate = get(),
        )
    }

    viewModel { (sessionId: Long, parentIndex: Int) ->
        SearchViewModel(
            sessionId = sessionId,
            parentIndex = parentIndex,
            getSession = get(),
            getParent = get(),
            getShortlistIdsFlow = get(),
            searchNames = get(),
            getTopNames = get(),
            searchFreeText = get(),
            addName = get(),
            removeName = get(),
            validateList = get(),
            getNamesByIds = get(),
            getNameDetail = get(),
            getNamesWithMeaning = get(),
            addCustomName = get(),
        )
    }

    viewModel { (sessionId: Long) ->
        LaunchViewModel(
            sessionId = sessionId,
            getSession = get(),
            getShortlistIds = get(),
            startBattle = get(),
        )
    }

    viewModel { (sessionId: Long) ->
        BattleViewModel(
            sessionId = sessionId,
            getBattleState = get(),
            chooseWinner = get(),
            getNamesByIds = get(),
        )
    }

    viewModel { (sessionId: Long) ->
        ResultsViewModel(
            sessionId = sessionId,
            getSession = get(),
            getBattleState = get(),
            getNamesByIds = get(),
        )
    }
}
