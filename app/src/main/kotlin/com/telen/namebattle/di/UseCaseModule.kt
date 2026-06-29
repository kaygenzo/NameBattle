package com.telen.namebattle.di

import com.telen.namebattle.domain.usecase.auth.AuthenticateParentUseCase
import com.telen.namebattle.domain.usecase.battle.ChooseWinnerUseCase
import com.telen.namebattle.domain.usecase.battle.ClearBattleStateUseCase
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.battle.StartBattleUseCase
import com.telen.namebattle.domain.usecase.firstname.AddCustomNameToShortlistUseCase
import com.telen.namebattle.domain.usecase.firstname.AddNameToShortlistUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNameDetailUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesWithMeaningUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsFlowUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.firstname.GetTopNamesUseCase
import com.telen.namebattle.domain.usecase.firstname.RemoveNameFromShortlistUseCase
import com.telen.namebattle.domain.usecase.firstname.SearchFreeTextUseCase
import com.telen.namebattle.domain.usecase.firstname.SearchNamesUseCase
import com.telen.namebattle.domain.usecase.firstname.ValidateParentListUseCase
import com.telen.namebattle.domain.usecase.session.CreateSessionUseCase
import com.telen.namebattle.domain.usecase.session.DeleteSessionUseCase
import com.telen.namebattle.domain.usecase.session.GetAllSessionsUseCase
import com.telen.namebattle.domain.usecase.session.GetParentUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Session
    factory { CreateSessionUseCase(repo = get()) }
    factory { GetSessionUseCase(repo = get()) }
    factory { GetAllSessionsUseCase(repo = get()) }
    factory { DeleteSessionUseCase(repo = get()) }
    factory { GetParentUseCase(repo = get()) }

    // Auth
    factory { AuthenticateParentUseCase(repo = get()) }

    // First name search
    factory { SearchNamesUseCase(repo = get()) }
    factory { SearchFreeTextUseCase(repo = get()) }
    factory { GetTopNamesUseCase(repo = get()) }
    factory { GetNamesByIdsUseCase(repo = get()) }
    factory { GetNameDetailUseCase(repo = get()) }
    factory { GetNamesWithMeaningUseCase(repo = get()) }

    // Shortlist
    factory { GetShortlistIdsUseCase(repo = get()) }
    factory { GetShortlistIdsFlowUseCase(repo = get()) }
    factory { AddNameToShortlistUseCase(repo = get()) }
    factory {
        AddCustomNameToShortlistUseCase(firstNameRepository = get(), sessionRepository = get())
    }
    factory { RemoveNameFromShortlistUseCase(repo = get()) }
    factory { ValidateParentListUseCase(repo = get()) }

    // Battle
    factory { StartBattleUseCase(sessionRepo = get(), battleRepo = get()) }
    factory { ChooseWinnerUseCase(battleRepo = get()) }
    factory { GetBattleStateUseCase(battleRepo = get()) }
    factory { ClearBattleStateUseCase(repo = get()) }
}
