package com.kmhinfratech.safetytaskmanager.ui.screens.splash


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashRoute(
    onGoLogin: () -> Unit,
    onGoHome: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    when (vm.destination) {
        SplashDestination.Home -> onGoHome()
        SplashDestination.Login -> onGoLogin()
        null -> { /* render nothing / simple splash UI if you want */ }
    }
}
