package com.ssafy.cheket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ssafy.cheket.ui.theme.CheketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as CheketApplication).appContainer
        setContent {
            CheketTheme {
                AppNavGraph(appContainer = appContainer)
            }
        }
    }
}
