package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLog
import com.example.data.model.Asset
import com.example.data.model.AssetStatus
import com.example.data.model.UserRole
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ExportUtil
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val assets by viewModel.allAssets.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    // Status counts
    val totalCount = assets.size
    val assignedCount = assets.count { it.status == AssetStatus.ASSIGNED.name }
    val availableCount = assets.count { it.status == AssetStatus.AVAILABLE.name }
    val maintenanceCount = assets.count { it.status == AssetStatus.MAINTENANCE.name }
    val faultyCount = assets.count { it.status == AssetStatus.FAULTY.name }
    val lostCount = assets.count { it.status == AssetStatus.LOST.name }
    val retiredCount = assets.count { it.status == AssetStatus.RETIRED.name }
    val totalValue = assets.sumOf { it.purchasePrice }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
            maximumFractionDigits = 0
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // 1. App Header & Enterprise Portfolio Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Logo Box
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CardSurfaceElevated)
                                    .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Inventory2,
                                    contentDescription = "Logo",
                                    tint = AccentTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AKYOL INVENTORY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Demirbaş Yönetim Sistemi",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Role Indicator
                        Surface(
                            color = CardSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = currentRole.name,
                                color = AccentTealLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Portfolio Value Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Toplam Demirbaş Değeri",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormatter.format(totalValue),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Raporlar", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // 2. Quick Actions
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionCard(
                    title = "Demirbaş Ekle",
                    icon = Icons.Default.AddCircle,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.ASSET_ADD_EDIT) }
                )
                QuickActionCard(
                    title = "QR / Barkod",
                    icon = Icons.Default.QrCodeScanner,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.QR_SCANNER) }
                )
                QuickActionCard(
                    title = "Sayım",
                    icon = Icons.Default.FactCheck,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.AUDIT) }
                )
                QuickActionCard(
                    title = "Excel Aktar",
                    icon = Icons.Default.FileDownload,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val result = ExportUtil.exportToXlsx(context, assets, "Tüm Demirbaşlar")
                        result.onSuccess { file ->
                            viewModel.showMessage("Excel dosyası hazırlandı.")
                            ExportUtil.shareFile(
                                context,
                                file,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "Akyol Inventory Excel İndir"
                            )
                        }.onFailure { err ->
                            android.util.Log.e("DashboardScreen", "Excel export error", err)
                            viewModel.showMessage("Excel dosyası oluşturulamadı: ${err.localizedMessage}")
                        }
                    }
                )
            }
        }

        // 3. Section Title: Demirbaş Durum Özeti
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Demirbaş Durum Özeti",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${assets.size} Kayıt",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // 4. Metric Stat Cards (Redesigned as per Requirement 3: Clean, uncluttered, big number, short label, subtle indicator)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Toplam & Zimmetli
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    CleanMetricCard(
                        count = totalCount,
                        label = "Toplam Demirbaş",
                        indicatorColor = TextCode,
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.clearFilters()
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    CleanMetricCard(
                        count = assignedCount,
                        label = "Zimmetli",
                        indicatorColor = StatusAssigned,
                        icon = Icons.Default.AssignmentInd,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.ASSIGNED.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                }

                // Row 2: Boşta & Bakımda
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    CleanMetricCard(
                        count = availableCount,
                        label = "Boşta",
                        indicatorColor = StatusAvailable,
                        icon = Icons.Default.CheckCircleOutline,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.AVAILABLE.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    CleanMetricCard(
                        count = maintenanceCount,
                        label = "Bakımda",
                        indicatorColor = StatusMaintenance,
                        icon = Icons.Default.Build,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.MAINTENANCE.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                }

                // Row 3: Arızalı & Hurda/Kayıp
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    CleanMetricCard(
                        count = faultyCount,
                        label = "Arızalı",
                        indicatorColor = StatusFaulty,
                        icon = Icons.Default.ErrorOutline,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.FAULTY.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    CleanMetricCard(
                        count = lostCount + retiredCount,
                        label = "Hurda / Kayıp",
                        indicatorColor = StatusRetired,
                        icon = Icons.Default.Archive,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.RETIRED.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                }
            }
        }

        // 5. Category Breakdown Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kategori Dağılımı",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val topCategories = remember(assets) {
                        assets.groupBy { it.category }
                            .mapValues { it.value.size }
                            .toList()
                            .sortedByDescending { it.second }
                            .take(5)
                    }

                    topCategories.forEach { (categoryName, count) ->
                        val percent = if (totalCount > 0) (count.toFloat() / totalCount) else 0f
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = categoryName, fontSize = 13.sp, color = TextPrimary)
                                Text(text = "$count adet (%${(percent * 100).toInt()})", fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                color = AccentTeal,
                                trackColor = SurfaceDark,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Activity Feed ("Son İşlemler")
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Son İşlemler",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Denetim İzi",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        items(activities.take(5)) { act ->
            ActivityFeedItem(activity = act)
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

// Clean Metric Card as requested in Requirement 3
@Composable
fun CleanMetricCard(
    count: Int,
    label: String,
    indicatorColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subtle status dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = indicatorColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Big Number
            Text(
                text = String.format(Locale.GERMANY, "%,d", count),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Short Label
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AccentTeal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActivityFeedItem(activity: ActivityLog) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            val isAdd = activity.action.contains("Eklendi", ignoreCase = true)
            val isAssign = activity.action.contains("Zimmetlendi", ignoreCase = true)
            val isReturn = activity.action.contains("Düşüldü", ignoreCase = true)
            val isMaint = activity.action.contains("Bakım", ignoreCase = true)
            val isAudit = activity.action.contains("Sayım", ignoreCase = true)

            val icon = when {
                isAdd -> Icons.Default.AddCircle
                isAssign -> Icons.Default.AssignmentInd
                isReturn -> Icons.Default.AssignmentReturn
                isMaint -> Icons.Default.Build
                isAudit -> Icons.Default.QrCodeScanner
                else -> Icons.Default.History
            }
            val iconTint = when {
                isAdd -> StatusAvailable
                isAssign -> StatusAssigned
                isReturn -> AccentTeal
                isMaint -> StatusMaintenance
                isAudit -> AccentBlue
                else -> TextSecondary
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val detailText = if (activity.newValue.isNotBlank()) {
                    "${activity.action}: ${activity.newValue}"
                } else {
                    activity.action
                }
                Text(
                    text = detailText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activity.assetCode.isNotBlank()) {
                        Text(
                            text = activity.assetCode,
                            style = SmallCodeTextStyle
                        )
                        Text(
                            text = " • ",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "${activity.user} • ${activity.getFormattedDate()}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
