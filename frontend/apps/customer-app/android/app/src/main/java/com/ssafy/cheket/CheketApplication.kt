package com.ssafy.cheket

import android.app.Application

class CheketApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = RealAppContainer()
    }
}
