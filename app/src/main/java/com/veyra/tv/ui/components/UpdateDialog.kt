package com.veyra.tv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.veyra.tv.data.updater.UpdateInfo
import com.veyra.tv.data.updater.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    updateManager: UpdateManager,
    onDismiss: () -> Unit
) {
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        title = { Text("Update Available: ${updateInfo.versionName}") },
        text = {
            Column {
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = "A new version is available!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Changelog:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = updateInfo.changelog,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(
                    onClick = {
                        isDownloading = true
                        scope.launch {
                            updateManager.downloadAndInstall(updateInfo.apkUrl) { p ->
                                progress = p
                            }
                            // Don't reset isDownloading here immediately as install intent starts
                        }
                    }
                ) {
                    Text("Update Now")
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}
