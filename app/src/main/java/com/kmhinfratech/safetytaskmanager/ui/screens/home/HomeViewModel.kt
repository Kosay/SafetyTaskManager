package com.kmhinfratech.safetytaskmanager.ui.screens.home



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmhinfratech.safetytaskmanager.data.model.TaskStatus
import com.kmhinfratech.safetytaskmanager.data.model.TaskUi
import com.kmhinfratech.safetytaskmanager.data.repo.AuthRepository
import com.kmhinfratech.safetytaskmanager.data.repo.TaskRepository
import com.kmhinfratech.safetytaskmanager.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeData(
    val pending: List<TaskUi>,
    val inProgress: List<TaskUi>,
    val submitted: List<TaskUi>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val repo: TaskRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeData>> = _state

    fun load() = viewModelScope.launch {
        val uid = authRepo.currentUserId ?: run {
            _state.value = UiState.Error("Not authenticated")
            return@launch
        }

        _state.value = UiState.Loading
        try {
            val tasks = repo.getAssignedTasksOnce(uid)
            val projectMap = repo.getProjectsByIds(tasks.map { it.projectId }.toSet())

            val ui = tasks.map { t -> TaskUi(task = t, projectName = projectMap[t.projectId]?.name ?: "") }
            val pending = ui.filter { it.task.status == TaskStatus.Pending }
            val inProg = ui.filter { it.task.status == TaskStatus.InProgress }
            val submitted = ui.filter { it.task.status == TaskStatus.Submitted }

            _state.value = UiState.Success(HomeData(pending, inProg, submitted))
        } catch (t: Throwable) {
            _state.value = UiState.Error(t.message ?: "Failed to load tasks", t)
        }
    }

    fun logout() = authRepo.logout()
}
