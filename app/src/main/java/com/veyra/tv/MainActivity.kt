package com.veyra.tv

import com.veyra.tv.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.veyra.tv.model.Channel
import com.veyra.tv.ui.VideoPlayer
import com.veyra.tv.ui.SettingsScreen
import com.veyra.tv.ui.theme.BackgroundEnd
import com.veyra.tv.ui.theme.BackgroundStart
import com.veyra.tv.ui.theme.IptvplayerTheme
import com.veyra.tv.ui.utils.isTV
import com.veyra.tv.viewmodel.MainViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IptvplayerTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { (url, name) ->
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
            navController.navigate("player/$encodedUrl/$encodedName") {
                popUpTo("home") { inclusive = false }
            }
        }
    }

    Box(modifier = Modifier.background(Brush.verticalGradient(listOf(BackgroundStart, BackgroundEnd)))) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onChannelClick = { channel ->
                        val encodedUrl = URLEncoder.encode(channel.streamUrl, StandardCharsets.UTF_8.toString())
                        val encodedName = URLEncoder.encode(channel.name, StandardCharsets.UTF_8.toString())
                        navController.navigate("player/$encodedUrl/$encodedName")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
            composable("player/{url}/{name}") { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url")
                val name = backStackEntry.arguments?.getString("name")
                if (url != null) {
                    VideoPlayer(
                        url = url,
                        channelName = name ?: "Unknown Channel",
                        onBack = { navController.popBackStack() },
                        onError = { viewModel.onPlaybackFailed(name ?: "Unknown Channel", url) }
                    )
                }
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onChannelClick: (Channel) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val isTv = isTV(context)
    
    val pagedChannels = viewModel.pagedChannels.collectAsLazyPagingItems()
    val categoriesList by viewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val availableCountriesList by viewModel.availableCountries.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentChannelsList by viewModel.recentChannels.collectAsStateWithLifecycle()
    val searchQueryText by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryText by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCountryCode by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val isSyncingState by viewModel.isSyncing.collectAsStateWithLifecycle()
    
    val gridFocusRequester = remember { FocusRequester() }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Removed the LaunchedEffect that stole focus on search results update
    // This allows the user to keep typing without the keyboard closing

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black, Color.Black.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
            ) {
                CenterAlignedTopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "VEYRA TV",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSyncingState) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(end = 16.dp).size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            var isMenuFocused by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { showBottomSheet = true },
                                modifier = Modifier
                                    .onFocusChanged { if(isTv) isMenuFocused = it.isFocused }
                                    .scale(if (isMenuFocused) 1.2f else 1f)
                                    .border(2.dp, if (isMenuFocused) Color.White else Color.Transparent, CircleShape)
                                    .background(if (isMenuFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert, 
                                    contentDescription = "Menu", 
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )

                SearchBar(
                    query = searchQueryText,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    isTv = isTv,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                if (selectedCountryCode != null || selectedCategoryText != null) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCountryCode != null) {
                            FilterIndicator(label = "Region: $selectedCountryCode") {
                                viewModel.setSelectedCountry(null)
                            }
                        }
                        if (selectedCategoryText != null) {
                            FilterIndicator(label = "Category: $selectedCategoryText") {
                                viewModel.onCategorySelected(null)
                            }
                        }
                    }
                }
                
                if (selectedCountryCode != null) {
                    CategorySelector(
                        categories = categoriesList,
                        selectedCategory = selectedCategoryText,
                        onCategorySelected = viewModel::onCategorySelected,
                        isTv = isTv
                    )
                } else {
                    CountryFilterSelector(
                        countries = availableCountriesList,
                        selectedCountry = selectedCountryCode,
                        onCountrySelected = viewModel::setSelectedCountry,
                        isTv = isTv
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (pagedChannels.itemCount > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .focusRequester(gridFocusRequester),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchQueryText.isEmpty() && selectedCategoryText == null && recentChannelsList.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RecentChannelsSlider(
                                channels = recentChannelsList,
                                onChannelClick = {
                                    viewModel.onChannelSelected(it)
                                    onChannelClick(it)
                                },
                                isTv = isTv
                            )
                        }
                    }

                    if (searchQueryText.isEmpty() && selectedCategoryText == null && pagedChannels.itemCount > 0) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                             pagedChannels[0]?.let { firstChannel ->
                                 HeroSection(channel = firstChannel, onClick = { 
                                     viewModel.onChannelSelected(firstChannel)
                                     onChannelClick(firstChannel) 
                                 }, isTv = isTv)
                             }
                        }
                    }

                    items(pagedChannels.itemCount) { index ->
                        val channel = pagedChannels[index]
                        if (searchQueryText.isEmpty() && selectedCategoryText == null && index == 0) return@items

                        if (channel != null) {
                             ChannelGridItem(
                                channel = channel,
                                isFavorite = channel.isFavorite,
                                onFavoriteClick = { viewModel.toggleFavorite(channel) },
                                onClick = { 
                                    viewModel.onChannelSelected(channel)
                                    onChannelClick(channel) 
                                },
                                isTv = isTv
                            )
                        } else {
                             ChannelGridItemSkeleton()
                        }
                    }
                    
                    if (pagedChannels.loadState.append is LoadState.Loading) {
                         item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            } else if (pagedChannels.loadState.refresh is LoadState.Loading || isSyncingState) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Syncing channels...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (pagedChannels.loadState.refresh is LoadState.NotLoading) {
                EmptyState(
                    category = selectedCategoryText,
                    onClearFilters = {
                        viewModel.onSearchQueryChanged("")
                        viewModel.onCategorySelected(null)
                        viewModel.setSelectedCountry(null)
                    }
                )
            }
        }
    }
    
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "MENU",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                )
                
                var isItem1Focused by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text("Select Country / Region", color = if (isItem1Focused) MaterialTheme.colorScheme.primary else Color.White) },
                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isItem1Focused) MaterialTheme.colorScheme.primary else Color.White) },
                    modifier = Modifier
                        .clickable { 
                            showBottomSheet = false
                            showCountryDialog = true 
                        }
                        .onFocusChanged { isItem1Focused = it.isFocused }
                        .background(if (isItem1Focused) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        .padding(horizontal = 8.dp),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                
                var isItem2Focused by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text("Browse All Categories", color = if (isItem2Focused) MaterialTheme.colorScheme.primary else Color.White) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = if (isItem2Focused) MaterialTheme.colorScheme.primary else Color.White) },
                    modifier = Modifier
                        .clickable { 
                            showBottomSheet = false
                            showCategoryDialog = true 
                        }
                        .onFocusChanged { isItem2Focused = it.isFocused }
                        .background(if (isItem2Focused) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        .padding(horizontal = 8.dp),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                
                var isItem3Focused by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text("App Settings", color = if (isItem3Focused) MaterialTheme.colorScheme.primary else Color.White) },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = if (isItem3Focused) MaterialTheme.colorScheme.primary else Color.White) },
                    modifier = Modifier
                        .clickable { 
                            showBottomSheet = false
                            onSettingsClick() 
                        }
                        .onFocusChanged { isItem3Focused = it.isFocused }
                        .background(if (isItem3Focused) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        .padding(horizontal = 8.dp),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
    
    if (showCountryDialog) {
        CountrySelectionDialog(
            countries = availableCountriesList,
            currentSelection = selectedCountryCode,
            onDismiss = { showCountryDialog = false },
            onCountrySelected = { 
                viewModel.setSelectedCountry(it)
                showCountryDialog = false
            }
        )
    }

    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = categoriesList,
            currentSelection = selectedCategoryText,
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { 
                viewModel.onCategorySelected(it)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
fun RecentChannelsSlider(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    isTv: Boolean
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = "Recently Watched",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(channels) { channel ->
                RecentChannelItem(channel = channel, onClick = { onChannelClick(channel) }, isTv = isTv)
            }
        }
    }
}

