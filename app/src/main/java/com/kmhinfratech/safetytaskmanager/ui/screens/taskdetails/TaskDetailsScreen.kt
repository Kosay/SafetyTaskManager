package com.kmhinfratech.safetytaskmanager.ui.screens.taskdetails



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kmhinfratech.safetytaskmanager.data.model.TaskStatus
import com.kmhinfratech.safetytaskmanager.ui.state.UiState

@Composable
fun TaskDetailsScreen(
    taskId: String,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    vm: TaskDetailsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(taskId) { vm.load(taskId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.padding(padding).padding(16.dp)) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Column(Modifier.padding(padding).padding(16.dp)) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.load(taskId) }) { Text("Retry") }
            }
            is UiState.Success -> {
                val ui = s.data
                val t = ui.task

                Column(
                    modifier = Modifier.padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(t.title, style = MaterialTheme.typography.headlineSmall)
                    Text("Project: ${ui.projectName}")
                    Text("Risk: ${t.riskCategory}")
                    Text("Location: ${t.location}")
                    Text("Due: ${t.date?.toDate() ?: "-"}")
                    Divider()
                    Text("Description", style = MaterialTheme.typography.titleMedium)
                    Text(t.description)

                    Spacer(Modifier.height(10.dp))

                    when (t.status) {
                        TaskStatus.Pending -> Button(
                            onClick = { vm.startProgress(taskId) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Start Progress") }

                        TaskStatus.InProgress -> Button(
                            onClick = onSubmit,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Submit for Approval") }

                        TaskStatus.Submitted -> {
                            AssistChip(onClick = {}, label = { Text("Already Submitted") })
                        }

                        TaskStatus.Completed -> {
                            AssistChip(onClick = {}, label = { Text("Completed") })
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
