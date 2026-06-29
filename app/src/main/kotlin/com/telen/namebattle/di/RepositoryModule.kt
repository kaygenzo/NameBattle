package com.telen.namebattle.di

import com.telen.namebattle.data.repository.BattleRepositoryImpl
import com.telen.namebattle.data.repository.FirstNameRepositoryImpl
import com.telen.namebattle.data.repository.SessionRepositoryImpl
import com.telen.namebattle.domain.repository.BattleRepository
import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.domain.repository.SessionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<FirstNameRepository> { FirstNameRepositoryImpl(dao = get(), meaningRemote = get()) }
    single<SessionRepository> { SessionRepositoryImpl(dao = get()) }
    single<BattleRepository> { BattleRepositoryImpl(dao = get()) }
}
