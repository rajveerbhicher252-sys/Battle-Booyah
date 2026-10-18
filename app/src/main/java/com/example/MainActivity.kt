package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.app.viewmodel.AppViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.user.UserMainScreen
import com.example.ui.admin.AdminMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "auth" -> AuthScreen(viewModel)
                        "user_main" -> UserMainScreen(viewModel)
                        "admin_main" -> AdminMainScreen(viewModel)
                        else -> AuthScreen(viewModel)
                    }
                }
            }
        }
    }
}
