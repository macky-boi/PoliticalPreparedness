package com.example.android.politicalpreparedness

import android.app.Application
import timber.log.Timber

class PoliticalPreparednessApplication: Application() {
    val container: AppContainer by lazy {
        DefaultAppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}