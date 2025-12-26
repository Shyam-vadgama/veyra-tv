package com.veyra.tv.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.isActive
import java.util.Formatter
import java.util.Locale

import androidx.compose.ui.input.key.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    url: String,
    channelName: String,
    onBack: () -> Unit,
    onError: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val activity = context as? Activity

    // -- TV Focus & State Management --
    val playPauseFocusRequester = remember { FocusRequester() }
    val settingsFocusRequester = remember { FocusRequester() }
    val backFocusRequester = remember { FocusRequester() }
    val sliderFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Track Selector for Quality
    val trackSelector = remember { DefaultTrackSelector(context) }

    // Player state
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var currentQuality by remember { mutableStateOf("Auto") }

    // Progress state
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

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
                            duration = this@apply.duration.coerceAtLeast(0L)
                        } else if (playbackState == Player.STATE_ENDED) {
                            isControlsVisible = true
                        }
                    }

                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        isPlaying = isPlayingState
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        hasError = true
                        isLoading = false
                        onError()
                    }
                })
            }
    }

    // Handle Back Button
    BackHandler {
        if (isControlsVisible) {
            isControlsVisible = false
        } else {
            onBack()
        }
    }

    // -- TV Focus Handling --
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            playPauseFocusRequester.requestFocus()
        }
    }

    // Update progress loop
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(1000)
        }
    }

    // Auto-hide controls
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(8000) // Longer delay for TV
            isControlsVisible = false
        }
    }

    // Manage system UI visibility based on orientation
    DisposableEffect(configuration.orientation) {
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
        onDispose {}
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
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER -> {
                            if (!isControlsVisible) {
                                isControlsVisible = true
                                true
                            } else false
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_UP,
                        android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (!isControlsVisible) {
                                isControlsVisible = true
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { 
                isControlsVisible = !isControlsVisible 
            }
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Gradient Backgrounds for visibility
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent))))
                Box(modifier = Modifier.fillMaxWidth().height(140.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)))))

                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp).align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // -- TV Focusable Button --
                        var backFocused by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (backFocused) 1.1f else 1f, label = "BackScale")
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .focusRequester(backFocusRequester)
                                .focusProperties { down = playPauseFocusRequester; right = settingsFocusRequester }
                                .onFocusChanged { backFocused = it.isFocused }
                                .scale(scale)
                                .border(2.dp, if (backFocused) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .background(if (backFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = channelName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row {
                        // -- TV Focusable Button --
                        var settingsFocused by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (settingsFocused) 1.1f else 1f, label = "SettingsScale")
                        IconButton(
                            onClick = { showQualityDialog = true },
                             modifier = Modifier
                                 .focusRequester(settingsFocusRequester)
                                 .focusProperties { down = playPauseFocusRequester; left = backFocusRequester }
                                 .onFocusChanged { settingsFocused = it.isFocused }
                                 .scale(scale)
                                 .border(2.dp, if (settingsFocused) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                 .background(if (settingsFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Quality", tint = Color.White)
                        }
                    }
                }

                // Center Play/Pause Button
                var playPauseFocused by remember { mutableStateOf(false) }
                val playScale by animateFloatAsState(if (playPauseFocused) 1.1f else 1f, label = "PlayScale")
                IconButton(
                    onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    modifier = Modifier
                        .focusRequester(playPauseFocusRequester)
                        .focusProperties { up = settingsFocusRequester; down = sliderFocusRequester }
                        .onFocusChanged { playPauseFocused = it.isFocused }
                        .align(Alignment.Center)
                        .size(80.dp)
                        .scale(playScale)
                        .border(3.dp, if (playPauseFocused) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                ) {
                     val pauseIcon = remember {
                        ImageVector.Builder(
                            name = "Pause",
                            defaultWidth = 24.dp,
                            defaultHeight = 24.dp,
                            viewportWidth = 24f,
                            viewportHeight = 24f
                        ).apply {
                            path(fill = SolidColor(Color.White)) {
                                moveTo(6f, 19f)
                                horizontalLineToRelative(4f)
                                verticalLineTo(5f)
                                horizontalLineTo(6f)
                                verticalLineToRelative(14f)
                                close()
                                moveTo(14f, 5f)
                                verticalLineToRelative(14f)
                                horizontalLineToRelative(4f)
                                verticalLineTo(5f)
                                horizontalLineToRelative(-4f)
                                close()
                            }
                        }.build()
                    }
                    Icon(imageVector = if (isPlaying) pauseIcon else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                }

                // Bottom Controls
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(24.dp)
                ) {
                    if (duration > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.labelMedium)
                            
                            // -- TV Focusable Slider --
                            var sliderFocused by remember { mutableStateOf(false) }
                            val sliderScale by animateFloatAsState(if (sliderFocused) 1.05f else 1f, label = "SliderScale")
                            
                            Slider(
                                value = currentPosition.toFloat(),
                                onValueChange = { exoPlayer.seekTo(it.toLong()) },
                                valueRange = 0f..duration.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                                    .scale(sliderScale)
                                    .focusRequester(sliderFocusRequester)
                                    .focusProperties { up = playPauseFocusRequester }
                                    .onFocusChanged { sliderFocused = it.isFocused }
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.type == KeyEventType.KeyDown) {
                                            when (keyEvent.nativeKeyEvent.keyCode) {
                                                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                                                    true
                                                }
                                                android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                                    exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration))
                                                    true
                                                }
                                                else -> false
                                            }
                                        } else false
                                    },
                                colors = SliderDefaults.colors(
                                    thumbColor = if (sliderFocused) MaterialTheme.colorScheme.primary else Color.White,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            Text(text = formatTime(duration), color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        var rotateFocused by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (rotateFocused) 1.1f else 1f, label = "RotateScale")
                        IconButton(
                            onClick = {
                                val currentOrientation = configuration.orientation
                                if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                } else {
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                                }
                            },
                             modifier = Modifier
                                 .onFocusChanged { rotateFocused = it.isFocused }
                                 .scale(scale)
                                 .border(2.dp, if (rotateFocused) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                 .background(if (rotateFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Rotate", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Loading Indicator
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Buffering...", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Error Message
        if (hasError) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Error", tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error playing video", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { exoPlayer.prepare(); exoPlayer.play() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Retry") }
            }
        }

        if (showQualityDialog) {
            QualitySelectionDialog(
                trackSelector = trackSelector,
                onDismiss = { showQualityDialog = false },
                currentQuality = currentQuality,
                onQualitySelected = { qualityName -> currentQuality = qualityName; showQualityDialog = false }
            )
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    val formatBuilder = StringBuilder()
    val formatter = Formatter(formatBuilder, Locale.getDefault())
    return if (hours > 0) formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString() else formatter.format("%02d:%02d", minutes, seconds).toString()
}

@OptIn(UnstableApi::class)
@Composable
fun QualitySelectionDialog(
    trackSelector: DefaultTrackSelector,
    onDismiss: () -> Unit,
    currentQuality: String,
    onQualitySelected: (String) -> Unit
) {
    val tracks = remember {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        val videoGroups = mutableListOf<Pair<String, TrackSelectionOverride?>>().apply { add("Auto" to null) }
        if (mappedTrackInfo != null) {
            for (i in 0 until mappedTrackInfo.rendererCount) {
                if (mappedTrackInfo.getRendererType(i) == androidx.media3.common.C.TRACK_TYPE_VIDEO) {
                    val trackGroups = mappedTrackInfo.getTrackGroups(i)
                    for (j in 0 until trackGroups.length) {
                        val group = trackGroups.get(j)
                        for (k in 0 until group.length) {
                            if (mappedTrackInfo.getTrackSupport(i, j, k) == androidx.media3.common.C.FORMAT_HANDLED) {
                                val format = group.getFormat(k)
                                val label = if (format.height > 0) "${format.height}p" else "Stream $k"
                                videoGroups.add(label to TrackSelectionOverride(group, k))
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
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E), // Premium dark card background
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFF333333))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Quality",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(tracks.size) { index ->
                        val (name, override) = tracks[index]
                        val isSelected = name == currentQuality
                        var itemFocused by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (itemFocused) 1.05f else 1f, label = "ItemScale")
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .scale(scale)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .border(1.dp, if (itemFocused) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (override == null) {
                                        trackSelector.parameters = trackSelector.buildUponParameters().clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO).build()
                                    } else {
                                        trackSelector.parameters = trackSelector.buildUponParameters().clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO).addOverride(override).build()
                                    }
                                    onQualitySelected(name)
                                }
                                .onFocusChanged { itemFocused = it.isFocused }
                                .focusable()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { 
                    Text("Close", fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}