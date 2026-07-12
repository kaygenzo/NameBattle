package com.telen.namebattle.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        crashlytics.log("${priorityLabel(priority)}/$tag: $message")
        if (t != null) crashlytics.recordException(t)
    }

    private fun priorityLabel(priority: Int) = when (priority) {
        Log.WARN -> "W"
        Log.ERROR -> "E"
        else -> "I"
    }
}
