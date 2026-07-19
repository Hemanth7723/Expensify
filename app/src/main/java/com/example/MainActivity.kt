package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Expense
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ThemeColors {
    @Composable
    fun background(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF0B1220) else Color(0xFFF8FAFC),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "backgroundColor"
        ).value
    }

    @Composable
    fun surface(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF151F32) else Color(0xFFFFFFFF),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "surfaceColor"
        ).value
    }

    @Composable
    fun border(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "borderColor"
        ).value
    }

    @Composable
    fun textPrimary(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "textPrimaryColor"
        ).value
    }

    @Composable
    fun textSecondary(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "textSecondaryColor"
        ).value
    }

    @Composable
    fun accent(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "accentColor"
        ).value
    }

    @Composable
    fun accentContainer(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "accentContainerColor"
        ).value
    }

    @Composable
    fun onAccentContainer(isDark: Boolean): Color {
        return animateColorAsState(
            targetValue = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "onAccentContainerColor"
        ).value
    }

    @Composable
    fun accentBrush(isDark: Boolean): Brush {
        val color1 = animateColorAsState(
            targetValue = if (isDark) Color(0xFF0EA5E9) else Color(0xFF6366F1),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "brushColor1"
        ).value
        val color2 = animateColorAsState(
            targetValue = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "brushColor2"
        ).value
        return Brush.linearGradient(colors = listOf(color1, color2))
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ExpenseViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppHost(viewModel)
            }
        }
    }
}

@Composable
fun MainAppHost(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    
    // Screen Routing: "splash", "onboarding", "dashboard"
    var currentScreen by remember { mutableStateOf("splash") }
    
    val onboardingCompleted = remember { viewModel.preferencesManager.hasCompletedOnboarding() }

    LaunchedEffect(Unit) {
        // Animate splash loading screen
        delay(2500)
        currentScreen = if (onboardingCompleted) "dashboard" else "onboarding"
    }

    Crossfade(targetState = currentScreen, animationSpec = tween(500), label = "ScreenTransition") { screen ->
        when (screen) {
            "splash" -> SplashScreen()
            "onboarding" -> OnboardingScreen(
                viewModel = viewModel,
                onComplete = { currentScreen = "dashboard" }
            )
            "dashboard" -> MainDashboardView(viewModel = viewModel)
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN & LOGO
// ==========================================
@Composable
fun SplashScreen() {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "SplashLogoPulse")
    
    // Smooth infinite pulsing motion
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0xFF0F172A), Color(0xFF020617)) else listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Visual Logo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .animateContentSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    (if (isDark) Color(0xFF38BDF8) else Color(0xFF6750A4)).copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                radius = size.minDimension * 0.9f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring
                Canvas(modifier = Modifier.size(110.dp)) {
                    drawArc(
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF6750A4),
                        startAngle = 0f,
                        sweepAngle = 280f * scale,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner core finance symbol representation
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEADDFF))
                        .border(1.5.dp, if (isDark) Color(0xFF334155) else Color(0xFFD0BCFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💎",
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text Typography
            Text(
                text = "COSMIC BUDGET",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1C1B1F),
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            
            Text(
                text = "Secure Personal Expense Tracker",
                fontSize = 14.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF49454F),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Linear Progress Indicator representing loader
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE7E0EC)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.35f + (shimmerAlpha * 0.6f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isDark) listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8)) else listOf(Color(0xFF6750A4), Color(0xFF03A9F4))
                            )
                        )
                )
            }
            
            Text(
                text = "Loading Expense Tracker...",
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF64748B) else Color(0xFF79747E),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

