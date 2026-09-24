package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AssetStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ExportUtil
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val assets by viewModel.allAssets.collectAsState()
    val assignments by viewModel.allAssignments.collectAsState()
    val maintenance by viewModel.allMaintenance.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()

    val reportTypes = listOf(
        "Demirbaş Listesi",
        "Zimmet Raporu",
        "Bölüm Bazlı",
        "Konum Bazlı",
        "Bakım Raporu",
        "Garanti Bitiş",
        "Sayım Raporu",
        "Hurda Raporu",
        "İşlem Geçmişi",
        "Değer Raporu"
    )

    var selectedReport by remember { mutableStateOf(reportTypes[0]) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
            maximumFractionDigits = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
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
                Text(
                    text = "Kurumsal Raporlar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyDark
                )
            }

            Button(
                onClick = {
                    val csv = ExportUtil.generateAssetCsv(assets)
                    ExportUtil.shareText(context, "AKYOL_${selectedReport.replace(" ", "_")}.csv", csv)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Excel / CSV Aktar", fontSize = 12.sp)
            }
        }

        // Report Type Horizontal Selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            items(reportTypes) { rep ->
                FilterChip(
                    selected = selectedReport == rep,
                    onClick = { selectedReport = rep },
                    label = { Text(rep, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TurquoiseDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Report Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = selectedReport,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Akyol Envanter Yönetimi • Otomatik Konsolide Veri",
                            fontSize = 11.sp,
                            color = TurquoiseLight
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Toplam Demirbaş", fontSize = 11.sp, color = TextMutedDark)
                                Text("${assets.size} Adet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Toplam Portföy Değeri", fontSize = 11.sp, color = TextMutedDark)
                                Text(currencyFormatter.format(assets.sumOf { it.purchasePrice }), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TurquoiseLight)
                            }
                            Column {
                                Text("Zimmet Oranı", fontSize = 11.sp, color = TextMutedDark)
                                val ratio = if (assets.isNotEmpty()) (assets.count { it.status == AssetStatus.ASSIGNED.name } * 100 / assets.size) else 0
                                Text("%$ratio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrightBlue)
                            }
                        }
                    }
                }
            }

            // Report Details based on Selection
            when (selectedReport) {
                "Değer Raporu" -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Departman Bazlı Demirbaş Değer Dağılımı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                val deptValues = assets.groupBy { it.department }.mapValues { (_, list) -> list.sumOf { it.purchasePrice } }
                                val maxVal = deptValues.values.maxOrNull() ?: 1.0

                                deptValues.forEach { (dept, value) ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(dept, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text(currencyFormatter.format(value), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TurquoiseDark)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (value / maxVal).toFloat() },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = TurquoiseDark,
                                            trackColor = NeutralCardBorder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Garanti Bitiş" -> {
                    val expiringAssets = assets.filter { it.warrantyEndDate.contains("2024") || it.warrantyEndDate.contains("2025") }
                    items(expiringAssets) { a ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(14.dp).fillMaxWidth()
                            ) {
                                Column {
                                    Text(a.assetCode, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TurquoiseDark)
                                    Text(a.assetName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Bölüm: ${a.department}", fontSize = 11.sp, color = TextSecondaryLight)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Garanti Bitiş", fontSize = 10.sp, color = TextSecondaryLight)
                                    Text(a.warrantyEndDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                }
                            }
                        }
                    }
                }

                "Bakım Raporu" -> {
                    items(maintenance) { m ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${m.assetCode} - ${m.maintenanceType}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(currencyFormatter.format(m.cost), fontWeight = FontWeight.Bold, color = TurquoiseDark)
                                }
                                Text(m.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Firma: ${m.serviceProvider} • Tarih: ${m.maintenanceDate}", fontSize = 11.sp, color = TextSecondaryLight)
                            }
                        }
                    }
                }

                else -> {
                    // Standard tabular asset listing
                    items(assets) { a ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(AppScreen.ASSET_DETAIL, a.assetCode) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(a.assetCode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TurquoiseDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(a.category, fontSize = 11.sp, color = TextSecondaryLight)
                                    }
                                    Text(a.assetName, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text("${a.department} • ${a.building} ${a.room}", fontSize = 11.sp, color = TextSecondaryLight)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(currencyFormatter.format(a.purchasePrice), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                    Text(a.getStatusEnum().labelTr, fontSize = 11.sp, color = StatusAssigned, fontWeight = FontWeight.SemiBold)
                                }
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
