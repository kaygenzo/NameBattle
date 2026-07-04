package com.telen.namebattle.di

import com.telen.namebattle.data.remote.FirebaseMeaningConfigGateway
import com.telen.namebattle.data.remote.FirebaseMeaningStorageGateway
import com.telen.namebattle.data.remote.MeaningRemoteDataSource
import org.koin.dsl.module

val networkModule = module {
    single<com.telen.namebattle.data.remote.MeaningConfigGateway> { FirebaseMeaningConfigGateway() }
    single<com.telen.namebattle.data.remote.MeaningStorageGateway> { FirebaseMeaningStorageGateway() }
    single {
        MeaningRemoteDataSource(
            prefs = get(),
            configGateway = get(),
            storageGateway = get(),
            dao = get(),
        )
    }
}
