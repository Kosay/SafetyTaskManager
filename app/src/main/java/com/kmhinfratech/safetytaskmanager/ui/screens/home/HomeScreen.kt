package com.kmhinfratech.safetytaskmanager.ui.screens.home


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.kmhinfratech.safetytaskofficer.data.model.TaskUi
import com.kmhinfratech.safetytaskofficer.ui.state.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenTask: (String) -> Unit,
    onLogout: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Safety Tasks") },
                actions = {
                    TextButton(onClick = {
                        vm.logout()
                        onLogout()
                    }) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            is UiState.Error -> Column(Modifier.padding(padding).padding(16.dp)) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.load() }) { Text("Retry") }
            }
            is UiState.Success -> {
                val data = s.data
                var tab by remember { mutableStateOf(0) }
                Column(Modifier.padding(padding)) {
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Pending") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("In-Progress") })
                        Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Submitted") })
                    }
                    val list = when (tab) {
                        0 -> data.pending
                        1 -> data.inProgress
                        else -> data.submitted
                    }
                    TaskList(list = list, onOpenTask = onOpenTask)
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun TaskList(
    list: List<TaskUi>,
    onOpenTask: (String) -> Unit
) {
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            Text("No tasks here.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list, key = { it.task.id }) { item ->
            TaskRow(item, onClick = { onOpenTask(item.task.id) })
        }
    }
}

@Composable
private fun TaskRow(item: TaskUi, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(14.dp)) {
            Text(item.task.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Project: ${item.projectName.ifBlank { item.task.projectId }}")
            Spacer(Modifier.height(4.dp))
            Text("Due: ${formatTs(item.task.date)}")
        }
    }
}

private fun formatTs(ts: Timestamp?): String {
    if (ts == null) return "-"
    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return df.format(ts.toDate())
}