// ==========================================
// 2. ONBOARDING SCREEN (PROFILE CONFIG)
// ==========================================
@Composable
fun OnboardingScreen(viewModel: ExpenseViewModel, onComplete: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var bioInput by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = ThemeColors.background(isDark)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome to Cosmic Budget",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.textPrimary(isDark)
                )
                Text(
                    text = "Create your secure financial identity. All information is optional and encrypted locally on your device.",
                    fontSize = 14.sp,
                    color = ThemeColors.textSecondary(isDark),
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Name Input
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name (e.g. Alex)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ThemeColors.accent(isDark)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ThemeColors.accent(isDark)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bio Input
                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it },
                    label = { Text("Short Bio / Savings Target (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = ThemeColors.accent(isDark)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("onboarding_bio_input"),
                    shape = RoundedCornerShape(12.dp)
                )

            }

            Column(modifier = Modifier.padding(top = 32.dp)) {
                Button(
                    onClick = {
                        // Persist credentials encrypted and proceed
                        viewModel.saveProfile(nameInput, emailInput, bioInput, "")
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(ThemeColors.accentBrush(isDark), shape = RoundedCornerShape(14.dp))
                        .testTag("onboarding_confirm_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MAIN DASHBOARD VIEW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardView(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    // Observe DB States
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val allRawExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val activeCurrency by viewModel.currency.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedFilterCat by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    // Speech permissions activity launcher
    var voiceHasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        voiceHasPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Microphone enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone access denied. Voice parsing limited.", Toast.LENGTH_LONG).show()
        }
    }

    // Modal and Panel Controls
    var isProfileOpen by remember { mutableStateOf(false) }
    var isManualAddOpen by remember { mutableStateOf(false) }
    var isVoiceTranscribePanelOpen by remember { mutableStateOf(false) }
    var isCurrencyDropdownOpen by remember { mutableStateOf(false) }

    // On-Demand Spring Entrance Animation trigger scale
    val animateEntrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animateEntrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Scaffold(
        containerColor = ThemeColors.background(isDark),
        topBar = {
            Surface(
                color = ThemeColors.background(isDark),
                border = BorderStroke(1.dp, ThemeColors.border(isDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ThemeColors.accentBrush(isDark)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("E", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Expensify",
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.textPrimary(isDark),
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        // Currency Quick Picker Dropdown Selector
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ThemeColors.surface(isDark))
                                    .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(20.dp))
                                    .clickable { isCurrencyDropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Currency: ", fontSize = 11.sp, color = ThemeColors.textSecondary(isDark))
                                    Text(
                                        text = activeCurrency,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeColors.accent(isDark)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Currency",
                                        tint = ThemeColors.accent(isDark),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isCurrencyDropdownOpen,
                                onDismissRequest = { isCurrencyDropdownOpen = false },
                                modifier = Modifier
                                    .background(ThemeColors.surface(isDark))
                                    .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(8.dp))
                            ) {
                                val list = listOf("₹", "$", "€", "£", "¥", "₩")
                                list.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = curr, 
                                                fontWeight = FontWeight.Medium,
                                                color = ThemeColors.textPrimary(isDark)
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.changeCurrency(curr)
                                            isCurrencyDropdownOpen = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = ThemeColors.textPrimary(isDark)
                                        )
                                    )
                                }
                            }
                        }

                        // Quick theme toggle button
                        IconButton(
                            onClick = { viewModel.toggleDarkTheme(!isDark) },
                            modifier = Modifier.testTag("quick_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = ThemeColors.textSecondary(isDark),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Profile top-right access icon trigger
                        IconButton(
                            onClick = { isProfileOpen = true },
                            modifier = Modifier.testTag("profile_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Edit Profile",
                                tint = ThemeColors.textSecondary(isDark),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ThemeColors.background(isDark)
                    )
                )
            }
        },
        bottomBar = {
            // Sleek dynamic functional panel buttons
            Surface(
                color = ThemeColors.surface(isDark),
                border = BorderStroke(1.dp, ThemeColors.border(isDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Manual Addition quick button (Primary)
                    Button(
                        onClick = { isManualAddOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(ThemeColors.accentBrush(isDark), shape = RoundedCornerShape(12.dp))
                            .testTag("add_manual_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Expense Manually", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Dashboard Container
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Greeting Ribbon Card
                item {
                    ProfileRibbonCard(viewModel, animateEntrance.value)
                }



                // Interactive Chart Analytics Tabs and Cards
                item {
                    DashboardMetricsPanel(
                        viewModel = viewModel,
                        expenses = allRawExpenses,
                        currency = activeCurrency,
                        scaleFactor = animateEntrance.value
                    )
                }

                // Transaction Ledger Header Title (Filters, Searches, and Export XLS trigger)
                item {
                    LedgerHeaderArea(
                        viewModel = viewModel,
                        expensesCount = expenses.size,
                        selectedCategory = selectedFilterCat,
                        sortOrder = sortOrder,
                        onExportTrigger = {
                            val xlsxData = generateXlsxData(expenses, activeCurrency)
                            downloadLedgerFile(context, xlsxData)
                        }
                    )
                }

                // Sorted Expenses Area
                if (expenses.isEmpty()) {
                    item {
                        EmptyStateCard(isDark = isDark)
                    }
                } else {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseRowItem(
                            expense = expense,
                            currency = activeCurrency,
                            viewModel = viewModel
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }

            // =========================
            // DIALOG Modals & Overlays
            // =========================
            // Profile Edit Dialog Modal
            if (isProfileOpen) {
                ProfileEditModal(
                    viewModel = viewModel,
                    onDismiss = { isProfileOpen = false }
                )
            }

            // Manual Expense Entry Modal
            if (isManualAddOpen) {
                ManualAddExpenseDialog(
                    viewModel = viewModel,
                    currency = activeCurrency,
                    onDismiss = { isManualAddOpen = false }
                )
            }


        }
    }
}

// ==========================================
// COMPONENT: PROFILE BANNER
// ==========================================
@Composable
fun ProfileRibbonCard(viewModel: ExpenseViewModel, scale: Float) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val name = if (viewModel.userName.trim().isEmpty()) "Explorer" else viewModel.userName
    val bio = if (viewModel.userBio.trim().isEmpty()) "Budgeting with dynamic currency tracking." else viewModel.userBio

    Card(
        colors = CardDefaults.cardColors(containerColor = ThemeColors.surface(isDark)),
        border = BorderStroke(1.dp, ThemeColors.border(isDark)),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = scale),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(ThemeColors.accentContainer(isDark))
                    .border(1.5.dp, ThemeColors.border(isDark), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(2).uppercase(Locale.ROOT),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ThemeColors.onAccentContainer(isDark)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello, $name 👋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.textPrimary(isDark)
                )
                Text(
                    text = bio,
                    fontSize = 12.sp,
                    color = ThemeColors.textSecondary(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// COMPONENT: AI SPENDING CULTURE INSIGHT
// ==========================================
@Composable
fun AiInsightCard(viewModel: ExpenseViewModel, currency: String) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ThemeColors.surface(isDark)),
        border = BorderStroke(1.dp, ThemeColors.border(isDark)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ThemeColors.accentContainer(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gemini Budget Analyst",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.textPrimary(isDark)
                    )
                    Text(
                        text = "Witty Spending Culture Review",
                        fontSize = 11.sp,
                        color = ThemeColors.accent(isDark),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Analyze button
                Button(
                    onClick = {
                        viewModel.genAiInsights()
                        isExpanded = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.accent(isDark)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !viewModel.isGeneratingInsights,
                    modifier = Modifier.testTag("activate_ai_insights_btn")
                ) {
                    if (viewModel.isGeneratingInsights) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Analyze", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Results Display block
            if (viewModel.aiInsights != null || viewModel.isGeneratingInsights || viewModel.aiInsightError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ThemeColors.background(isDark))
                        .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        if (viewModel.isGeneratingInsights) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ThemeColors.accent(isDark))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("AI is writing financial critique...", fontSize = 12.sp, color = ThemeColors.textSecondary(isDark))
                            }
                        } else if (viewModel.aiInsightError != null) {
                            Text(
                                text = "⚠️ ${viewModel.aiInsightError}",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = viewModel.aiInsights ?: "",
                                fontSize = 13.sp,
                                color = ThemeColors.textPrimary(isDark),
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .clickable { isExpanded = !isExpanded },
                                maxLines = if (isExpanded) 100 else 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = if (isExpanded) "Show Less 🔺" else "Expand Insight Review 🔻",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.accent(isDark),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// METRICS PANEL WITH CANVAS CUSTOM CHARTS
// ==========================================
@Composable
fun DashboardMetricsPanel(
    viewModel: ExpenseViewModel,
    expenses: List<Expense>,
    currency: String,
    scaleFactor: Float
) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    // Interactive Tab category: 0 -> By Week, 1 -> By Month, 2 -> By Year, 3 -> By Category
    var activeChartTab by remember { mutableStateOf(0) }
    
    // Note: No budget threshold concept exists per instruction ("Remove threshold feature")
    
    val totalSpen = viewModel.calculateTotalSpend(expenses)

    Column(modifier = Modifier.fillMaxWidth()) {
        // High visibility display metric card (Hero Purple/Blue Card with Brush)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp))
                .background(ThemeColors.accentBrush(isDark), shape = RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "TOTAL EXPENSES",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFFE0F2FE) else Color(0xFFEADDFF),
                    letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Big Dynamic Currency-backed Figure text representation
                Text(
                    text = "$currency${String.format(Locale.US, "%,.2f", totalSpen)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Box(
                //     modifier = Modifier
                //         .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                //         .padding(horizontal = 10.dp, vertical = 4.dp)
                // ) {
                //     // Text(
                //     //     text = "Reflected instantly across ${expenses.size} ledger entries",
                //     //     color = Color.White,
                //     //     fontSize = 11.sp,
                //     //     fontWeight = FontWeight.Medium
                //     // )
                // }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Navigation Tab Bar for analytics groupings (Sleek dynamic capsule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(ThemeColors.surface(isDark))
                .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(100.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabsLabels = listOf("Daily", "Weekly", "Monthly", "Yearly", "Categories")
            tabsLabels.forEachIndexed { idx, label ->
                val active = activeChartTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (active) (if (isDark) Color(0xFF2C2C2C) else Color.White) else Color.Transparent)
                        .clickable { activeChartTab = idx }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) ThemeColors.accent(isDark) else ThemeColors.textSecondary(isDark)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart display box
        Card(
            colors = CardDefaults.cardColors(containerColor = ThemeColors.surface(isDark)),
            border = BorderStroke(1.dp, ThemeColors.border(isDark)),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = scaleFactor, scaleY = scaleFactor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Spending Density Analytics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ThemeColors.textPrimary(isDark)
                )

                Spacer(modifier = Modifier.height(20.dp))

                when (activeChartTab) {
                    0 -> DailySegmentChart(viewModel, expenses, currency)
                    1 -> WeeklySegmentChart(viewModel, expenses, currency)
                    2 -> MonthlySegmentChart(viewModel, expenses, currency)
                    3 -> YearlySegmentChart(viewModel, expenses, currency)
                    4 -> CategorySegmentChart(viewModel, expenses, currency)
                }
            }
        }
    }
}

// ==========================================
// DENSITY CHARTS SECTION
// ==========================================
// ==========================================
// DAILY SEGMENT STAGGERED CHART COMPOSABLE
// ==========================================
@Composable
fun DailySegmentChart(viewModel: ExpenseViewModel, expenses: List<Expense>, currency: String) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val map = viewModel.groupByDay(expenses)
    if (map.isEmpty()) {
        ChartEmptyState()
    } else {
        val sortedEntries = map.entries.toList().takeLast(6)
        val maxSpend = sortedEntries.maxOfOrNull { it.value } ?: 1.0

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sortedEntries.forEach { entry ->
                    val ratio = entry.value / maxSpend
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "$currency${entry.value.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.accent(isDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(ratio.toFloat().coerceAtLeast(0.06f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
                                        } else {
                                            listOf(Color(0xFFB39DDB), Color(0xFF6750A4))
                                        }
                                    )
                                )
                        )
                    }
                }
            }

            // Smooth continuous horizontal baseline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                sortedEntries.forEach { entry ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.key,
                            fontSize = 10.sp,
                            color = ThemeColors.textSecondary(isDark),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// WEEKLY SEGMENT STAGGERED CHART COMPOSABLE
// ==========================================
@Composable
fun WeeklySegmentChart(viewModel: ExpenseViewModel, expenses: List<Expense>, currency: String) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val map = viewModel.groupByWeek(expenses)
    if (map.isEmpty()) {
        ChartEmptyState()
    } else {
        val sortedEntries = map.entries.toList().takeLast(5)
        val maxSpend = sortedEntries.maxOfOrNull { it.value } ?: 1.0

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sortedEntries.forEach { entry ->
                    val ratio = entry.value / maxSpend
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "$currency${entry.value.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.accent(isDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .fillMaxHeight(ratio.toFloat().coerceAtLeast(0.06f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
                                        } else {
                                            listOf(Color(0xFFB39DDB), Color(0xFF6750A4))
                                        }
                                    )
                                )
                        )
                    }
                }
            }

            // Smooth continuous horizontal baseline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                sortedEntries.forEach { entry ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.key,
                            fontSize = 9.sp,
                            color = ThemeColors.textSecondary(isDark),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// MONTHLY SEGMENT STAGGERED CHART COMPOSABLE
