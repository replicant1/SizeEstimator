package com.example.sizeestimator

import android.app.Application
import timber.log.Timber

class SizeEstimatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}