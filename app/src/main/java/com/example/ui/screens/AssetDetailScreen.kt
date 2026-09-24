package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ExportUtil
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val asset by viewModel.selectedAsset.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val allMaintenance by viewModel.allMaintenance.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }
    var showZimmetleDialog by remember { mutableStateOf(false) }
    var showZimmettenDusDialog by remember { mutableStateOf(false) }
    var showAddMaintenanceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (asset == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Demirbaş kaydı bulunamadı.", color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ASSET_LIST) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Listeye Dön", color = BackgroundDark)
                }
            }
        }
        return
    }

    val currentAsset = asset!!
    val assetAssignments = remember(allAssignments, currentAsset.assetCode) {
        allAssignments.filter { it.assetCode == currentAsset.assetCode }
    }
    val assetMaintenance = remember(allMaintenance, currentAsset.assetCode) {
        allMaintenance.filter { it.assetCode == currentAsset.assetCode }
    }
    val assetActivities = remember(activities, currentAsset.assetCode) {
        activities.filter { it.assetCode == currentAsset.assetCode }
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
            maximumFractionDigits = 0
        }
    }

    // Exact tab list as requested in Requirement 5
    val tabs = listOf(
        "Genel Bilgiler",
        "Zimmet",
        "Bakım",
        "Belgeler",
        "Fotoğraflar",
        "Konum",
        "Geçmiş"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Action Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.ASSET_LIST) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = currentAsset.assetCode,
                        style = CodeTextStyle
                    )
                    Text(
                        text = currentAsset.assetName,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Row {
                IconButton(
                    onClick = { showQrDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR Göster", tint = TextPrimary)
                }
                IconButton(
                    onClick = { showLabelDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Etiket Yazdır", tint = TextPrimary)
                }
                if (currentRole == UserRole.ADMIN) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Sil", tint = StatusFaulty)
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // SECTION 1: Top Hero Section [Fotoğraf, Demirbaş Adı, Kod, Durum badge]
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
                            // Large Photo / Icon Box
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentAsset.category) {
                                        "Dizüstü Bilgisayar", "Masaüstü Bilgisayar" -> Icons.Default.Computer
                                        "Monitör" -> Icons.Default.Tv
                                        "Yazıcı" -> Icons.Default.Print
                                        "Telefon", "Tablet" -> Icons.Default.Smartphone
                                        "Ağ Ekipmanı" -> Icons.Default.Router
                                        "Mobilya" -> Icons.Default.Chair
                                        else -> Icons.Default.Inventory2
                                    },
                                    contentDescription = "Demirbaş Fotoğrafı",
                                    tint = AccentTeal,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            // Status and Condition Badges
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(status = AssetStatus.fromString(currentAsset.status))
                                Spacer(modifier = Modifier.height(6.dp))
                                ConditionBadge(condition = AssetCondition.fromString(currentAsset.condition))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Demirbaş Adı
                        Text(
                            text = currentAsset.assetName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Demirbaş Kodu in Monospace
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentAsset.assetCode,
                            style = CodeTextStyle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Workflow Action Buttons (Zimmetle / Zimmetten Düş, Bakım Ekle)
                        val canMutate = currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER
                        if (canMutate) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (currentAsset.status == AssetStatus.AVAILABLE.name) {
                                    Button(
                                        onClick = { showZimmetleDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusAssigned),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Zimmetle", color = BackgroundDark, fontWeight = FontWeight.Bold)
                                    }
                                } else if (currentAsset.status == AssetStatus.ASSIGNED.name) {
                                    OutlinedButton(
                                        onClick = { showZimmettenDusDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Icon(Icons.Default.PersonRemove, contentDescription = null, tint = StatusAvailable, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Zimmetten Düş", color = TextPrimary)
                                    }
                                }

                                Button(
                                    onClick = { showAddMaintenanceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bakım Kaydet", color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: Structured 2-Column / Card Layout (Requirement 5)
            // 1. Teknik & Donanım Bilgileri
            item {
                DetailCard(
                    title = "Teknik ve Donanım",
                    icon = Icons.Default.Devices
                ) {
                    DetailGridRow("Kategori", currentAsset.category, "Marka", currentAsset.brand.ifBlank { "Belirtilmemiş" })
                    DetailGridRow("Model", currentAsset.model.ifBlank { "Belirtilmemiş" }, "Seri No", currentAsset.serialNumber.ifBlank { "Yok" }, isSecondCode = true)
                }
            }

            // 2. Zimmet & Departman
            item {
                DetailCard(
                    title = "Zimmet ve Organizasyon",
                    icon = Icons.Default.AssignmentInd
                ) {
                    DetailGridRow(
                        "Zimmetli Kişi",
                        currentAsset.assignedUserName.ifBlank { "Boşta (Zimmetsiz)" },
                        "Departman",
                        currentAsset.department
                    )
                    DetailGridRow(
                        "Tahsis Tarihi",
                        currentAsset.assignmentDate.ifBlank { "-" },
                        "Tahmini İade",
                        currentAsset.expectedReturnDate.ifBlank { "Süresiz" }
                    )
                }
            }

            // 3. Konum & Yerleşim
            item {
                DetailCard(
                    title = "Konum ve Yerleşim",
                    icon = Icons.Default.Place
                ) {
                    DetailGridRow("Şube", currentAsset.branch, "Bina", currentAsset.building)
                    DetailGridRow("Kat", currentAsset.floor, "Oda", currentAsset.room)
                }
            }

            // 4. Mali & Finans Bilgileri
            item {
                DetailCard(
                    title = "Satın Alma ve Finans",
                    icon = Icons.Default.MonetizationOn
                ) {
                    DetailGridRow(
                        "Satın Alma Tarihi",
                        currentAsset.purchaseDate,
                        "Satın Alma Bedeli",
                        currencyFormatter.format(currentAsset.purchasePrice)
                    )
                    DetailGridRow(
                        "Tedarikçi",
                        currentAsset.supplier.ifBlank { "Doğrudan Temin" },
                        "Fatura No",
                        currentAsset.invoiceNumber.ifBlank { "-" }
                    )
                }
            }

            // 5. Garanti & Servis Takvimi
            item {
                DetailCard(
                    title = "Garanti ve Bakım Takvimi",
                    icon = Icons.Default.DateRange
                ) {
                    DetailGridRow(
                        "Garanti Bitiş",
                        currentAsset.warrantyEndDate.ifBlank { "Belirtilmemiş" },
                        "Sonraki Bakım",
                        currentAsset.nextMaintenanceDate.ifBlank { "Planlanmamış" }
                    )
                    DetailGridRow(
                        "Son Bakım Tarihi",
                        currentAsset.lastMaintenanceDate.ifBlank { "Kayıt Yok" },
                        "Garanti Başlangıç",
                        currentAsset.warrantyStartDate.ifBlank { "-" }
                    )
                }
            }

            // SECTION 3: Tab System (Genel Bilgiler, Zimmet, Bakım, Belgeler, Fotoğraflar, Konum, Geçmiş)
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = SurfaceDark,
                    contentColor = AccentTeal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == index) TextPrimary else TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Tab 0: Genel Bilgiler
            if (selectedTab == 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailItem("Barkod", currentAsset.barcode.ifBlank { "-" }, isCode = true)
                            DetailItem("Açıklama", currentAsset.description.ifBlank { "Kurumsal demirbaş kaydı." })
                            DetailItem("Kayıt Oluşturan", currentAsset.createdBy)
                            DetailItem("Kayıt Tarihi", SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(currentAsset.createdAt)))
                            DetailItem("Özel Notlar", currentAsset.notes.ifBlank { "Ek not bulunmuyor." })
                        }
                    }
                }
            }

            // Tab 1: Zimmet
            if (selectedTab == 1) {
                if (assetAssignments.isEmpty()) {
                    item { EmptyTabMessage("Kayıtlı zimmet geçmişi bulunmuyor.") }
                } else {
                    items(assetAssignments) { rec ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(rec.employeeName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Surface(
                                        color = if (rec.returnDate.isBlank()) StatusAssigned.copy(alpha = 0.2f) else StatusAvailable.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (rec.returnDate.isBlank()) "Aktif Zimmet" else "İade Edildi",
                                            color = if (rec.returnDate.isBlank()) StatusAssigned else StatusAvailable,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Bölüm: ${rec.department} • Tahsis: ${rec.assignedDate}", fontSize = 12.sp, color = TextSecondary)
                                if (rec.returnDate.isNotBlank()) {
                                    Text("İade Tarihi: ${rec.returnDate}", fontSize = 12.sp, color = StatusAvailable)
                                }
                                if (rec.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Not: ${rec.notes}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Bakım
            if (selectedTab == 2) {
                if (assetMaintenance.isEmpty()) {
                    item { EmptyTabMessage("Kayıtlı periyodik veya arıza bakım kaydı bulunmuyor.") }
                } else {
                    items(assetMaintenance) { m ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(m.maintenanceType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(currencyFormatter.format(m.cost), fontWeight = FontWeight.Bold, color = AccentTeal, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tarih: ${m.maintenanceDate} • Servis: ${m.serviceProvider}", fontSize = 12.sp, color = TextSecondary)
                                Text("Açıklama: ${m.description}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Tab 3: Belgeler
            if (selectedTab == 3) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DocumentListItem(title = "Dijital Zimmet Tutanağı.pdf", size = "245 KB", date = currentAsset.purchaseDate)
                            DocumentListItem(title = "Fatura ve İrsaliye Belgesi.pdf", size = "1.2 MB", date = currentAsset.purchaseDate)
                            DocumentListItem(title = "Garanti Belgesi ve Şartnamesi.pdf", size = "580 KB", date = currentAsset.warrantyStartDate)
                        }
                    }
                }
            }

            // Tab 4: Fotoğraflar
            if (selectedTab == 4) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Demirbaş Fotoğraf Arşivi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceDark)
                                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Tab 5: Konum
            if (selectedTab == 5) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Yerleşim ve GPS Koordinatları", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            DetailItem("Şube & Kampüs", currentAsset.branch)
                            DetailItem("Bina & Blok", currentAsset.building)
                            DetailItem("Kat & Oda", "${currentAsset.floor} / ${currentAsset.room}")
                            DetailItem("Konum Notu", currentAsset.locationDescription.ifBlank { "Standart operasyon alanı" })
                            DetailItem("GPS Koordinatları", "${currentAsset.latitude}, ${currentAsset.longitude}")
                        }
                    }
                }
            }

            // Tab 6: Geçmiş (İşlem Logları)
            if (selectedTab == 6) {
                if (assetActivities.isEmpty()) {
                    item { EmptyTabMessage("Bu demirbaşa ait işlem geçmişi bulunmuyor.") }
                } else {
                    items(assetActivities) { act ->
                        ActivityFeedItem(activity = act)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Modal Dialogs
    if (showQrDialog) {
        QrCodeDialog(
            asset = currentAsset,
            onDismiss = { showQrDialog = false },
            onPrintLabel = { showLabelDialog = true }
        )
    }

    if (showLabelDialog) {
        PrintLabelDialog(
            asset = currentAsset,
            onDismiss = { showLabelDialog = false }
        )
    }

    if (showZimmetleDialog) {
        ZimmetleDialog(
            asset = currentAsset,
            employees = employees,
            onDismiss = { showZimmetleDialog = false },
            onConfirm = { emp, retDate, notes ->
                viewModel.assignAsset(currentAsset, emp, retDate, notes)
                showZimmetleDialog = false
            }
        )
    }

    if (showZimmettenDusDialog) {
        ZimmettenDusDialog(
            asset = currentAsset,
            onDismiss = { showZimmettenDusDialog = false },
            onConfirm = { notes ->
                viewModel.returnAsset(currentAsset, notes)
                showZimmettenDusDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Demirbaşı Sil", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("${currentAsset.assetCode} kodlu demirbaş kalıcı olarak silinecektir. Bu işlem geri alınamaz.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAsset(currentAsset.assetCode)
                        showDeleteDialog = false
                        viewModel.navigateTo(AppScreen.ASSET_LIST)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFaulty)
                ) {
                    Text("Evet, Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = CardSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailGridRow(
    label1: String, value1: String,
    label2: String, value2: String,
    isSecondCode: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label1, fontSize = 11.sp, color = TextMuted)
            Text(value1, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label2, fontSize = 11.sp, color = TextMuted)
            if (isSecondCode) {
                Text(value2, style = SmallCodeTextStyle, maxLines = 1)
            } else {
                Text(value2, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, isCode: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        if (isCode) {
            Text(value, style = SmallCodeTextStyle)
        } else {
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

@Composable
private fun DocumentListItem(title: String, size: String, date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StatusFaulty, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
                Text("$size • $date", fontSize = 11.sp, color = TextMuted)
            }
        }
        Icon(Icons.Default.Download, contentDescription = "İndir", tint = AccentTeal, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyTabMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextSecondary, fontSize = 13.sp)
    }
}
