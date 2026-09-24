package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkyolInventoryTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val userMessage by viewModel.userMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
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
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        // Show bottom navigation bar on all top-level screens
                        AkyolBottomNavigation(
                            currentScreen = currentScreen,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    },
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
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

            // Prominent Central Camera / AI Scan Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .clickable { onNavigate(AppScreen.ASSET_ADD_EDIT) }
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(TurquoisePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Kamera / AI Ekle",
                        tint = NavyDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Kamera",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TurquoiseDark
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) TurquoiseDark else TextMutedLight,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TurquoiseDark else TextMutedLight
        )
    }
}
