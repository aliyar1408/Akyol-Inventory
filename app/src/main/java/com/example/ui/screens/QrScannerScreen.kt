package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(viewModel: InventoryViewModel) {
    val assets by viewModel.allAssets.collectAsState()
    var manualCode by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }

    // Scanner Laser Animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
    ) {
        // Scanner Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
            }
            Text(
                text = "QR / Barkod Tarayıcı",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )
            IconButton(onClick = { flashEnabled = !flashEnabled }) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flaş",
                    tint = if (flashEnabled) WarningAmber else Color.White
                )
            }
        }

        // Camera Viewfinder Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder Reticle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, TurquoisePrimary, RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                // Moving laser scan line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = laserOffset.dp)
                        .background(TurquoiseLight)
                )

                // Corner brackets
                Icon(
                    Icons.Default.CropFree,
                    contentDescription = null,
                    tint = TurquoisePrimary.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize().padding(12.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Karekod veya Barkodu Çerçeveye Hizalayın",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Kayıt otomatik olarak açılacaktır",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }

        // Bottom Controls: Quick Test Buttons & Manual Code Input
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Hızlı Test: Simüle Tarama",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Chips for quick scanning assets in emulator
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(assets.take(6)) { a ->
                        Surface(
                            color = TurquoiseDark.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TurquoiseDark.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                viewModel.navigateTo(AppScreen.ASSET_DETAIL, a.assetCode)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(a.assetCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TurquoiseDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Veya Kodu Manuel Olarak Girin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { manualCode = it },
                        placeholder = { Text("Örn: AKY-2026-000001", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (manualCode.isNotBlank()) {
                                val found = assets.find { it.assetCode.equals(manualCode.trim(), ignoreCase = true) }
                                if (found != null) {
                                    viewModel.navigateTo(AppScreen.ASSET_DETAIL, found.assetCode)
                                } else {
                                    viewModel.showMessage("Demirbaş kodu bulunamadı: $manualCode")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Git")
                    }
                }
            }
        }
    }
}
