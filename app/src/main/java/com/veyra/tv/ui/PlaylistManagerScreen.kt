package com.veyra.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.veyra.tv.model.Playlist
import com.veyra.tv.model.PlaylistKind
import com.veyra.tv.ui.utils.isTV
import com.veyra.tv.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManagerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val error by viewModel.playlistError.collectAsStateWithLifecycle()
    val isTv = isTV(LocalContext.current)
    val scope = rememberCoroutineScope()

    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Playlist?>(null) }
    var deleting by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PLAYLISTS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    var isBackFocused by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .onFocusChanged { if (isTv) isBackFocused = it.isFocused }
                            .scale(if (isBackFocused) 1.2f else 1f)
                            .background(
                                if (isBackFocused) Color.White.copy(alpha = 0.12f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF101014),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF101014)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            error?.let { message ->
                ErrorBanner(message = message) { viewModel.clearPlaylistError() }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Button(
                        onClick = {
                            editing = null
                            viewModel.clearPlaylistError()
                            showEditor = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add playlist")
                    }
                }

                items(playlists, key = { it.id }) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        onSelect = { viewModel.selectAndSyncPlaylist(playlist) },
                        onEdit = {
                            editing = playlist
                            viewModel.clearPlaylistError()
                            showEditor = true
                        },
                        onDelete = { deleting = playlist }
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        Text(
                            text = "No playlists yet. Add an M3U URL or your Xtream Codes login to get started.",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        AddEditPlaylistDialog(
            initial = editing,
            error = error,
            onDismiss = {
                showEditor = false
                editing = null
                viewModel.clearPlaylistError()
            },
            onSave = { name, kind, m3uUrl, server, username, password ->
                scope.launch {
                    val saved = viewModel.savePlaylist(
                        existing = editing,
                        name = name,
                        kind = kind,
                        m3uUrl = m3uUrl,
                        server = server,
                        username = username,
                        password = password
                    )
                    if (saved) {
                        showEditor = false
                        editing = null
                        viewModel.clearPlaylistError()
                    }
                }
            }
        )
    }

    deleting?.let { target ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete playlist?") },
            text = {
                Text("\"${target.name}\" and its channels will be removed. Favorites in other playlists are kept.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlaylist(target)
                    deleting = null
                }) { Text("Delete", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancel") }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.02f else 1f, label = "PlaylistRowScale")
    val border = if (isFocused) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0xFF2B2B2B))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        color = if (playlist.isSelected) Color(0xFF17251C) else Color(0xFF1E1E1E),
        border = border,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(if (playlist.kind == PlaylistKind.XTREAM) "Xtream Codes" else "M3U playlist")
                        append(" · ")
                        append(if (playlist.isSelected) "Selected" else "Press OK to use")
                    },
                    color = if (playlist.isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit ${playlist.name}", tint = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${playlist.name}", tint = Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFF3A1E1E), RoundedCornerShape(8.dp))
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = Color(0xFFFFB4B4),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall
        )
        TextButton(onClick = onDismiss) { Text("Dismiss", color = Color.White) }
    }
}

@Composable
private fun AddEditPlaylistDialog(
    initial: Playlist?,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, kind: PlaylistKind, m3uUrl: String, server: String, username: String, password: String) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var kind by remember(initial) { mutableStateOf(initial?.kind ?: PlaylistKind.M3U) }
    var m3uUrl by remember(initial) {
        mutableStateOf(if (initial?.kind == PlaylistKind.M3U) initial.url else "")
    }
    var server by remember(initial) { mutableStateOf(initial?.serverUrl ?: "") }
    var username by remember(initial) { mutableStateOf(initial?.username ?: "") }
    var password by remember(initial) { mutableStateOf(initial?.password ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initial == null) "Add playlist" else "Edit playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = kind == PlaylistKind.M3U,
                        onClick = { kind = PlaylistKind.M3U },
                        label = { Text("M3U / M3U8") }
                    )
                    FilterChip(
                        selected = kind == PlaylistKind.XTREAM,
                        onClick = { kind = PlaylistKind.XTREAM },
                        label = { Text("Xtream Codes") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (kind == PlaylistKind.M3U) {
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text("Playlist URL") },
                        placeholder = { Text("https://example.com/playlist.m3u") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = { Text("Server URL") },
                        placeholder = { Text("http://host:8080") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Your credentials stay on this device and are only used to build the M3U/EPG URLs.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                error?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFFB4B4),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, kind, m3uUrl, server, username, password) }
                    ) {
                        Text(if (initial == null) "Add" else "Save")
                    }
                }
            }
        }
    }
}
