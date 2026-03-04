package com.example.cheketqr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.cheketqr.ui.theme.CheketQRTheme
import com.example.cheketqr.presentation.scanner.ScannerRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheketQRTheme {
                ScannerRoute(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
