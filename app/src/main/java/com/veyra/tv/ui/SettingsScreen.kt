package com.veyra.tv.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.veyra.tv.viewmodel.MainViewModel
import com.veyra.tv.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTv = com.veyra.tv.ui.utils.isTV(context)
    val categories by viewModel.settingsCategories.collectAsStateWithLifecycle()
    val userCountry by viewModel.userCountry.collectAsStateWithLifecycle()
    val currentDefaultCategory = viewModel.getDefaultCategory()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SETTINGS", 
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
                            .border(
                                width = 2.dp,
                                color = if (isBackFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = androidx.compose.foundation.shape.CircleShape
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- SECTION: ABOUT ---
            item { SettingsHeader("About") }
            item {
                SettingsRow(
                    title = "App Name",
                    subtitle = "Veyra TV",
                    icon = Icons.Default.Info,
                    isTv = isTv
                )
            }
            item {
                SettingsRow(
                    title = "Description",
                    subtitle = "A clean, open-source media player for IPTV streams. We do not host or provide any content.",
                    icon = Icons.Default.Description,
                    isTv = isTv
                )
            }
            item {
                SettingsRow(
                    title = "Version",
                    subtitle = "1.0.0 (Build 1)",
                    icon = Icons.Default.Build,
                    isTv = isTv
                )
            }

            // --- SECTION: PREFERENCES ---
            item { SettingsHeader("Preferences") }
            item {
                var expanded by remember { mutableStateOf(false) }
                var selectedOptionText by remember { mutableStateOf(currentDefaultCategory ?: "All") }
                
                PreferenceDropdownRow(
                    title = "Default Category",
                    subtitle = "Showing: $selectedOptionText",
                    icon = Icons.Default.Settings,
                    isTv = isTv,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    options = listOf("All") + categories,
                    selectedOption = selectedOptionText,
                    onOptionSelected = {
                        selectedOptionText = it
                        viewModel.saveDefaultCategory(if (it == "All") null else it)
                        expanded = false
                    }
                )
            }
            item {
                SettingsRow(
                    title = "Detected Region",
                    subtitle = userCountry ?: "Detecting...",
                    icon = Icons.Default.LocationOn,
                    isTv = isTv
                )
            }

            // --- SECTION: DEVELOPER ---
            item { SettingsHeader("Developer") }
            item {
                SettingsRow(
                    title = "Developer",
                    subtitle = "Veyra Open Source Team",
                    icon = Icons.Default.Person,
                    isTv = isTv
                )
            }
            item {
                SettingsRow(
                    title = "GitHub Repository",
                    subtitle = "View source code on GitHub",
                    icon = Icons.Default.Code,
                    isTv = isTv,
                    onClick = { openUrl(context, "https://github.com/Shyam-vadgama/veyra-tv") }
                )
            }
            item {
                SettingsRow(
                    title = "LinkedIn",
                    subtitle = "Connect with us",
                    icon = Icons.Default.AccountBox,
                    isTv = isTv,
                    onClick = { openUrl(context, "https://www.linkedin.com/in/shyam-vadgama-873955369/") }
                )
            }

            // --- SECTION: SUPPORT & FEEDBACK ---
            item { SettingsHeader("Support & Feedback") }
            item {
                SettingsRow(
                    title = "Report a Bug",
                    subtitle = "Help us improve Veyra TV",
                    icon = Icons.Default.BugReport,
                    isTv = isTv,
                    onClick = { openUrl(context, "https://github.com/Shyam-vadgama/veyra-tv/issues/new?template=bug_report.yml") }
                )
            }
            item {
                SettingsRow(
                    title = "Request a Feature",
                    subtitle = "Suggest a new idea",
                    icon = Icons.Default.Add,
                    isTv = isTv,
                    onClick = { openUrl(context, "https://github.com/Shyam-vadgama/veyra-tv/issues/new?template=feature_request.yml") }
                )
            }
            item {
                SettingsRow(
                    title = "Contact Support",
                    subtitle = "Send us an email",
                    icon = Icons.Default.Email,
                    isTv = isTv,
                    onClick = { sendEmail(context, "shyam.veyra.tv@gmail.com", "Veyra TV Support Request") }
                )
            }

            // --- SECTION: LEGAL ---
            item { SettingsHeader("Legal") }
            item {
                SettingsRow(
                    title = "Disclaimer",
                    subtitle = "Veyra TV is a media player. Users must provide their own content (M3U playlists).",
                    icon = Icons.Default.Gavel,
                    isTv = isTv
                )
            }
            item {
                SettingsRow(
                    title = "Open Source Licenses",
                    subtitle = "Third-party libraries used in this project",
                    icon = Icons.Default.MenuBook,
                    isTv = isTv,
                    onClick = { /* Navigate to license page if available */ }
                )
            }

            // --- SECTION: MISC ---
            item { SettingsHeader("Misc") }
            item {
                SettingsRow(
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    icon = Icons.Default.PrivacyTip,
                    isTv = isTv,
                    onClick = { openUrl(context, "https://Shyam-vadgama.github.io/veyra-tv/privacy.html") }
                )
            }
            item {
                SettingsRow(
                    title = "Terms of Use",
                    subtitle = "Agreement for using the app",
                    icon = Icons.Default.Description,
                    isTv = isTv,
                    onClick = { openUrl(context, "https://Shyam-vadgama.github.io/veyra-tv/terms.html") }
                )
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    isTv: Boolean,
    onClick: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.02f else 1f, label = "RowScale")
    val backgroundColor = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(enabled = onClick != null || isTv)
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceDropdownRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isTv: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.02f else 1f, label = "RowScale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            Surface(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .scale(scale)
                    .onFocusChanged { isFocused = it.isFocused }
                    .focusable()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onExpandedChange(!expanded) },
                color = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                border = BorderStroke(2.dp, if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = if (isFocused) MaterialTheme.colorScheme.primary else Color.Gray)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Icon(
                        if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        null,
                        tint = Color.Gray
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = { onOptionSelected(option) },
                        colors = MenuDefaults.itemColors()
                    )
                }
            }
        }
    }
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error
    }
}

fun sendEmail(context: Context, email: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error
    }
}
