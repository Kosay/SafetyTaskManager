package com.kmhinfratech.safetytaskmanager.ui.screens.submit


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmhinfratech.safetytaskmanager.data.repo.TaskRepository
import com.kmhinfratech.safetytaskmanager.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubmitUi(
    val closureNotes: String = "",
    val localUris: List<Uri> = emptyList(),  // camera files (local)
    val uploadedUrls: List<String> = emptyList(), // storage urls
    val isUploading: Boolean = false
)

@HiltViewModel
class SubmitTaskViewModel @Inject constructor(
    private val repo: TaskRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SubmitUi())
    val ui: StateFlow<SubmitUi> = _ui

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState

    fun setNotes(v: String) { _ui.value = _ui.value.copy(closureNotes = v) }

    fun addLocalPhoto(uri: Uri) {
        _ui.value = _ui.value.copy(localUris = _ui.value.localUris + uri)
    }

    fun removeLocalPhoto(uri: Uri) {
        _ui.value = _ui.value.copy(localUris = _ui.value.localUris - uri)
    }

    fun submit(taskId: String) = viewModelScope.launch {
        val current = _ui.value
        if (current.closureNotes.isBlank()) {
            _submitState.value = UiState.Error("Closure Notes are required.")
            return@launch
        }

        _submitState.value = UiState.Loading

        try {
            // Upload images first (requires connection).
            _ui.value = current.copy(isUploading = true)
            val urls = current.localUris.map { repo.uploadEvidence(taskId, it) }

            // Update Firestore
            repo.submitTaskForApproval(taskId, current.closureNotes.trim(), urls)

            _ui.value = current.copy(uploadedUrls = urls, isUploading = false)
            _submitState.value = UiState.Success(Unit)
        } catch (t: Throwable) {
            _ui.value = _ui.value.copy(isUploading = false)
            _submitState.value = UiState.Error(
                t.message ?: "Submit failed. Check internet connection for uploads.",
                t
            )
        }
    }
}
