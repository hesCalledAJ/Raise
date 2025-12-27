package com.alijafari.raise.core

import android.R
import android.app.Application
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.alijafari.raise.MainActivity
import com.alijafari.raise.feature_exceptions.CrashActivity
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class Raise : Application() {
    override fun onCreate() {
        super.onCreate()

        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
            .enabled(true)
            .showErrorDetails(true)
            .showRestartButton(false)
            .logErrorOnRestart(false)
            .trackActivities(true)
            .minTimeBetweenCrashesMs(2000)
            .restartActivity(MainActivity::class.java)
            .errorActivity(CrashActivity::class.java)
            .apply()
    }
}