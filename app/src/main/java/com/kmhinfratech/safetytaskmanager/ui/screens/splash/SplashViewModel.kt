package com.kmhinfratech.safetytaskmanager.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.kmhinfratech.safetytaskofficer.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class SplashDestination { Login, Home }

@HiltViewModel
class SplashViewModel @Inject constructor(
    authRepo: AuthRepository
) : ViewModel() {
    val destination: SplashDestination? =
        if (authRepo.isLoggedIn()) SplashDestination.Home else SplashDestination.Login
}