// ==========================================
@Composable
fun MonthlySegmentChart(viewModel: ExpenseViewModel, expenses: List<Expense>, currency: String) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val map = viewModel.groupByMonth(expenses)
    if (map.isEmpty()) {
        ChartEmptyState()
    } else {
        val sortedEntries = map.entries.toList().takeLast(6)
        val maxSpend = sortedEntries.maxOfOrNull { it.value } ?: 1.0

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sortedEntries.forEach { entry ->
                    val ratio = entry.value / maxSpend
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "$currency${entry.value.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.accent(isDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(ratio.toFloat().coerceAtLeast(0.06f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
                                        } else {
                                            listOf(Color(0xFFB39DDB), Color(0xFF6750A4))
                                        }
                                    )
                                )
                        )
                    }
                }
            }

            // Smooth continuous horizontal baseline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                sortedEntries.forEach { entry ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.key,
                            fontSize = 10.sp,
                            color = ThemeColors.textSecondary(isDark),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// YEARLY SEGMENT STAGGERED CHART COMPOSABLE
// ==========================================
@Composable
fun YearlySegmentChart(viewModel: ExpenseViewModel, expenses: List<Expense>, currency: String) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val map = viewModel.groupByYear(expenses)
    if (map.isEmpty()) {
        ChartEmptyState()
    } else {
        val sortedEntries = map.entries.toList().takeLast(6)
        val maxSpend = sortedEntries.maxOfOrNull { it.value } ?: 1.0

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                sortedEntries.forEach { entry ->
                    val ratio = entry.value / maxSpend
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "$currency${entry.value.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.accent(isDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight(ratio.toFloat().coerceAtLeast(0.06f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
                                        } else {
                                            listOf(Color(0xFFB39DDB), Color(0xFF6750A4))
                                        }
                                    )
                                )
                        )
                    }
                }
            }

            // Smooth continuous horizontal baseline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                sortedEntries.forEach { entry ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.key,
                            fontSize = 11.sp,
                            color = ThemeColors.textSecondary(isDark),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SEGMENTED CATEGORY PIE BAR CHART
