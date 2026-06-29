package com.telen.namebattle.di

import com.telen.namebattle.data.local.AppPreferences
import com.telen.namebattle.data.local.DatabaseSeeder
import com.telen.namebattle.data.local.NameBattleDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { NameBattleDatabase.create(context = androidContext()) }
    single { get<NameBattleDatabase>().firstNameDao() }
    single { get<NameBattleDatabase>().sessionDao() }
    single { AppPreferences(context = androidContext()) }
    single { DatabaseSeeder(context = androidContext(), firstNameDao = get(), prefs = get()) }
}
