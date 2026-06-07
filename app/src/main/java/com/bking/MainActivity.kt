package com.bking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bking.ui.BkingApp
import com.bking.ui.theme.BkingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BkingStartup", "MainActivity onCreate")
        enableEdgeToEdge()
        setContent {
            Log.d("BkingStartup", "setContent invoked")
            BkingTheme {
                BkingApp()
            }
        }
    }
}
