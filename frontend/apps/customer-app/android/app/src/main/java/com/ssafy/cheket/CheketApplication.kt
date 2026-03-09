package com.ssafy.cheket

import android.app.Application
import com.ssafy.cheket.core.network.AuthDataStore
import com.ssafy.cheket.core.network.EncryptedSharedPrefManager
import com.ssafy.cheket.core.network.RetrofitClient

class CheketApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    lateinit var authDataStore: AuthDataStore
        private set

    override fun onCreate() {
        super.onCreate()

        val secureStorage = EncryptedSharedPrefManager(this)
        authDataStore = AuthDataStore(secureStorage)
        RetrofitClient.init(authDataStore)

        // Mock API 사용 (서버 준비되면 RealAppContainer()로 변경)
        appContainer = FakeAppContainer()
    }
}
