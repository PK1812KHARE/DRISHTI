package com.example.assistantapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.assistantapp.ui.theme.DrishtiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrishtiTheme {
                val navController = rememberNavController()
                Navigation(navController = navController)
            }
        }
    }
}
