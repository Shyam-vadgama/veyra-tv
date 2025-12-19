package com.example.iptv_player.ui

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    url: String,
    channelName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val activity = context as? Activity

    // Track Selector for Quality
    val trackSelector = remember { DefaultTrackSelector(context) }
    
    // Player state
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var currentQuality by remember { mutableStateOf("Auto") }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isLoading = playbackState == Player.STATE_BUFFERING
                        if (playbackState == Player.STATE_READY) {
                            hasError = false
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        hasError = true
                        isLoading = false
                    }
                })
            }
    }

    // Auto-hide controls
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Manage system UI visibility based on orientation
    LaunchedEffect(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
             activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
             activity?.window?.decorView?.systemUiVisibility = (
                 android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                 or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                 or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
             )
        } else {
             activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
             activity?.window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    // Clean up player
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                exoPlayer.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
            
            // Restore UI flags when exiting
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity?.window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(url) {
        isLoading = true
        hasError = false
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isControlsVisible = !isControlsVisible }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false // Use our custom controls
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER) // We use our own
                    isFocusable = true
                    keepScreenOn = true
                }
            }
        )

        // Custom Overlay Controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = channelName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { showQualityDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Quality", tint = Color.White)
                    }
                }
            }
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Error Message
        if (hasError) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error playing video", color = Color.White)
                Button(onClick = {
                    exoPlayer.prepare()
                    exoPlayer.play()
                }) {
                    Text("Retry")
                }
            }
        }
        
        // Quality Selection Dialog
        if (showQualityDialog) {
            QualitySelectionDialog(
                trackSelector = trackSelector,
                onDismiss = { showQualityDialog = false },
                currentQuality = currentQuality,
                onQualitySelected = { qualityName ->
                    currentQuality = qualityName
                    showQualityDialog = false
                }
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun QualitySelectionDialog(
    trackSelector: DefaultTrackSelector,
    onDismiss: () -> Unit,
    currentQuality: String,
    onQualitySelected: (String) -> Unit
) {
    val context = LocalContext.current
    // Get available video tracks
    val tracks = remember {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        val videoGroups = mutableListOf<Pair<String, TrackSelectionOverride?>>()
        
        // Option for Auto
        videoGroups.add("Auto" to null)

        if (mappedTrackInfo != null) {
            for (i in 0 until mappedTrackInfo.rendererCount) {
                if (mappedTrackInfo.getRendererType(i) == androidx.media3.common.C.TRACK_TYPE_VIDEO) {
                    val trackGroups = mappedTrackInfo.getTrackGroups(i)
                    for (j in 0 until trackGroups.length) {
                        val group = trackGroups.get(j)
                        for (k in 0 until group.length) {
                            if (mappedTrackInfo.getTrackSupport(i, j, k) == androidx.media3.common.C.FORMAT_HANDLED) {
                                val format = group.getFormat(k)
                                val height = format.height
                                val bitrate = format.bitrate / 1000
                                val label = if (height > 0) "${height}p ($bitrate kbps)" else "Stream $k"
                                
                                val override = TrackSelectionOverride(group, k)
                                videoGroups.add(label to override)
                            }
                        }
                    }
                }
            }
        }
        videoGroups
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Quality",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn {
                    items(tracks.size) { index ->
                        val (name, override) = tracks[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (override == null) {
                                        // Auto
                                        trackSelector.parameters = trackSelector.buildUponParameters()
                                            .clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO)
                                            .build()
                                    } else {
                                        // Specific track
                                        trackSelector.parameters = trackSelector.buildUponParameters()
                                            .clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO)
                                            .addOverride(override)
                                            .build()
                                    }
                                    onQualitySelected(name)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = name == currentQuality,
                                onClick = null 
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name)
                        }
                    }
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
