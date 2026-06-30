package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun SkincareAppContent(viewModel: SkincareViewModel) {
    val user by viewModel.userProfile.collectAsState()

    // Redirect to LOGIN if not logged in
    LaunchedEffect(user) {
        if (user == null || user?.isLoggedIn == false) {
            viewModel.currentScreen = SkincareScreen.LOGIN
        } else if (viewModel.currentScreen == SkincareScreen.LOGIN) {
            viewModel.currentScreen = SkincareScreen.DASHBOARD
        }
    }

    Scaffold(
        bottomBar = {
            if (viewModel.currentScreen != SkincareScreen.LOGIN) {
                BottomBarNavigation(
                    currentScreen = viewModel.currentScreen,
                    onNavigate = { viewModel.currentScreen = it }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = viewModel.currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    SkincareScreen.LOGIN -> LoginScreen(viewModel)
                    SkincareScreen.DASHBOARD -> DashboardScreen(viewModel)
                    SkincareScreen.SCANNER -> ScannerScreen(viewModel)
                    SkincareScreen.MIRROR -> VirtualMirrorScreen(viewModel)
                    SkincareScreen.PRODUCTS -> ProductsScreen(viewModel)
                    SkincareScreen.FORUM -> CommunityForumScreen(viewModel)
                    SkincareScreen.SMARTWATCH -> SmartwatchSyncScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun BottomBarNavigation(
    currentScreen: SkincareScreen,
    onNavigate: (SkincareScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF030712),
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        val items = listOf(
            Triple(SkincareScreen.DASHBOARD, "Glow", Icons.Outlined.Dashboard),
            Triple(SkincareScreen.SCANNER, "AI Scan", Icons.Outlined.Face),
            Triple(SkincareScreen.MIRROR, "Mirror", Icons.Outlined.AutoAwesome),
            Triple(SkincareScreen.PRODUCTS, "Products", Icons.Outlined.ShoppingBag),
            Triple(SkincareScreen.FORUM, "Forum", Icons.Outlined.Forum),
            Triple(SkincareScreen.SMARTWATCH, "Band", Icons.Outlined.Watch)
        )

        items.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_tab_${label.lowercase(Locale.getDefault())}")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: SkincareViewModel) {
    var emailOrMobile by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = "GlowSkin Spa Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "GlowSkin AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Text(
            text = "AI-Driven Skincare Regimen & Tracker",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Card Frame for inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome back",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Log in with email or mobile to save your skin profiles, hydration targets, and scan history.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = "" },
                    label = { Text("Your Name") },
                    placeholder = { Text("e.g. Aria Carter") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_name_input")
                )

                OutlinedTextField(
                    value = emailOrMobile,
                    onValueChange = { emailOrMobile = it; errorText = "" },
                    label = { Text("Email or Mobile Number") },
                    placeholder = { Text("e.g. aria@example.com or +1234567") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email/Phone Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_credential_input")
                )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank() || emailOrMobile.isBlank()) {
                            errorText = "Please enter both fields to proceed."
                        } else {
                            viewModel.login(emailOrMobile, name)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Access Skincare Studio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guest preview option
        TextButton(
            onClick = {
                viewModel.login("guest@glowskin.ai", "Glow Guest")
            },
            modifier = Modifier.testTag("login_guest_button")
        ) {
            Text(
                text = "Continue as Demo Guest",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DashboardScreen(viewModel: SkincareViewModel) {
    val user by viewModel.userProfile.collectAsState()
    val hydration by viewModel.todayHydration.collectAsState()
    val fitnessSync by viewModel.todayFitnessSync.collectAsState()
    val historyList by viewModel.scanHistory.collectAsState()

    var showProfileEditor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Dashboard Header
        val latestScore = historyList.firstOrNull()?.overallHealthScore ?: 84
        DashboardHeader(
            userName = user?.name ?: "User",
            onToggleTheme = { viewModel.isDarkMode = !viewModel.isDarkMode },
            isDarkMode = viewModel.isDarkMode,
            onEditProfile = { showProfileEditor = true },
            latestScore = latestScore
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Weather Preset Selection & Regimen Suggestions based on weather!
            WeatherRoutineCard(viewModel)

            // Smartband Correlation Banner
            SmartbandCorrelationBanner(fitnessSync)

            // Dynamic Hydration Progress
            hydration?.let { HydrationProgressCard(log = it, onLogWater = { viewModel.logWater(it) }) }

            // Healthy Skin Foods section
            HealthySkinFoodsSection(viewModel)

            // Notifications & Timely Reminders Hub
            RemindersHubSection(viewModel)

            // Historic skin scores timeline (Custom charts/progress)
            if (historyList.isNotEmpty()) {
                SkinScoresTimelineCard(historyList)
            } else {
                EmptyScanHistoryPrompt { viewModel.currentScreen = SkincareScreen.SCANNER }
            }
        }
    }

    if (showProfileEditor) {
        ProfileEditorDialog(
            user = user,
            onDismiss = { showProfileEditor = false },
            onSave = { name, type, concerns, age ->
                viewModel.updateUserProfile(name, type, concerns, age)
                showProfileEditor = false
            }
        )
    }
}

@Composable
fun DashboardHeader(
    userName: String,
    onToggleTheme: () -> Unit,
    isDarkMode: Boolean,
    onEditProfile: () -> Unit,
    latestScore: Int = 84
) {
    val initials = if (userName.length >= 2) {
        userName.substring(0, 2).uppercase()
    } else {
        userName.take(1).uppercase() + "U"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
            Text(
                text = greeting.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF14B8A6),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = userName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = Color(0xFF14B8A6),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onEditProfile,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, Color(0xFF1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onEditProfile() }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.5.dp, Color(0xFF14B8A6).copy(alpha = 0.3f), CircleShape)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E293B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF1F5F9)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF14B8A6), CircleShape)
                        .border(1.5.dp, Color(0xFF0A0A0A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = latestScore.toString(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherRoutineCard(viewModel: SkincareViewModel) {
    val activePreset = viewModel.activeWeather
    val suggestions = viewModel.getWeatherRecommendations()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Skin Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Badges from HTML
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // UV Index
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF14B8A6).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF14B8A6).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UV INDEX: ${activePreset.uvIndex}.0",
                                color = Color(0xFF2DD4BF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Humidity
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF60A5FA).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "HUMIDITY: ${activePreset.humidityPercent}%",
                                color = Color(0xFF60A5FA),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Small Selector to test other weather regimes
                var showDropdown by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Change Weather Simulation",
                            tint = Color(0xFF14B8A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        WeatherPreset.values().forEach { preset ->
                            DropdownMenuItem(
                                text = { Text("${preset.icon} ${preset.title}") },
                                onClick = {
                                    viewModel.activeWeather = preset
                                    viewModel.addAppNotification("Weather condition simulated to ${preset.title}.")
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specific weather warning advice (quoted & styled from HTML)
            Text(
                text = "\"${activePreset.recommendationText}\"",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8), // slate-400
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 2.dp)
            )

            // Dynamic Stats Grid from the Sophisticated Dark design theme
            Spacer(modifier = Modifier.height(18.dp))
            Divider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stat 1: Hydration
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HYDRATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B), // Slate 500
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val hydValue = if (activePreset.humidityPercent < 40) "Dehydrated" else "Optimal"
                    val hydColor = if (activePreset.humidityPercent < 40) Color(0xFFFB923C) else Color(0xFF2DD4BF)
                    Text(
                        text = hydValue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = hydColor
                    )
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF1E293B))
                )

                // Stat 2: Pore Clarity
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PORE CLARITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B), // Slate 500
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val clarityValue = if (activePreset == WeatherPreset.HOT_HUMID) "64%" else "82%"
                    Text(
                        text = clarityValue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFB923C) // Orange 400
                    )
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF1E293B))
                )

                // Stat 3: Texture
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TEXTURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B), // Slate 500
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val textureValue = if (activePreset == WeatherPreset.COLD_DRY) "Flaky" else "Smooth"
                    Text(
                        text = textureValue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF818CF8) // Indigo 400
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Divider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "🎯 Suggested Daily Steps for Current Environment:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            suggestions.forEach { step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14B8A6),
                        modifier = Modifier.width(16.dp)
                    )
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun SmartbandCorrelationBanner(sync: FitnessSync?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Watch,
                contentDescription = "Fitness watch correlation",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Smartband Correlation Engine",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (sync != null && sync.steps > 0) {
                    val sleepAdv = if (sync.sleepHours >= 8.0) "Optimal skin cell repair" else "Mild barrier susceptibility"
                    Text(
                        text = "Today's metrics: ${sync.steps} steps | ${sync.sleepHours}h sleep (${sync.sleepQuality}). Sleep Quality index correlates to: **$sleepAdv**.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "No Smartband data synchronized today. Sync in the 'Band' tab to evaluate sleep-to-sebum correlations.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun HydrationProgressCard(log: HydrationLog, onLogWater: (Int) -> Unit) {
    val progress = (log.amountMl.toFloat() / log.goalMl.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = "Water Tracker Icon",
                        tint = Color(0xFF14B8A6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hydration Tracker",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "${(log.amountMl / 1000.0)} / ${(log.goalMl / 1000.0)} Liters",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2DD4BF)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF14B8A6),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(250, 500, 750).forEach { amount ->
                    OutlinedButton(
                        onClick = { onLogWater(amount) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2DD4BF)
                        )
                    ) {
                        Text(text = "+$amount ml", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthySkinFoodsSection(viewModel: SkincareViewModel) {
    val foods = viewModel.getSkinFoods()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = "Food icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Skin-Boosting Food Items",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Promote skin health from the inside out with these nutritious ingredients:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable or list representation of foods
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                foods.forEach { (food, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Eco icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = food,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersHubSection(viewModel: SkincareViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Reminder Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Timely Reminders & Logs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = "${viewModel.appNotifications.size} Alert(s)",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(2.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Show latest 3 notifications
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.appNotifications.take(3).forEach { notification ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔔",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = notification,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SkinScoresTimelineCard(history: List<ScanHistory>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Skin Progress Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Visualizing your overall skin health scores from your AI-driven scans.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Draw a gorgeous customized visual graph representing skin health improvements
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .drawBehind {
                        val points = history.take(6).reversed()
                        if (points.size < 2) {
                            // Draw horizontal line
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 2f
                            )
                            return@drawBehind
                        }

                        val maxScore = 100f
                        val minScore = 0f
                        val scoreRange = maxScore - minScore

                        val widthStep = size.width / (points.size - 1)
                        val pathPoints = points.mapIndexed { idx, scan ->
                            val x = idx * widthStep
                            val scoreFraction = (scan.overallHealthScore - minScore) / scoreRange
                            val y = size.height - (scoreFraction * size.height)
                            Offset(x, y)
                        }

                        // Draw background fill gradient
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height)
                            pathPoints.forEach { point ->
                                lineTo(point.x, point.y)
                            }
                            lineTo(pathPoints.last().x, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFD67E65).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )

                        // Draw path line
                        for (i in 0 until pathPoints.size - 1) {
                            drawLine(
                                color = Color(0xFFD67E65),
                                start = pathPoints[i],
                                end = pathPoints[i + 1],
                                strokeWidth = 4f
                            )
                        }

                        // Draw point indicators
                        pathPoints.forEachIndexed { i, point ->
                            drawCircle(
                                color = Color(0xFFD67E65),
                                radius = 6f,
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = point
                            )
                        }
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // X-axis label references
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dates = history.take(6).reversed()
                dates.forEach { scan ->
                    val format = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val dateLabel = format.format(Date(scan.timestamp))
                    Text(
                        text = "$dateLabel (${scan.overallHealthScore}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyScanHistoryPrompt(onScanNow: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Face Scan Reminder",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Unlock AI Skincare Regimens",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Use our AI scanner to get your Skin Health Index, analyze breakouts, wrinkle indexes, and generate customized product recommendations.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onScanNow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("scan_now_dashboard_button")
            ) {
                Text(text = "Scan Face with AI")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorDialog(
    user: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var ageStr by remember { mutableStateOf(user?.age?.toString() ?: "25") }
    var selectedSkinType by remember { mutableStateOf(user?.skinType ?: "Normal") }
    var concerns by remember { mutableStateOf(user?.primaryConcerns ?: "") }

    val skinTypes = listOf("Normal", "Dry", "Oily", "Sensitive", "Combination")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Skincare Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Skin Type:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Horizontal Flow/Row of Skin Types
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    skinTypes.forEach { type ->
                        val selected = selectedSkinType == type
                        FilterChip(
                            selected = selected,
                            onClick = { selectedSkinType = type },
                            label = { Text(type, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = concerns,
                    onValueChange = { concerns = it },
                    label = { Text("Primary Skin Concerns") },
                    placeholder = { Text("e.g. Acne, fine lines, dryness") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageStr.toIntOrNull() ?: 25
                    onSave(name, selectedSkinType, concerns, age)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ScannerScreen(viewModel: SkincareViewModel) {
    val context = LocalContext.current
    val history by viewModel.scanHistory.collectAsState()

    var inputConcerns by remember { mutableStateOf("Acne breakout and dehydrated skin.") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var bitmapPreview by remember { mutableStateOf<Bitmap?>(null) }

    // Intent launcher to simulate face capturing or gallery uploads
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                bitmapPreview = BitmapFactory.decodeStream(inputStream)
                viewModel.addAppNotification("Face scan image loaded successfully.")
            } catch (e: Exception) {
                viewModel.addAppNotification("Error loading image: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI-Driven Face Scan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Scan your face using our high-precision computer-vision AI to track hydration, wrinkles, congestion, and build expert routines with explanation.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Visual Scanner Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmapPreview != null) {
                Image(
                    bitmap = bitmapPreview!!.asImageBitmap(),
                    contentDescription = "Face Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Cool Scanner Placeholder with graphic lines
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Draw bounding scanner box
                            val margin = 60f
                            val w = size.width
                            val h = size.height
                            drawLine(
                                Color(0xFFD67E65),
                                Offset(margin, margin),
                                Offset(margin + 40f, margin),
                                strokeWidth = 8f
                            )
                            drawLine(
                                Color(0xFFD67E65),
                                Offset(margin, margin),
                                Offset(margin, margin + 40f),
                                strokeWidth = 8f
                            )

                            drawLine(
                                Color(0xFFD67E65),
                                Offset(w - margin, margin),
                                Offset(w - margin - 40f, margin),
                                strokeWidth = 8f
                            )
                            drawLine(
                                Color(0xFFD67E65),
                                Offset(w - margin, margin),
                                Offset(w - margin, margin + 40f),
                                strokeWidth = 8f
                            )

                            drawLine(
                                Color(0xFFD67E65),
                                Offset(margin, h - margin),
                                Offset(margin + 40f, h - margin),
                                strokeWidth = 8f
                            )
                            drawLine(
                                Color(0xFFD67E65),
                                Offset(margin, h - margin),
                                Offset(margin, h - margin - 40f),
                                strokeWidth = 8f
                            )

                            drawLine(
                                Color(0xFFD67E65),
                                Offset(w - margin, h - margin),
                                Offset(w - margin - 40f, h - margin),
                                strokeWidth = 8f
                            )
                            drawLine(
                                Color(0xFFD67E65),
                                Offset(w - margin, h - margin),
                                Offset(w - margin, h - margin - 40f),
                                strokeWidth = 8f
                            )
                        }
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera placeholder",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Face viewport ready",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Capture simulation or upload skin selfie photo.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Scanner Sweep line animation (only active when analyzing)
            if (viewModel.scanUiState is ScanUiState.Analyzing) {
                val infiniteTransition = rememberInfiniteTransition(label = "SweepTransition")
                val yOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 260f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "ScannerSweep"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = yOffset.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFFD67E65),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Action selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("upload_skin_selfie_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Photo", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    // Simulates real capture by choosing a simulated placeholder selfie path
                    selectedImageUri = "simulated_selfie_capture"
                    bitmapPreview = null // trigger virtual scan
                    viewModel.addAppNotification("Selfie captured via simulated front camera viewport!")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("capture_sim_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture Selfie", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Skin Concerns detail text
        OutlinedTextField(
            value = inputConcerns,
            onValueChange = { inputConcerns = it },
            label = { Text("Describe current concerns or focus points") },
            placeholder = { Text("e.g. Dry skin around cheeks, dark spots under eyes.") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_concerns_input"),
            maxLines = 3
        )

        // Run Analysis button
        Button(
            onClick = {
                viewModel.triggerSkinScan(
                    customInputConcern = inputConcerns,
                    mockImagePath = selectedImageUri ?: "simulated_selfie_capture"
                )
            },
            enabled = viewModel.scanUiState !is ScanUiState.Analyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("run_analysis_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (viewModel.scanUiState is ScanUiState.Analyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("AI Analyzing Face Scan...")
            } else {
                Text("Execute AI Skin Analysis", fontWeight = FontWeight.Bold)
            }
        }

        // Present output states
        when (val state = viewModel.scanUiState) {
            is ScanUiState.Success -> {
                AnalysisResultsCard(scan = state.result, onReset = { viewModel.resetScanState() })
            }
            is ScanUiState.Error -> {
                Text(
                    text = "Analysis Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                // Show past scans
                if (history.isNotEmpty()) {
                    Text(
                        text = "Historical Scans History",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )

                    history.forEach { pastScan ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(pastScan.timestamp)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                                        Text(
                                            text = "Glow Index: ${pastScan.overallHealthScore}%",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = pastScan.reasoning,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisResultsCard(scan: ScanHistory, onReset: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analysis_results_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Completed ✨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onReset) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Results")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score indices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreItem(label = "Overall Glow", score = scan.overallHealthScore, color = Color(0xFFD67E65))
                ScoreItem(label = "Acne Care", score = 100 - scan.acneScore, color = Color(0xFF4A6B5D))
                ScoreItem(label = "Elasticity", score = 100 - scan.wrinkleScore, color = Color(0xFFEF9A9A))
                ScoreItem(label = "Hydration", score = 100 - scan.dryScore, color = Color(0xFF29B6F6))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Detailed Analysis Reasoning:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = scan.reasoning,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Personalized Skincare Routine Suggested:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Morning / Evening steps formatted
            Text(
                text = scan.recommendedRoutine,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ScoreItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.12f), CircleShape)
                .border(1.5.dp, color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VirtualMirrorScreen(viewModel: SkincareViewModel) {
    val history by viewModel.scanHistory.collectAsState()

    var sliderPosition by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Virtual Mirror & Timeline",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Compare your face scanning history side-by-side to track redness reductions, hydration levels, and routine efficacy over time.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        // Interactive split comparison view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Left image (Before / Past scan simulation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Drawing dynamic simulated "Before" face skin with minor redness
                        drawRect(Color(0xFFF9E0D3))
                        // Draw acne indicators or texture blemishes on the left half
                        drawCircle(Color(0xFFE57373).copy(alpha = 0.6f), radius = 22f, center = Offset(size.width * 0.25f, size.height * 0.4f))
                        drawCircle(Color(0xFFE57373).copy(alpha = 0.4f), radius = 14f, center = Offset(size.width * 0.35f, size.height * 0.55f))
                        drawCircle(Color(0xFFE57373).copy(alpha = 0.5f), radius = 18f, center = Offset(size.width * 0.2f, size.height * 0.65f))
                    }
            ) {
                // Label
                Text(
                    text = "PAST (BEFORE)",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Right image (After / Simulated progress) based on slider position
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = (300 * sliderPosition).dp) // clip right side dynamically
                    .drawBehind {
                        // Drawing dynamic simulated "After" face skin - smooth, clean glow!
                        drawRect(Color(0xFFFDEFE6))
                        // Draw subtle healthy glow accents
                        drawCircle(Color.White.copy(alpha = 0.4f), radius = 35f, center = Offset(size.width * 0.75f, size.height * 0.35f))
                    }
            ) {
                // Label
                Text(
                    text = "PRESENT (AFTER)",
                    color = Color(0xFFD67E65),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Slider divider line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .offset(x = (320 * sliderPosition).dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        // Slider controller
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "← Past Blemishes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Healthy Glow →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("mirror_slider")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Skincare progress description card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📈 Skin Improvement Insights",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "With regular application of Hyaluronic Acid and Niacinamide combined with consistent 8-hour sleep syncs, your barrier redness index has decreased by 35% over the last 14 days, and dermal hydration levels are up 40%.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun ProductsScreen(viewModel: SkincareViewModel) {
    val products by viewModel.allProducts.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Derive filtered and sorted product lists reactively
    val filteredProducts = remember(
        products,
        viewModel.searchQuery,
        viewModel.selectedCategoryFilter,
        viewModel.selectedIngredientFilter,
        viewModel.selectedSkinTypeFilter,
        viewModel.sortByOption
    ) {
        var list = products.filter { product ->
            val matchSearch = product.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                    product.brand.contains(viewModel.searchQuery, ignoreCase = true) ||
                    product.activeIngredient.contains(viewModel.searchQuery, ignoreCase = true)

            val matchCategory = viewModel.selectedCategoryFilter == "All" ||
                    product.category.equals(viewModel.selectedCategoryFilter, ignoreCase = true)

            val matchIngredient = viewModel.selectedIngredientFilter == "All" ||
                    product.activeIngredient.equals(viewModel.selectedIngredientFilter, ignoreCase = true)

            val matchSkinType = viewModel.selectedSkinTypeFilter == "All" ||
                    product.skinTypeCompat.equals(viewModel.selectedSkinTypeFilter, ignoreCase = true) ||
                    product.skinTypeCompat.equals("All", ignoreCase = true)

            matchSearch && matchCategory && matchIngredient && matchSkinType
        }

        // Apply sorting
        list = when (viewModel.sortByOption) {
            "Price Low to High" -> list.sortedBy { it.price }
            "Price High to Low" -> list.sortedByDescending { it.price }
            "Rating (High to Low)" -> list.sortedByDescending { it.rating }
            else -> list
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recommended Products",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("Search by brand, product, or ingredient...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("product_search_bar"),
            singleLine = true
        )

        // Filter / Sort toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredProducts.size} Products matching",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Button(
                onClick = { showFilters = !showFilters },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (showFilters) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                modifier = Modifier.testTag("toggle_filters_button")
            ) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filters icon")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Filters & Sort", fontSize = 12.sp)
            }
        }

        // Expanded filter options
        if (showFilters) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category filter
                    FilterRow(
                        label = "Category:",
                        options = listOf("All", "Cleanser", "Serum", "Moisturizer", "Sunscreen", "Toner"),
                        selected = viewModel.selectedCategoryFilter,
                        onSelect = { viewModel.selectedCategoryFilter = it }
                    )

                    // Active Ingredient filter
                    FilterRow(
                        label = "Active Ingredient:",
                        options = listOf("All", "Hyaluronic Acid", "Salicylic Acid", "Niacinamide", "Retinol", "Vitamin C", "Zinc Oxide"),
                        selected = viewModel.selectedIngredientFilter,
                        onSelect = { viewModel.selectedIngredientFilter = it }
                    )

                    // Sort row
                    Text(
                        text = "Sort Products by:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf("Rating (High to Low)", "Price Low to High", "Price High to Low").forEach { option ->
                            val selected = viewModel.sortByOption == option
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.sortByOption = option },
                                label = { Text(option, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Products scrolling lists
        if (filteredProducts.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProducts) { product ->
                    ProductCardItem(product)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No products match filters.", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Try clearing active ingredient search queries or switching filters.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FilterRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    label = { Text(option, fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
fun ProductCardItem(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.brand.uppercase(Locale.getDefault()),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "$${product.price}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = product.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Active ingredient
                Badge(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)) {
                    Text(text = product.activeIngredient, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Routine suitability badge
                if (product.isMorning && product.isEvening) {
                    Badge(containerColor = Color.LightGray.copy(alpha = 0.3f)) {
                        Text(text = "Day & Night", modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                    }
                } else if (product.isMorning) {
                    Badge(containerColor = Color(0xFFFFF9C4)) {
                        Text(text = "Morning only", modifier = Modifier.padding(4.dp), fontSize = 10.sp, color = Color.DarkGray)
                    }
                } else if (product.isEvening) {
                    Badge(containerColor = Color(0xFFE8EAF6)) {
                        Text(text = "Night only", modifier = Modifier.padding(4.dp), fontSize = 10.sp, color = Color(0xFF3F51B5))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dermatologist explanation reasons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.FactCheck,
                    contentDescription = "Checkmark",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Why we recommend this:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = product.benefits,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityForumScreen(viewModel: SkincareViewModel) {
    val posts by viewModel.forumPosts.collectAsState()

    var inputContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Routine Tip") }
    val categories = listOf("Routine Tip", "Product Review", "Skincare Question")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "GlowSkin Forum",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Write a new post card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Share Your Skincare Review / Tip", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = inputContent,
                    onValueChange = { inputContent = it },
                    placeholder = { Text("Write your routine tip, ask questions, or review products here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("forum_post_input"),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (inputContent.isNotBlank()) {
                            viewModel.submitForumPost(
                                content = inputContent,
                                category = selectedCategory,
                                rating = if (selectedCategory == "Product Review") 5 else 0
                            )
                            inputContent = ""
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("submit_forum_post_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Publish Post", fontSize = 12.sp)
                }
            }
        }

        // Scrolling Feed
        if (posts.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(posts) { post ->
                    ForumPostItem(post = post, onLike = { viewModel.toggleLikeForumPost(post) })
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Be the first to post a skincare review!")
            }
        }
    }
}

@Composable
fun ForumPostItem(post: ForumPost, onLike: () -> Unit) {
    val avatarColors = listOf(
        Color(0xFFF5C2B3),
        Color(0xFFC5E1A5),
        Color(0xFF90CAF9),
        Color(0xFFFFE082),
        Color(0xFFE1BEE7)
    )
    val avatarColor = avatarColors.getOrElse(post.avatarColorIndex) { Color.LightGray }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Custom gorgeous Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(avatarColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.take(1).uppercase(Locale.getDefault()),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = post.author,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val dateLabel = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(post.timestamp))
                        Text(
                            text = dateLabel,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                    Text(
                        text = post.category,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like action button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLike() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Icon",
                        tint = if (post.likedByMe) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likes} Like(s)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Helpfulness prompt
                Text(
                    text = "Was this helpful?",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SmartwatchSyncScreen(viewModel: SkincareViewModel) {
    val fitnessSync by viewModel.todayFitnessSync.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Fitness Band & Watch Sync",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Connect with smartwatches (Fitbit, Garmin, Apple Watch) to evaluate correlations between sleep cycles, step targets, sweat levels, and skin barriers.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        // Sync Status Graphic Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = "Watch Sync logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Glow Watch Link Active",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (fitnessSync != null && fitnessSync!!.steps > 0) {
                    Text(
                        text = "Last sync completed 1 minute ago.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SyncMetricItem(label = "Daily Steps", value = "${fitnessSync!!.steps}", sub = "Goal: 10,000")
                        SyncMetricItem(label = "Sleep Duration", value = "${fitnessSync!!.sleepHours}h", sub = "Quality: ${fitnessSync!!.sleepQuality}")
                        SyncMetricItem(label = "Active Mins", value = "${fitnessSync!!.activeMinutes}m", sub = "Burn: High")
                    }
                } else {
                    Text(
                        text = "No metrics synced for today. Establish connection to parse steps and sleep cycles.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.syncSmartwatchData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sync_watch_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synchronize Wearable Metrics", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dermatologist correlation guidance
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Cellular Repair Correlation Insights",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Did you know? Consistent deep sleep (7.5+ hours) helps release skin-growth hormones. If sleep falls below 6.5 hours, the skin barrier moisture retention is compromised, resulting in elevated dry score indicators. Keep your smartwatch synced to automate skin diagnostics!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun SyncMetricItem(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(text = sub, fontSize = 10.sp, color = Color.Gray)
    }
}