// ==========================================
@Composable
fun CategorySegmentChart(viewModel: ExpenseViewModel, expenses: List<Expense>, currency: String) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Sorting Option state: "Daily", "Weekly", "Monthly", "Yearly"
    var catFilterType by remember { mutableStateOf("Weekly") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val calendarSelected = Calendar.getInstance().apply { timeInMillis = selectedDate }

    // Filter dynamic expense list
    val filteredList = remember(expenses, catFilterType, selectedDate) {
        expenses.filter { expense ->
            val expenseCal = Calendar.getInstance().apply { timeInMillis = expense.timestamp }
            when (catFilterType) {
                "Daily" -> {
                    expenseCal.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                    expenseCal.get(Calendar.DAY_OF_YEAR) == calendarSelected.get(Calendar.DAY_OF_YEAR)
                }
                "Weekly" -> {
                    expenseCal.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                    expenseCal.get(Calendar.WEEK_OF_YEAR) == calendarSelected.get(Calendar.WEEK_OF_YEAR)
                }
                "Monthly" -> {
                    expenseCal.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR) &&
                    expenseCal.get(Calendar.MONTH) == calendarSelected.get(Calendar.MONTH)
                }
                "Yearly" -> {
                    expenseCal.get(Calendar.YEAR) == calendarSelected.get(Calendar.YEAR)
                }
                else -> true
            }
        }
    }

    val map = viewModel.groupByCategory(filteredList)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Horizontal option chips/capsule selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filterOptions = listOf("Daily", "Weekly", "Monthly", "Yearly")
            filterOptions.forEach { opt ->
                val active = catFilterType == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) ThemeColors.accent(isDark) else Color.Transparent)
                        .clickable { catFilterType = opt }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else ThemeColors.textSecondary(isDark)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date and period display area with chevron/arrow increment & decrement buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                    when (catFilterType) {
                        "Daily" -> cal.add(Calendar.DAY_OF_YEAR, -1)
                        "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, -1)
                        "Monthly" -> cal.add(Calendar.MONTH, -1)
                        "Yearly" -> cal.add(Calendar.YEAR, -1)
                    }
                    selectedDate = cal.timeInMillis
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Interval",
                    tint = ThemeColors.accent(isDark)
                )
            }

            val intervalLabel = remember(catFilterType, selectedDate) {
                when (catFilterType) {
                    "Daily" -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(selectedDate))
                    "Weekly" -> {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                        val start = SimpleDateFormat("MMM d", Locale.US).format(cal.time)
                        cal.add(Calendar.DAY_OF_WEEK, 6)
                        val end = SimpleDateFormat("MMM d, yyyy", Locale.US).format(cal.time)
                        "$start - $end"
                    }
                    "Monthly" -> SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(selectedDate))
                    "Yearly" -> SimpleDateFormat("yyyy", Locale.US).format(Date(selectedDate))
                    else -> ""
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ThemeColors.accent(isDark),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = intervalLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.textPrimary(isDark)
                )
            }

            IconButton(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                    when (catFilterType) {
                        "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                        "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                        "Monthly" -> cal.add(Calendar.MONTH, 1)
                        "Yearly" -> cal.add(Calendar.YEAR, 1)
                    }
                    selectedDate = cal.timeInMillis
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Interval",
                    tint = ThemeColors.accent(isDark)
                )
            }
        }

        if (showDatePicker) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DisposableEffect(Unit) {
                val dialog = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCal = Calendar.getInstance()
                        newCal.set(Calendar.YEAR, year)
                        newCal.set(Calendar.MONTH, month)
                        newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        selectedDate = newCal.timeInMillis
                        showDatePicker = false
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                dialog.setOnDismissListener { showDatePicker = false }
                dialog.show()
                onDispose {
                    dialog.dismiss()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (map.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No category ledger records for this period",
                    fontSize = 12.sp,
                    color = ThemeColors.textSecondary(isDark)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val maxSum = map.values.maxOfOrNull { it } ?: 1.0
                map.entries.sortedByDescending { it.value }.take(5).forEach { entry ->
                    val (ic, cColor) = viewModel.getCategoryIconAndColor(entry.key)
                    val ratio = entry.value / maxSum

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = ic, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = entry.key,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ThemeColors.textPrimary(isDark)
                                )
                            }
                            Text(
                                text = "$currency${String.format(Locale.US, "%,.2f", entry.value)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.textPrimary(isDark)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Horizontal Level visual bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(ThemeColors.border(isDark))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio.toFloat())
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(cColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(
                1.dp,
                Color(0xFFE7E0EC),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Insufficient ledger data to build metrics", fontSize = 12.sp, color = Color(0xFF79747E))
        }
    }
}

// ==========================================
// COMPONENT: LEDGER HEAD AREA
// ==========================================
@Composable
fun LedgerHeaderArea(
    viewModel: ExpenseViewModel,
    expensesCount: Int,
    selectedCategory: String,
    sortOrder: String,
    onExportTrigger: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    var isSortDropdownOpen by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRANSACTION LEDGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.accent(isDark),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Showing $expensesCount sorted entries",
                    fontSize = 12.sp,
                    color = ThemeColors.textSecondary(isDark)
                )
            }

            // Elegant Download spreadsheet card button
            Button(
                onClick = onExportTrigger,
                colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.accentContainer(isDark)),
                border = BorderStroke(1.dp, ThemeColors.border(isDark)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("download_ledger_xls_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = ThemeColors.onAccentContainer(isDark), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download XLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.onAccentContainer(isDark)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Filters Horizontal Toolbar segment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort Selector Box Trigger
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ThemeColors.surface(isDark))
                        .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(12.dp))
                        .clickable { isSortDropdownOpen = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sort, contentDescription = null, tint = ThemeColors.accent(isDark), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sort: $sortOrder", fontSize = 11.sp, color = ThemeColors.textPrimary(isDark), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Sort Option", tint = ThemeColors.accent(isDark), modifier = Modifier.size(14.dp))
                    }
                }

                DropdownMenu(
                    expanded = isSortDropdownOpen,
                    onDismissRequest = { isSortDropdownOpen = false },
                    modifier = Modifier
                        .background(ThemeColors.surface(isDark))
                        .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(8.dp))
                ) {
                    val sortOptions = listOf("Newest", "Oldest", "Amount High", "Amount Low")
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = option, 
                                    fontWeight = FontWeight.Medium,
                                    color = ThemeColors.textPrimary(isDark)
                                ) 
                            },
                            onClick = {
                                viewModel.changeSortOrder(option)
                                isSortDropdownOpen = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = ThemeColors.textPrimary(isDark)
                            )
                        )
                    }
                }
            }

            // Categories filters
            val filterOptions = listOf("All") + viewModel.categories
            filterOptions.forEach { cat ->
                val selected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) ThemeColors.accent(isDark) else ThemeColors.surface(isDark))
                        .border(1.dp, if (selected) ThemeColors.accent(isDark) else ThemeColors.border(isDark), RoundedCornerShape(12.dp))
                        .clickable { viewModel.changeCategoryFilter(cat) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else ThemeColors.textPrimary(isDark)
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: EXPENSE SINGLE ROW ITEM ROW
// ==========================================
@Composable
fun ExpenseRowItem(
    expense: Expense,
    currency: String,
    viewModel: ExpenseViewModel
) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val (ic, iconColor) = viewModel.getCategoryIconAndColor(expense.category)

    var showActionSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActionSheet = true }
            .testTag("expense_row_card_${expense.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeColors.surface(isDark)),
        border = BorderStroke(1.dp, ThemeColors.border(isDark))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Rounded Area
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f))
                    .border(1.dp, iconColor.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = ic, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ThemeColors.textPrimary(isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${expense.category} • ${formatter.format(Date(expense.timestamp))}",
                    fontSize = 11.sp,
                    color = ThemeColors.textSecondary(isDark)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                // Strictly updates and maps currency instantly based on preference!
                Text(
                    text = "$currency${String.format(Locale.US, "%,.2f", expense.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ThemeColors.accent(isDark)
                )
                
                if (expense.note.trim().isNotEmpty()) {
                    Text(
                        text = expense.note,
                        fontSize = 10.sp,
                        color = ThemeColors.textSecondary(isDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 100.dp)
                    )
                }
            }
        }
    }

    if (showActionSheet) {
        AlertDialog(
            onDismissRequest = { showActionSheet = false },
            title = { Text("Manage Transaction Details", color = ThemeColors.textPrimary(isDark), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Title: ${expense.title}", color = ThemeColors.textSecondary(isDark), fontSize = 14.sp)
                    Text(text = "Cost: $currency${expense.amount}", color = ThemeColors.textSecondary(isDark), fontSize = 14.sp)
                    Text(text = "Category: ${expense.category}", color = ThemeColors.textSecondary(isDark), fontSize = 14.sp)
                    if (expense.note.trim().isNotEmpty()) {
                        Text(text = "Context: ${expense.note}", color = ThemeColors.textSecondary(isDark), fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense)
                        showActionSheet = false
                    }
                ) {
                    Text("Delete Item", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionSheet = false }) {
                    Text("Dismiss", color = ThemeColors.accent(isDark))
                }
            },
            containerColor = ThemeColors.surface(isDark)
        )
    }
}

