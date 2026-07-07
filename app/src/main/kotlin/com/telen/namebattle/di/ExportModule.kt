package com.telen.namebattle.di

import com.telen.namebattle.domain.usecase.export.ExportBattleReportUseCase
import com.telen.namebattle.export.BattleReportPdfGenerator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val exportModule = module {
    single { BattleReportPdfGenerator(androidContext()) }
    factory { ExportBattleReportUseCase(get(), get(), get(), get()) }
}
