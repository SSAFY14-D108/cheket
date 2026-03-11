package com.ssafy.cheket

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ssafy.cheket.ui.theme.CheketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        enableEdgeToEdge()
        val app = application as CheketApplication
        val appContainer = app.appContainer
        val isAlreadyLoggedIn = app.authDataStore.isLoggedIn()
        Log.d(TAG, "onCreate() — appContainer=${appContainer::class.simpleName}, isLoggedIn=$isAlreadyLoggedIn")
        setContent {
            CheketTheme {
                AppNavGraph(
                    appContainer = appContainer,
                    startLoggedIn = isAlreadyLoggedIn,
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
