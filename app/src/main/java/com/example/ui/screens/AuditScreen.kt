package com.example.ui.screens

import androidx.compose.foundation.background
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
    val missingCount = 0 // In real audits, after session ends, remaining pending become missing

    val progressPercent = if (totalTarget > 0) (scannedTotal.toFloat() / totalTarget) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                }
                Column {
                    Text(
                        text = sessionName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "Aktif Sayım Oturumu • $currentScanLocation",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            Button(
                onClick = {
                    val reportText = buildString {
                        append("AKYOL INVENTORY - SAYIM RAPORU\n")
                        append("Oturum: $sessionName\n")
                        append("Tarih: 2026-09-24\n")
                        append("Toplam Hedef: $totalTarget\n")
                        append("Bulunan: $foundCount\n")
                        append("Yanlış Konumda: $wrongLocationCount\n")
                        append("Bekleyen: $pendingCount\n\n")
                        append("TARANAN DEMİRBAŞLAR:\n")
                        auditItems.forEach {
                            append("${it.assetCode} - ${it.assetName} [${it.status}] (Beklenen: ${it.expectedLocation}, Taranan: ${it.scannedLocation})\n")
                        }
                    }
                    ExportUtil.shareText(context, "Sayım Raporu: $sessionName", reportText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Raporla", fontSize = 11.sp)
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sayım İlerleme Durumu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("%${(progressPercent * 100).toInt()} Tamamlandı", fontWeight = FontWeight.Bold, color = TurquoiseDark)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = TurquoisePrimary,
                            trackColor = NeutralCardBorder
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Audit 4-Status Metrics Bar
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuditMetricBadge("Bulunan", foundCount.toString(), StatusAvailable, Modifier.weight(1f))
                            AuditMetricBadge("Bekleyen", pendingCount.toString(), WarningAmber, Modifier.weight(1f))
                            AuditMetricBadge("Yanlış Konum", wrongLocationCount.toString(), Color(0xFF8B5CF6), Modifier.weight(1f))
                            AuditMetricBadge("Bulunamayan", missingCount.toString(), StatusFaulty, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Quick Scan Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Karekod / Barkod Sayım Taraması", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Sayım lokasyonunu seçin ve demirbaş kodunu okutun.", fontSize = 11.sp, color = TextSecondaryLight)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Location Selector
                        val locList = listOf("A Blok Kat 3", "B Blok Kat 2", "A Blok Kat 4", "C Blok Zemin Kat", "A Blok Kat 1")
                        var locExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { locExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Sayım Alanı: $currentScanLocation", fontSize = 12.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = locExpanded, onDismissRequest = { locExpanded = false }) {
                                locList.forEach { loc ->
                                    DropdownMenuItem(text = { Text(loc) }, onClick = {
                                        currentScanLocation = loc
                                        locExpanded = false
                                    })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = manualScanInput,
                                onValueChange = { manualScanInput = it },
                                placeholder = { Text("Kodu girin veya seçin...", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (manualScanInput.isNotBlank()) {
                                        viewModel.processAuditScan(manualScanInput.trim(), currentScanLocation)
                                        manualScanInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Tara")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick simulator chips for fast testing in streaming emulator
                        Text("Simülatör Hızlı Tarama Tuşları:", fontSize = 11.sp, color = TextSecondaryLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(assets.take(7)) { a ->
                                Surface(
                                    color = NeutralCardBorder.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        viewModel.processAuditScan(a.assetCode, currentScanLocation)
                                    }
                                ) {
                                    Text(
                                        text = a.assetCode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
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
                    Text("Taranan Demirbaşlar (${auditItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (auditItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Henüz demirbaş taraması yapılmadı. Yukarıdaki kodları kullanarak tarama başlatabilirsiniz.", fontSize = 12.sp, color = TextSecondaryLight)
                    }
                }
            } else {
                items(auditItems) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.status == "BULUNAN") StatusAvailable.copy(alpha = 0.15f) else Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.status == "BULUNAN") Icons.Default.Check else Icons.Default.WrongLocation,
                                    contentDescription = null,
                                    tint = if (item.status == "BULUNAN") StatusAvailable else Color(0xFF8B5CF6),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(item.assetCode, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(
                                        color = if (item.status == "BULUNAN") StatusAvailable.copy(alpha = 0.15f) else Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (item.status == "BULUNAN") "Bulundu" else "Yanlış Konumda",
                                            color = if (item.status == "BULUNAN") StatusAvailable else Color(0xFF8B5CF6),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(item.assetName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text("Beklenen: ${item.expectedLocation} • Taranan: ${item.scannedLocation}", fontSize = 11.sp, color = TextSecondaryLight)
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
fun AuditMetricBadge(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).fillMaxWidth()
        ) {
            Text(count, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
        }
    }
}
