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

    // Calculations
    val totalCount = assets.size
    val assignedCount = assets.count { it.status == AssetStatus.ASSIGNED.name }
    val availableCount = assets.count { it.status == AssetStatus.AVAILABLE.name }
    val maintenanceCount = assets.count { it.status == AssetStatus.MAINTENANCE.name }
    val faultyCount = assets.count { it.status == AssetStatus.FAULTY.name }
    val lostCount = assets.count { it.status == AssetStatus.LOST.name }
    val retiredCount = assets.count { it.status == AssetStatus.RETIRED.name }
    val totalValue = assets.sumOf { it.purchasePrice }
    val addedThisMonth = assets.count { it.purchaseDate.startsWith("2024-03") || it.purchaseDate.startsWith("2024-05") || it.purchaseDate.startsWith("2024-09") || it.purchaseDate.startsWith("2026") }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
            maximumFractionDigits = 0
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // App Header & Branding
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Geometric Cube / QR inspired Logo Mark
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TurquoisePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Inventory2,
                                    contentDescription = "Logo",
                                    tint = NavyDark,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AKYOL INVENTORY",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Demirbaş Yönetim Sistemi",
                                    fontSize = 12.sp,
                                    color = TurquoiseLight
                                )
                            }
                        }

                        // Role Tag
                        Surface(
                            color = BrightBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = currentRole.name,
                                color = BrightBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Total Value Banner
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavySurface)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Toplam Demirbaş Portföy Değeri",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                            Text(
                                text = currencyFormatter.format(totalValue),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TurquoiseLight
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Raporlar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionCard(
                    title = "+ Demirbaş Ekle",
                    icon = Icons.Default.AddCircle,
                    color = TurquoiseDark,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.ASSET_ADD_EDIT) }
                )
                QuickActionCard(
                    title = "QR / Barkod",
                    icon = Icons.Default.QrCodeScanner,
                    color = BrightBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.QR_SCANNER) }
                )
                QuickActionCard(
                    title = "Sayım Modu",
                    icon = Icons.Default.FactCheck,
                    color = WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.AUDIT) }
                )
                QuickActionCard(
                    title = "Dışa Aktar",
                    icon = Icons.Default.FileDownload,
                    color = NavyLight,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val csv = ExportUtil.generateAssetCsv(assets)
                        ExportUtil.shareText(context, "AKYOL_Demirbas_Listesi.csv", csv)
                    }
                )
            }
        }

        // Section Title: Demirbaş Durum Özeti
        item {
            Text(
                text = "Demirbaş Durum Özeti",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Summary Metric Cards Grid (Clickable to filtered list)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricStatCard(
                        title = "Toplam Demirbaş",
                        value = totalCount.toString(),
                        subtext = "Aktif Kayıt",
                        icon = Icons.Default.Inventory,
                        color = NavyDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.clearFilters()
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    MetricStatCard(
                        title = "Zimmetli",
                        value = assignedCount.toString(),
                        subtext = "%${if (totalCount > 0) (assignedCount * 100 / totalCount) else 0} Kullanımda",
                        icon = Icons.Default.AssignmentInd,
                        color = StatusAssigned,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.ASSIGNED.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    MetricStatCard(
                        title = "Boşta",
                        value = availableCount.toString(),
                        subtext = "Tahsise Hazır",
                        icon = Icons.Default.CheckCircle,
                        color = StatusAvailable,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.AVAILABLE.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricStatCard(
                        title = "Bakımda",
                        value = maintenanceCount.toString(),
                        subtext = "Servis Sürecinde",
                        icon = Icons.Default.Build,
                        color = StatusMaintenance,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.MAINTENANCE.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    MetricStatCard(
                        title = "Arızalı",
                        value = faultyCount.toString(),
                        subtext = "Müdahale Bekliyor",
                        icon = Icons.Default.Warning,
                        color = StatusFaulty,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.FAULTY.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                    MetricStatCard(
                        title = "Kayıp / Hurda",
                        value = (lostCount + retiredCount).toString(),
                        subtext = "$lostCount Kayıp, $retiredCount Hurda",
                        icon = Icons.Default.DeleteSweep,
                        color = StatusLost,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setStatusFilter(AssetStatus.RETIRED.name)
                            viewModel.navigateTo(AppScreen.ASSET_LIST)
                        }
                    )
                }
            }
        }

        // Section: Kategori Dağılımı Grafiği (Responsive Interactive Visual Bar)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Kategori Bazlı Dağılım",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Detay",
                            color = TurquoiseDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.REPORTS) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val categories = assets.groupBy { it.category }
                        .toList()
                        .sortedByDescending { it.second.size }
                        .take(5)

                    categories.forEach { (cat, list) ->
                        val ratio = if (totalCount > 0) list.size.toFloat() / totalCount else 0f
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${list.size} adet (%${(ratio * 100).toInt()})", fontSize = 12.sp, color = TextSecondaryLight)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = when (cat) {
                                    "Dizüstü Bilgisayar" -> TurquoisePrimary
                                    "Monitör" -> BrightBlue
                                    "Ağ Ekipmanı" -> NavyDark
                                    "Mobilya" -> WarningAmber
                                    else -> StatusAssigned
                                },
                                trackColor = NeutralCardBorder
                            )
                        }
                    }
                }
            }
        }

        // Section: Departman Dağılımı
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Departman Bazlı Demirbaşlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val deptMap = assets.groupBy { it.department }
                    deptMap.forEach { (dept, list) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDepartmentFilter(dept)
                                    viewModel.navigateTo(AppScreen.ASSET_LIST)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(TurquoiseDark)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(dept, fontSize = 14.sp, fontWeight = FontWeight.Normal)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = NeutralCardBorder.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${list.size} demirbaş",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(color = NeutralCardBorder.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Section: Son İşlemler (Recent Activity Feed)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Son İşlemler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Tüm Geçmiş",
                    color = TurquoiseDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.REPORTS) }
                )
            }
        }

        items(activities.take(6)) { log ->
            ActivityFeedItem(log = log, onClick = {
                if (log.assetCode.isNotBlank() && log.assetCode != "-") {
                    viewModel.navigateTo(AppScreen.ASSET_DETAIL, log.assetCode)
                }
            })
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp).fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = TextSecondaryLight,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActivityFeedItem(log: ActivityLog, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                        when {
                            log.action.contains("Zimmet", true) -> StatusAssigned.copy(alpha = 0.15f)
                            log.action.contains("Bakım", true) -> StatusMaintenance.copy(alpha = 0.15f)
                            log.action.contains("Oluştur", true) -> StatusAvailable.copy(alpha = 0.15f)
                            else -> TurquoiseDark.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        log.action.contains("Zimmet", true) -> Icons.Default.Assignment
                        log.action.contains("Bakım", true) -> Icons.Default.Build
                        log.action.contains("Konum", true) -> Icons.Default.LocationOn
                        else -> Icons.Default.History
                    },
                    contentDescription = null,
                    tint = TurquoiseDark,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.action,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = log.getFormattedDate(),
                        fontSize = 10.sp,
                        color = TextSecondaryLight
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${log.user} • ${log.newValue}",
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    maxLines = 1
                )
            }
        }
    }
}
