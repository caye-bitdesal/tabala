package com.bitdesal.tabala

import android.app.Application
import com.bitdesal.tabala.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TabalaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TabalaApplication)
            modules(appModule)
        }
    }
}
