package com.telen.namebattle

import android.app.Application
import com.telen.namebattle.data.remote.MeaningRemoteDataSource
import com.telen.namebattle.di.databaseModule
import com.telen.namebattle.di.networkModule
import com.telen.namebattle.di.repositoryModule
import com.telen.namebattle.di.useCaseModule
import com.telen.namebattle.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import timber.log.Timber

class NameBattleApp : Application(), KoinComponent {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val meaningDataSource: MeaningRemoteDataSource by inject()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin {
            androidLogger()
            androidContext(this@NameBattleApp)
            modules(
                databaseModule,
                networkModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }
        appScope.launch { meaningDataSource.sync() }
    }
}
