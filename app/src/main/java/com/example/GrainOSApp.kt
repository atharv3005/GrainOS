package com.example

import android.app.Application
import android.content.Context

class GrainOSApp : Application() {
    override fun onCreate() {
        val prefs = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            prefs.edit().putString("last_crash", stackTrace).commit()
            android.util.Log.e("CRASH_LOG", "Uncaught exception", throwable)
            kotlin.system.exitProcess(1)
        }
        super.onCreate()
    }
}
