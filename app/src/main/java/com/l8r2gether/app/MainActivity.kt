package com.l8r2gether.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.l8r2gether.app.ui.AppNavHost
import com.l8r2gether.app.ui.theme.LtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as LtApplication
        setContent {
            LtTheme {
                AppNavHost(sessionLibraryRepository = app.sessionLibraryRepository)
            }
        }
    }
}
