package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ExportUtil
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "ReportsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val assets by viewModel.allAssets.collectAsState()
    val allMaintenance by viewModel.allMaintenance.collectAsState()

    var selectedReport by remember { mutableStateOf("Genel Envanter Raporu") }
    var isExportingExcel by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }

    val reportTypes = listOf(
        "Genel Envanter Raporu",
        "Zimmetli Demirbaşlar",
        "Boşta / Tahsis Bekleyen",
        "Bakım & Onarım Raporu",
        "Departman Analizi"
    )

    val displayedAssets = remember(selectedReport, assets) {
        when (selectedReport) {
            "Zimmetli Demirbaşlar" -> assets.filter { it.status == AssetStatus.ASSIGNED.name }
            "Boşta / Tahsis Bekleyen" -> assets.filter { it.status == AssetStatus.AVAILABLE.name }
            "Bakım & Onarım Raporu" -> assets.filter { it.status == AssetStatus.MAINTENANCE.name || it.status == AssetStatus.FAULTY.name }
            else -> assets
        }
    }

    val totalValue = remember(displayedAssets) {
        displayedAssets.sumOf { it.purchasePrice }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
            maximumFractionDigits = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top App Bar
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
                        text = "Kurumsal Raporlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Excel (.xlsx) ve PDF raporlama modülü",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Report Type Selector Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
        ) {
            items(reportTypes) { rep ->
                FilterChip(
                    selected = selectedReport == rep,
                    onClick = { selectedReport = rep },
                    label = { Text(rep, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentTeal,
                        selectedLabelColor = BackgroundDark,
                        containerColor = CardSurfaceDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedReport == rep,
                        borderColor = BorderDark,
                        selectedBorderColor = AccentTeal
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Report Header & Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = selectedReport,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rapor Tarihi: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date())}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderDark)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Listelenen Demirbaş", fontSize = 12.sp, color = TextMuted)
                                Text("${displayedAssets.size} Adet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Toplam Portföy Değeri", fontSize = 12.sp, color = TextMuted)
                                Text(currencyFormatter.format(totalValue), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentTealLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Real Export Action Buttons (Excel & PDF)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Real Excel (.xlsx) Export
                            Button(
                                onClick = {
                                    isExportingExcel = true
                                    val result = ExportUtil.exportToXlsx(context, displayedAssets, selectedReport)
                                    isExportingExcel = false
                                    result.onSuccess { file ->
                                        viewModel.showMessage("Excel dosyası hazırlandı.")
                                        ExportUtil.shareFile(
                                            context,
                                            file,
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "AKYOL_$selectedReport.xlsx"
                                        )
                                    }.onFailure { err ->
                                        Log.e(TAG, "Excel oluşturma hatası", err)
                                        viewModel.showMessage("Excel dosyası oluşturulamadı: ${err.localizedMessage}")
                                    }
                                },
                                enabled = !isExportingExcel,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                if (isExportingExcel) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundDark, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Hazırlanıyor...", color = BackgroundDark, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Excel'e Aktar", color = BackgroundDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 2. Real PDF Export
                            OutlinedButton(
                                onClick = {
                                    isExportingPdf = true
                                    val result = ExportUtil.exportToPdf(context, displayedAssets, selectedReport, selectedReport)
                                    isExportingPdf = false
                                    result.onSuccess { file ->
                                        viewModel.showMessage("PDF raporu oluşturuldu.")
                                        ExportUtil.shareFile(
                                            context,
                                            file,
                                            "application/pdf",
                                            "AKYOL_$selectedReport.pdf"
                                        )
                                    }.onFailure { err ->
                                        Log.e(TAG, "PDF oluşturma hatası", err)
                                        viewModel.showMessage("PDF oluşturulamadı: ${err.localizedMessage}")
                                    }
                                },
                                enabled = !isExportingPdf,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                if (isExportingPdf) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Hazırlanıyor...", fontSize = 12.sp, color = TextPrimary)
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StatusFaulty, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PDF'e Aktar", fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Department breakdown in "Departman Analizi"
            if (selectedReport == "Departman Analizi") {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bölüm Bazlı Envanter Dağılımı", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            val deptGroups = assets.groupBy { it.department }
                            deptGroups.forEach { (dept, list) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(dept, fontSize = 13.sp, color = TextPrimary)
                                    Text("${list.size} adet • ${currencyFormatter.format(list.sumOf { it.purchasePrice })}", fontSize = 13.sp, color = TextSecondary)
                                }
                                HorizontalDivider(color = BorderDark.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Asset preview table list
            item {
                Text(
                    text = "Rapor Kapsamındaki Demirbaşlar (${displayedAssets.size})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }

            items(displayedAssets.take(20)) { asset ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(asset.assetName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(asset.assetCode, style = SmallCodeTextStyle)
                                Text(" • ${asset.department}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(currencyFormatter.format(asset.purchasePrice), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            StatusBadge(status = AssetStatus.fromString(asset.status))
                        }
                    }
                }
            }

            if (displayedAssets.size > 20) {
                item {
                    Text(
                        text = "+ ${displayedAssets.size - 20} adet daha... Tam listeyi incelemek için yukarıdaki Excel'e Aktar butonunu kullanabilirsiniz.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
