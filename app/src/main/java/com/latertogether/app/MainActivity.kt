package com.latertogether.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.latertogether.app.ui.AppNavHost
import com.latertogether.app.ui.theme.LtTheme

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
