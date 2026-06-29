package com.telen.namebattle.di

import com.telen.namebattle.data.remote.MeaningRemoteDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { MeaningRemoteDataSource(context = androidContext()) }
}