@Composable
fun RecentChannelItem(channel: Channel, onClick: () -> Unit, isTv: Boolean) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.15f else 1f, label = "RecentScale")
    val zIndex = if (isFocused) 1f else 0f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .zIndex(zIndex)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            border = if (isFocused) BorderStroke(3.dp, Color.White) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(channel.logoUrl)
                    .crossfade(true)
                    .size(300, 300) // Explicit low res for thumbnails
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = channel.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isFocused) Color.White else Color.Gray,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CountryFilterSelector(
    countries: List<String>,
    selectedCountry: String?,
    onCountrySelected: (String?) -> Unit,
    isTv: Boolean
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CategoryChip(
                label = "Global",
                isSelected = selectedCountry == null,
                onClick = { onCountrySelected(null) },
                isTv = isTv,
                icon = Icons.Default.LocationOn
            )
        }
        items(countries) { country ->
            CategoryChip(
                label = country,
                isSelected = selectedCountry == country,
                onClick = { onCountrySelected(country) },
                isTv = isTv
            )
        }
    }
}

@Composable
fun FilterIndicator(label: String, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onClear, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun CategorySelectionDialog(
    categories: List<String>,
    currentSelection: String?,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(null) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSelection == null,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("All Categories", color = Color.White)
                        }
                    }
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSelection == category,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(category, color = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun CountrySelectionDialog(
    countries: List<String>,
    currentSelection: String?,
    onDismiss: () -> Unit,
    onCountrySelected: (String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Country",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(null) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSelection == null,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("All Countries", color = Color.White)
                        }
                    }
                    items(countries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSelection == country,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(country, color = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EmptyState(category: String?, onClearFilters: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search, 
            contentDescription = null, 
            tint = Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No channels found",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClearFilters,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Clear All Filters")
        }
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isTv: Boolean,
    modifier: Modifier = Modifier
) {
    // Local state to hold the immediate text input
    var textState by remember(query) { mutableStateOf(query) }
    
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.02f else 1f, label = "SearchScale")
    val zIndex = if (isFocused) 1f else 0f
    
    // Debounce the update to the parent
    LaunchedEffect(textState) {
        if (textState != query) {
            kotlinx.coroutines.delay(300) // Debounce 300ms
            onQueryChange(textState)
        }
    }

    Box(modifier = modifier.zIndex(zIndex)) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .onFocusChanged { isFocused = it.isFocused },
            placeholder = { Text("Search channels...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            trailingIcon = {
                if (textState.isNotEmpty()) {
                    IconButton(onClick = { textState = ""; onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                 onQueryChange(textState) // Immediate update on enter
                 focusManager.moveFocus(FocusDirection.Down)
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2B2B2B),
                unfocusedContainerColor = Color(0xFF2B2B2B),
                focusedBorderColor = if (isFocused) Color.White else MaterialTheme.colorScheme.primary, // High contrast for TV
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    isTv: Boolean
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CategoryChip(
                label = "All",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                isTv = isTv
            )
        }
        items(categories) { category ->
            CategoryChip(
                label = category,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                isTv = isTv,
                icon = if (category == "Favorites") Icons.Default.Favorite else null
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isTv: Boolean,
    icon: ImageVector? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused || isSelected) 1.1f else 1f, label = "ChipScale")
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2B2B2B)
    val contentColor = if (isSelected) Color.White else Color.Gray
    val border = if (isFocused) BorderStroke(3.dp, Color.White) else null
    val zIndex = if (isFocused) 1f else 0f

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = border,
        modifier = Modifier
            .height(36.dp)
            .zIndex(zIndex)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp).padding(end = 6.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChannelGridItem(
    channel: Channel,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    isTv: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.1f else 1.0f, label = "CardScale")
    // Use White for TV focus to ensure visibility against dark backgrounds
    val border = if (isFocused) BorderStroke(3.dp, Color.White) else BorderStroke(1.dp, Color(0xFF2B2B2B))
    val zIndex = if (isFocused) 1f else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Cinematic Aspect Ratio
            .zIndex(zIndex) // Ensure focused item draws on top
            .onFocusChanged { isFocused = it.hasFocus }
            .focusable()
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Logo centered
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(channel.logoUrl)
                    .crossfade(true)
                    .diskCacheKey(channel.logoUrl)
                    .size(400, 225) // Approx 16:9 thumbnail size
                    .error(android.R.drawable.ic_menu_report_image)
                    .build(),
                contentDescription = channel.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp) // Generous padding for logos
                    .alpha(0.8f)
            )

            // Gradient Overlay for Text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)),
                            startY = 100f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = channel.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = channel.category,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Favorite Button
            var isFavFocused by remember { mutableStateOf(false) }
            // Animate scale when favorite status changes
            val favScale by animateFloatAsState(
                targetValue = if (isFavorite) 1.2f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "FavScale"
            )
            
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .onFocusChanged { if(isTv) isFavFocused = it.isFocused }
                    .background(if (isFavFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFF4081) else Color.Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .scale(favScale)
                )
            }
        }
    }
}

@Composable
fun ChannelGridItemSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2B2B2B)))
    }
}

@Composable
fun HeroSection(channel: Channel, onClick: () -> Unit, isTv: Boolean) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.05f else 1f, label = "HeroScale")
    val zIndex = if (isFocused) 1f else 0f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(bottom = 16.dp)
            .zIndex(zIndex)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(3.dp, if (isFocused) Color.White else Color.Transparent, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .onFocusChanged { isFocused = it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
    ) {
        // Background Image (Blurred or dim)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(channel.logoUrl)
                .crossfade(true)
                .size(800, 450) // Higher res for hero but still bounded
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.6f)
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = channel.category,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        // Play Button Overlay
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
                .size(56.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}
