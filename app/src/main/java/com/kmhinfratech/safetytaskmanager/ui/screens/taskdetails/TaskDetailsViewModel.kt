package com.kmhinfratech.safetytaskmanager.ui.screens.taskdetails



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmhinfratech.safetytaskmanager.data.model.Task
import com.kmhinfratech.safetytaskmanager.data.model.TaskStatus
import com.kmhinfratech.safetytaskmanager.data.repo.TaskRepository
import com.kmhinfratech.safetytaskmanager.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailsUi(
    val task: Task,
    val projectName: String
)

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val repo: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TaskDetailsUi>>(UiState.Loading)
    val state: StateFlow<UiState<TaskDetailsUi>> = _state

    fun load(taskId: String) = viewModelScope.launch {
        _state.value = UiState.Loading
        try {
            val task = repo.getTask(taskId)
            val projectName = repo.getProjectName(task.projectId)
            _state.value = UiState.Success(TaskDetailsUi(task, projectName))
        } catch (t: Throwable) {
            _state.value = UiState.Error(t.message ?: "Failed to load task", t)
        }
    }

    fun startProgress(taskId: String) = viewModelScope.launch {
        // Works offline (Firestore queues updates)
        try {
            repo.setTaskStatus(taskId, TaskStatus.InProgress.raw)
            load(taskId)
        } catch (t: Throwable) {
            _state.value = UiState.Error(t.message ?: "Failed to update status", t)
        }
    }
}
