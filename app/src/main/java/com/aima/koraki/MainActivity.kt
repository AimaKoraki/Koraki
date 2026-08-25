package com.aima.koraki

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.aima.koraki.navigation.KorakiNavHost
import com.aima.koraki.ui.login.LoginViewModel
import com.aima.koraki.ui.theme.KorakiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val loginVm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold splash screen until DataStore auth preferences are loaded
        splashScreen.setKeepOnScreenCondition {
            loginVm.uiState.value.isLoading
        }

        enableEdgeToEdge()
        setContent {
            KorakiTheme {
                KorakiNavHost(loginVm = loginVm)
            }
        }
    }
}