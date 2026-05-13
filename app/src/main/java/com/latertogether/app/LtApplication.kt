package com.latertogether.app

import android.app.Application
import com.latertogether.app.di.AppContainer

class LtApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
