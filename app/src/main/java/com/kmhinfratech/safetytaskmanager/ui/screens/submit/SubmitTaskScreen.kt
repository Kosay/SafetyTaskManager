package com.kmhinfratech.safetytaskmanager.ui.screens.submit


import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kmhinfratech.safetytaskmanager.ui.state.UiState
import java.io.File

@Composable
fun SubmitTaskScreen(
    taskId: String,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    vm: SubmitTaskViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val submitState by vm.submitState.collectAsState()

    // Camera output URI
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { vm.addLocalPhoto(it) }
        }
    }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) onSubmitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Task") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ui.closureNotes,
                onValueChange = vm::setNotes,
                label = { Text("Closure Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Button(
                onClick = {
                    val (uri, _) = createTempImageUri(ctx)
                    pendingCameraUri = uri
                    takePictureLauncher.launch(uri)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.isUploading
            ) {
                Text("Upload Photo Evidence (Camera)")
            }

            if (ui.localUris.isNotEmpty()) {
                Text("Photos", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ui.localUris) { uri ->
                        Card {
                            Column(Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Evidence",
                                    modifier = Modifier.size(90.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Remove",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable { vm.removeLocalPhoto(uri) }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { vm.submit(taskId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = submitState !is UiState.Loading && !ui.isUploading
            ) {
                if (submitState is UiState.Loading || ui.isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Submit")
            }

            if (submitState is UiState.Error) {
                Text((submitState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            AssistChip(
                onClick = {},
                label = { Text("Offline note: status updates sync offline; photo uploads need internet.") }
            )
        }
    }
}

private fun createTempImageUri(context: Context): Pair<Uri, File> {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "evidence_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    return uri to file
}
