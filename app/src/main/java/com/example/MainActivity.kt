package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AkyolInventoryTheme {
                val viewModel: InventoryViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val userMessage by viewModel.userMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Display Toast / Snackbar messages
                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearMessage()
                    }
                }

                // Handle back press
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    when (currentScreen) {
                        AppScreen.ASSET_DETAIL -> viewModel.navigateTo(AppScreen.ASSET_LIST)
                        AppScreen.ASSET_ADD_EDIT, AppScreen.AI_CONFIRMATION -> viewModel.navigateTo(AppScreen.ASSET_LIST)
                        else -> viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = CardSurfaceElevated,
                                contentColor = TextPrimary,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        AkyolBottomNavigation(
                            currentScreen = currentScreen,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    },
                    containerColor = BackgroundDark,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AppScreen.ASSET_LIST -> AssetListScreen(viewModel = viewModel)
                            AppScreen.ASSET_DETAIL -> AssetDetailScreen(viewModel = viewModel)
                            AppScreen.ASSET_ADD_EDIT -> AssetAddEditScreen(viewModel = viewModel)
                            AppScreen.AI_CONFIRMATION -> AiConfirmationScreen(viewModel = viewModel)
                            AppScreen.QR_SCANNER -> QrScannerScreen(viewModel = viewModel)
                            AppScreen.AUDIT -> AuditScreen(viewModel = viewModel)
                            AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                            AppScreen.EMPLOYEES -> EmployeesScreen(viewModel = viewModel)
                            AppScreen.PROFILE_SETTINGS -> ProfileSettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AkyolBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    Surface(
        color = SurfaceDark,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BorderDark,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .navigationBarsPadding()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            BottomNavItem(
                title = "Ana Sayfa",
                icon = Icons.Default.Dashboard,
                isSelected = currentScreen == AppScreen.DASHBOARD,
                onClick = { onNavigate(AppScreen.DASHBOARD) }
            )

            BottomNavItem(
                title = "Demirbaşlar",
                icon = Icons.Default.Inventory2,
                isSelected = currentScreen == AppScreen.ASSET_LIST,
                onClick = { onNavigate(AppScreen.ASSET_LIST) }
            )

            // Prominent Central Camera / AI Scan Button (Requirement 6)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .clickable { onNavigate(AppScreen.ASSET_ADD_EDIT) }
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(AccentTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Kamera / AI Ekle",
                        tint = BackgroundDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Kamera",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )
            }

            BottomNavItem(
                title = "QR Tara",
                icon = Icons.Default.QrCodeScanner,
                isSelected = currentScreen == AppScreen.QR_SCANNER,
                onClick = { onNavigate(AppScreen.QR_SCANNER) }
            )

            BottomNavItem(
                title = "Profil",
                icon = Icons.Default.Person,
                isSelected = currentScreen == AppScreen.PROFILE_SETTINGS,
                onClick = { onNavigate(AppScreen.PROFILE_SETTINGS) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 54.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) AccentTeal else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}
