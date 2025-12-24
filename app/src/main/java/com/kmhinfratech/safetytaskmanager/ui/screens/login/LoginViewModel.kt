package com.kmhinfratech.safetytaskmanager.ui.screens.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmhinfratech.safetytaskmanager.data.repo.AuthRepository
import com.kmhinfratech.safetytaskmanager.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            authRepo.login(email.trim(), password)
            _state.value = UiState.Success(Unit)
        } catch (t: Throwable) {
            _state.value = UiState.Error(t.message ?: "Login failed", t)
        }
    }
}