// ==========================================
// EMPTY STATE COMPOSABLE
// ==========================================
@Composable
fun EmptyStateCard(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(ThemeColors.surface(isDark))
                .border(1.dp, ThemeColors.border(isDark), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("💸", fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No recorded transactions yet",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = ThemeColors.textPrimary(isDark)
        )
        Text(
            text = "Use the bottom microphone helper to speak an expense or add one manually to analyze details.",
            fontSize = 12.sp,
            color = ThemeColors.textSecondary(isDark),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 6.dp)
        )
    }
}

// ==========================================
// ==========================================
// OVERLAY MODAL: SECURE PROFILE CONSOLE
// ==========================================
@Composable
fun ProfileEditModal(viewModel: ExpenseViewModel, onDismiss: () -> Unit) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    var nameField by remember { mutableStateOf(viewModel.userName) }
    var emailField by remember { mutableStateOf(viewModel.userEmail) }
    var bioField by remember { mutableStateOf(viewModel.userBio) }
    var keyField by remember { mutableStateOf(viewModel.customApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Secure Identity Controls",
                color = ThemeColors.textPrimary(isDark),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = emailField,
                    onValueChange = { emailField = it },
                    label = { Text("Email (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bioField,
                    onValueChange = { bioField = it },
                    label = { Text("Short Bio Target") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // High Quality Theme Toggle Row (Custom styling per guidelines)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ThemeColors.background(isDark))
                        .border(1.dp, ThemeColors.border(isDark), RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleDarkTheme(!isDark) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme Icon",
                            tint = ThemeColors.accent(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "App Theme",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.textPrimary(isDark)
                            )
                            Text(
                                text = if (isDark) "Dark Mode Enabled" else "Light Mode Enabled",
                                fontSize = 10.sp,
                                color = ThemeColors.textSecondary(isDark)
                            )
                        }
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { checked -> viewModel.toggleDarkTheme(checked) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ThemeColors.accent(isDark),
                            uncheckedThumbColor = ThemeColors.textSecondary(isDark),
                            uncheckedTrackColor = ThemeColors.border(isDark)
                        )
                    )
                }

                Text(
                    text = "⚠ Profile details are stored securely on device.",
                    fontSize = 10.sp,
                    color = ThemeColors.accent(isDark)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.saveProfile(nameField, emailField, bioField, "")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.background(ThemeColors.accentBrush(isDark), shape = RoundedCornerShape(12.dp))
            ) {
                Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ThemeColors.accent(isDark))
            }
        },
        containerColor = ThemeColors.surface(isDark)
    )
}

