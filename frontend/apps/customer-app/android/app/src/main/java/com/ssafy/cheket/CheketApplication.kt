package com.ssafy.cheket

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
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

        // Coil ImageLoader: 메모리 100MB + 디스크 250MB + asset URI 지원
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25) // 앱 가용 메모리의 25%
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(250L * 1024 * 1024) // 250MB
                        .build()
                }
                .components { add(AssetImageFetcher.Factory()) }
                .crossfade(true)
                .build()
        )

        appContainer = RealAppContainer(authDataStore)
        Log.d(TAG, "onCreate() — using ${appContainer::class.simpleName}")
    }

    companion object {
        private const val TAG = "CheketApplication"
    }
}
