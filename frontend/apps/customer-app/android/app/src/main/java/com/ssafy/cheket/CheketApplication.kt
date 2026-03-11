package com.ssafy.cheket

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import com.ssafy.cheket.core.network.AuthDataStore
import com.ssafy.cheket.core.network.EncryptedSharedPrefManager
import com.ssafy.cheket.core.network.RetrofitClient
import com.ssafy.cheket.core.ui.AssetImageFetcher

class CheketApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    lateinit var authDataStore: AuthDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() — initializing application")

        val secureStorage = EncryptedSharedPrefManager(this)
        authDataStore = AuthDataStore(secureStorage)
        RetrofitClient.init(authDataStore)

        // Coil: file:///android_asset/ URI 지원
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components { add(AssetImageFetcher.Factory()) }
                .build()
        )

        // Mock API 사용 (서버 준비되면 RealAppContainer()로 변경)
        appContainer = RealAppContainer(authDataStore)
        Log.d(TAG, "onCreate() — using ${appContainer::class.simpleName}")
    }

    companion object {
        private const val TAG = "CheketApplication"
    }
}