// ==========================================
// OVERLAY MODAL: MANUAL ADDITION FORM
// ==========================================
@Composable
fun ManualAddExpenseDialog(
    viewModel: ExpenseViewModel,
    currency: String,
    onDismiss: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tInput by remember { mutableStateOf("") }
    var aInput by remember { mutableStateOf("") }
    var nInput by remember { mutableStateOf("") }
    
    // Choose Category Selector
    var selectedCatIdx by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Transaction Ledger", color = ThemeColors.textPrimary(isDark), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tInput,
                    onValueChange = { tInput = it },
                    label = { Text("Title (e.g. Electric Bill)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = aInput,
                    onValueChange = { aInput = it },
                    label = { Text("Cost Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category select
                Text("Select Category:", fontSize = 12.sp, color = ThemeColors.textSecondary(isDark), fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    viewModel.categories.forEachIndexed { idx, cat ->
                        val active = idx == selectedCatIdx
                        val (ic, cColor) = viewModel.getCategoryIconAndColor(cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) cColor else ThemeColors.surface(isDark))
                                .border(1.dp, if (active) cColor else ThemeColors.border(isDark), RoundedCornerShape(12.dp))
                                .clickable { selectedCatIdx = idx }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "$ic $cat", fontSize = 12.sp, color = if (active) Color.White else ThemeColors.textPrimary(isDark))
                        }
                    }
                }

                OutlinedTextField(
                    value = nInput,
                    onValueChange = { nInput = it },
                    label = { Text("Notes (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = tInput.trim()
                    val amount = aInput.trim().toDoubleOrNull() ?: 0.0
                    if (trimmedTitle.isEmpty()) {
                        Toast.makeText(context, "Please enter a transaction title.", Toast.LENGTH_SHORT).show()
                    } else if (amount <= 0) {
                        Toast.makeText(context, "Please enter a valid positive cost amount.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addExpense(
                             title = trimmedTitle,
                             amount = amount,
                             category = viewModel.categories[selectedCatIdx],
                             note = nInput,
                             date = System.currentTimeMillis()
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.background(ThemeColors.accentBrush(isDark), shape = RoundedCornerShape(12.dp))
            ) {
                Text("Save Ledger", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ThemeColors.accent(isDark))
            }
        },
        containerColor = ThemeColors.surface(isDark)
    )
}

// ==========================================
// VOICE AND SPEECH SYNTACTIC CONVERTER PANEL
// ==========================================
@Composable
fun VoiceSpeechParserPanel(
    viewModel: ExpenseViewModel,
    activeCurrency: String,
    onDismiss: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val helper = viewModel.voiceHelper
    val isListening by helper.isListening.collectAsStateWithLifecycle()
    val spokenText by helper.spokenText.collectAsStateWithLifecycle()
    val recordError by helper.error.collectAsStateWithLifecycle()

    var customTextInput by remember { mutableStateOf("") }

    // Hold extracted preview
    var previewTitle by remember { mutableStateOf("") }
    var previewAmount by remember { mutableStateOf(0.0) }
    var previewCategory by remember { mutableStateOf("") }
    var previewNote by remember { mutableStateOf("") }
    var hasParsedPreview by remember { mutableStateOf(false) }

    LaunchedEffect(spokenText) {
        if (spokenText.isNotEmpty()) {
            customTextInput = spokenText
        }
    }

    // Spring scaling for recording circle
    val recordingPulse = rememberInfiniteTransition(label = "PulseRec")
    val recScale by recordingPulse.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RecScale"
    )

    AlertDialog(
        onDismissRequest = {
            helper.cancel()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = ThemeColors.accent(isDark))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Smart Voice Logging", color = ThemeColors.textPrimary(isDark), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Speak or write your transaction describing what you spent. E.g., 'Spent ${activeCurrency}45 at Walmart for fresh groceries'",
                    fontSize = 12.sp,
                    color = ThemeColors.textSecondary(isDark),
                    textAlign = TextAlign.Center
                )

                // MICROPHONE INTERACTIVE WAVE RIPPLE SPHERE
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Color(0xFFEF4444).copy(alpha = 0.15f)
                            else ThemeColors.surface(isDark)
                        )
                        .border(
                            1.dp,
                            if (isListening) Color(0xFFEF4444) else ThemeColors.border(isDark),
                            CircleShape
                        )
                        .clickable {
                            if (isListening) {
                                helper.stopListening()
                            } else {
                                helper.startListening(context)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer(
                                scaleX = if (isListening) recScale else 1.0f,
                                scaleY = if (isListening) recScale else 1.0f
                            )
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFFEF4444) else ThemeColors.accentContainer(isDark)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Trigger Speech",
                            tint = if (isListening) Color.White else ThemeColors.onAccentContainer(isDark),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                if (isListening) {
                    Text(
                        text = "Recording Voice Tone • Speak Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }

                if (!recordError.isNullOrEmpty()) {
                    Text(
                        text = "⚠️ $recordError",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Sentence Transcript preview box
                OutlinedTextField(
                    value = customTextInput,
                    onValueChange = { customTextInput = it },
                    label = { Text("Spoken Transcript Sentence") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ThemeColors.textPrimary(isDark),
                        unfocusedTextColor = ThemeColors.textPrimary(isDark),
                        focusedLabelColor = ThemeColors.accent(isDark),
                        unfocusedLabelColor = ThemeColors.textSecondary(isDark),
                        focusedBorderColor = ThemeColors.accent(isDark),
                        unfocusedBorderColor = ThemeColors.border(isDark)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("voice_transcript_text"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            viewModel.parseTranscriptToExpense(customTextInput) { tStr, aVal, cStr, nStr ->
                                previewTitle = tStr
                                previewAmount = aVal
                                previewCategory = cStr
                                previewNote = nStr
                                hasParsedPreview = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = customTextInput.isNotBlank() && !viewModel.isAiParsingVoice,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .background(
                                brush = if (customTextInput.isNotBlank() && !viewModel.isAiParsingVoice) {
                                    ThemeColors.accentBrush(isDark)
                                } else {
                                    SolidColor(ThemeColors.border(isDark))
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("submit_to_ai_parse_btn")
                    ) {
                        if (viewModel.isAiParsingVoice) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Extract", color = Color.White)
                            }
                        }
                    }
                }

                if (viewModel.voiceParsingError != null) {
                    Text(
                        text = "Parsing error: ${viewModel.voiceParsingError}",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // AI PREVIEW OUTCOME CARD
                if (hasParsedPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ThemeColors.accentContainer(isDark).copy(alpha = 0.15f))
                            .border(1.2.dp, ThemeColors.accent(isDark), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Extracted Preview",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeColors.accent(isDark)
                                )
                                Text(
                                    text = "$activeCurrency${String.format(Locale.US, "%.2f", previewAmount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ThemeColors.accent(isDark)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(text = "Title: $previewTitle", fontSize = 14.sp, color = ThemeColors.textPrimary(isDark), fontWeight = FontWeight.Bold)
                            Text(text = "Category: $previewCategory", fontSize = 13.sp, color = ThemeColors.textSecondary(isDark))
                            if (previewNote.isNotEmpty()) {
                                Text(text = "Note: $previewNote", fontSize = 12.sp, color = ThemeColors.textSecondary(isDark))
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    // Save the parsed expense
                                    viewModel.addExpense(
                                        title = previewTitle,
                                        amount = previewAmount,
                                        category = previewCategory,
                                        note = previewNote,
                                        date = System.currentTimeMillis()
                                    )
                                    helper.cancel()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ThemeColors.accentBrush(isDark), shape = RoundedCornerShape(12.dp))
                                    .testTag("save_ai_extracted_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm & Save to Ledger", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    helper.cancel()
                    onDismiss()
                }
            ) {
                Text("Dismiss", color = ThemeColors.accent(isDark))
            }
        },
        containerColor = ThemeColors.surface(isDark)
    )
}

// ==========================================
// EXPORTING AND DOWNLOADING HANDLER (.XLSX)
// ==========================================
fun generateXlsxData(expenses: List<Expense>, currencySymbol: String): ByteArray {
    val bos = ByteArrayOutputStream()
    val zos = ZipOutputStream(bos)

    // Helper to write file inside zip
    fun writeEntry(name: String, content: String) {
        zos.putNextEntry(ZipEntry(name))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    // 1. [Content_Types].xml
    writeEntry("[Content_Types].xml", """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        </Types>
    """.trimIndent())

    // 2. _rels/.rels
    writeEntry("_rels/.rels", """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent())

    // 3. xl/_rels/workbook.xml.rels
    writeEntry("xl/_rels/workbook.xml.rels", """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
        </Relationships>
    """.trimIndent())

    // 4. xl/workbook.xml
    writeEntry("xl/workbook.xml", """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
            <sheets>
                <sheet name="Expenses" sheetId="1" r:id="rId1"/>
            </sheets>
        </workbook>
    """.trimIndent())

    // 5. xl/worksheets/sheet1.xml
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
    sb.append("<sheetData>")

    // Headers Row (ID, Date, Category, Title, Amount, Currency, Notes)
    sb.append("<row r=\"1\">")
    val headers = listOf("ID", "Date", "Category", "Title", "Amount", "Currency", "Notes")
    headers.forEachIndexed { colIdx, header ->
        val colLetter = ('A' + colIdx).toString()
        sb.append("<c r=\"${colLetter}1\" t=\"inline\"><is><t>${xmlEscape(header)}</t></is></c>")
    }
    sb.append("</row>")

    // Data Rows
    expenses.forEachIndexed { index, expense ->
        val rIdx = index + 2
        val dateStr = df.format(Date(expense.timestamp))
        sb.append("<row r=\"$rIdx\">")
        
        // Col A: ID
        sb.append("<c r=\"A$rIdx\"><v>${expense.id}</v></c>")
        // Col B: Date
        sb.append("<c r=\"B$rIdx\" t=\"inline\"><is><t>${xmlEscape(dateStr)}</t></is></c>")
        // Col C: Category
        sb.append("<c r=\"C$rIdx\" t=\"inline\"><is><t>${xmlEscape(expense.category)}</t></is></c>")
        // Col D: Title
        sb.append("<c r=\"D$rIdx\" t=\"inline\"><is><t>${xmlEscape(expense.title)}</t></is></c>")
        // Col E: Amount
        sb.append("<c r=\"E$rIdx\"><v>${String.format(Locale.US, "%.2f", expense.amount)}</v></c>")
        // Col F: Currency
        sb.append("<c r=\"F$rIdx\" t=\"inline\"><is><t>${xmlEscape(currencySymbol)}</t></is></c>")
        // Col G: Notes
        sb.append("<c r=\"G$rIdx\" t=\"inline\"><is><t>${xmlEscape(expense.note)}</t></is></c>")
        
        sb.append("</row>")
    }

    sb.append("</sheetData>")
    sb.append("</worksheet>")

    writeEntry("xl/worksheets/sheet1.xml", sb.toString())

    zos.flush()
    zos.close()
    return bos.toByteArray()
}

fun xmlEscape(value: String): String {
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

fun downloadLedgerFile(context: Context, xlsxData: ByteArray) {
    val fileName = "Expensify_Budget_Report_${System.currentTimeMillis()}.xlsx"
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(xlsxData)
                }
                Toast.makeText(context, "Spreadsheet downloaded to Downloads folder", Toast.LENGTH_LONG).show()
            } else {
                throw Exception("Failed to create MediaStore entry")
            }
        } else {
            // Pre-Q: direct file access to Environment.DIRECTORY_DOWNLOADS
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            file.writeBytes(xlsxData)
            Toast.makeText(context, "Spreadsheet downloaded: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        // Fallback: If downloads folder access fails, we can write to app cache directory and trigger an intent/toast
        try {
            val cacheDir = context.cacheDir
            val fallbackFile = File(cacheDir, fileName)
            fallbackFile.writeBytes(xlsxData)
            
            // Create a file sharing chooser as a robust fallback
            val uri = FileProvider.getUriForFile(
                context,
                "com.aistudio.dailyexpensetracker.kxmpzq.fileprovider",
                fallbackFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_SUBJECT, "Budget Report Analysis")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Download failed, sharing instead"))
        } catch (fallbackEx: Exception) {
            Toast.makeText(context, "Export failure: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
