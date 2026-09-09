package com.palmlens

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PalmlensApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) enableStrictMode()
    }

    /** Surfaces accidental disk / network / decode work on the main thread during development. */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build(),
        )
    }
}
