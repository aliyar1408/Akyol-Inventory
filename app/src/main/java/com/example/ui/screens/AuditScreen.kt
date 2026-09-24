package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.data.model.AuditScanItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val assets by viewModel.allAssets.collectAsState()
    val auditItems by viewModel.auditItems.collectAsState()
    val sessionName = viewModel.auditSessionName

    var currentScanLocation by remember { mutableStateOf("A Blok Kat 3") }
    var manualScanInput by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("TÜMÜ") }

    // Calculated metrics
    val totalTarget = assets.size
    val foundCount = auditItems.count { it.status == "BULUNAN" }
    val wrongLocationCount = auditItems.count { it.status == "YANLIS_KONUM" }
    val scannedTotal = (foundCount + wrongLocationCount).coerceAtMost(totalTarget)
    val pendingCount = (totalTarget - scannedTotal).coerceAtLeast(0)

    val progressPercent = if (totalTarget > 0) (scannedTotal.toFloat() / totalTarget) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = sessionName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Aktif Sayım Oturumu • $currentScanLocation",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Button(
                onClick = {
                    val csv = ExportUtil.generateAssetCsv(assets)
                    ExportUtil.shareText(context, "SAYIM_RAPORU.csv", csv)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dışa Aktar", fontSize = 12.sp, color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Audit Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sayım İlerlemesi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("%${(progressPercent * 100).toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AccentTeal)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progressPercent },
                            color = AccentTeal,
                            trackColor = SurfaceDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid (Bulunan, Yanlış Konum, Kalan)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuditStatBox("Taranan", "$scannedTotal", StatusAssigned)
                            AuditStatBox("Bulunan", "$foundCount", StatusAvailable)
                            AuditStatBox("Farklı Konum", "$wrongLocationCount", StatusMaintenance)
                            AuditStatBox("Bekleyen", "$pendingCount", TextSecondary)
                        }
                    }
                }
            }

            // Quick Scan Bar
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Hızlı Barkod / QR Sayım Girişi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = manualScanInput,
                                onValueChange = { manualScanInput = it },
                                placeholder = { Text("Kod okutun veya yazın...", fontSize = 13.sp, color = TextMuted) },
                                textStyle = CodeTextStyle,
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTeal,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (manualScanInput.isNotBlank()) {
                                        val code = manualScanInput.trim()
                                        viewModel.processAuditScan(
                                            scannedCode = code,
                                            scannedLocation = currentScanLocation
                                        )
                                        manualScanInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text("Say", color = BackgroundDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Scanned Items List Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sayım Listesi (${auditItems.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "$currentScanLocation",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            if (auditItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Bu oturumda henüz sayım kaydı yapılmadı.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(auditItems) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column {
                                Text(item.assetCode, style = CodeTextStyle)
                                Text("Taranan Konum: ${item.scannedLocation}", fontSize = 12.sp, color = TextSecondary)
                                Text(java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale("tr", "TR")).format(java.util.Date(item.scanTime)), fontSize = 11.sp, color = TextMuted)
                            }

                            Surface(
                                color = if (item.status == "BULUNAN") StatusAvailable.copy(alpha = 0.2f) else StatusMaintenance.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (item.status == "BULUNAN") "Bulundu" else "Farklı Konum",
                                    color = if (item.status == "BULUNAN") StatusAvailable else StatusMaintenance,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun AuditStatBox(title: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(title, fontSize = 11.sp, color = TextSecondary)
    }
}
