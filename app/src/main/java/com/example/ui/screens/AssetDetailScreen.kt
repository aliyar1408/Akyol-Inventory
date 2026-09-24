package com.example.ui.screens

import android.content.Context
import android.location.Location
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
    var showLocationEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (asset == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Demirbaş kaydı bulunamadı.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateTo(AppScreen.ASSET_LIST) }) {
                    Text("Listeye Dön")
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

    val tabs = listOf(
        "Genel Bilgiler",
        "Zimmet Geçmişi",
        "Bakım",
        "Belgeler",
        "Fotoğraflar",
        "Konum",
        "İşlem Geçmişi"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Action Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.ASSET_LIST) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                }
                Column {
                    Text(
                        text = currentAsset.assetCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TurquoiseDark
                    )
                    Text(
                        text = currentAsset.assetName,
                        fontSize = 12.sp,
                        color = TextSecondaryLight,
                        maxLines = 1
                    )
                }
            }

            Row {
                IconButton(onClick = { showQrDialog = true }) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR Göster", tint = NavyDark)
                }
                IconButton(onClick = { showLabelDialog = true }) {
                    Icon(Icons.Default.Print, contentDescription = "Etiket Yazdır", tint = NavyDark)
                }
                if (currentRole == UserRole.ADMIN) {
                    IconButton(onClick = { showDeleteDialog = true }) {
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
            // Hero Asset Presentation Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NavyDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentAsset.category) {
                                        "Dizüstü Bilgisayar", "Masaüstü Bilgisayar" -> Icons.Default.Computer
                                        "Monitör" -> Icons.Default.DesktopWindows
                                        "Yazıcı" -> Icons.Default.Print
                                        "Telefon", "Tablet" -> Icons.Default.Smartphone
                                        "Ağ Ekipmanı" -> Icons.Default.Router
                                        "Mobilya" -> Icons.Default.Chair
                                        else -> Icons.Default.DevicesOther
                                    },
                                    contentDescription = null,
                                    tint = TurquoiseLight,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(status = currentAsset.getStatusEnum())
                                Spacer(modifier = Modifier.height(4.dp))
                                ConditionBadge(condition = currentAsset.getConditionEnum())
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentAsset.assetName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${currentAsset.brand} ${currentAsset.model} • Seri No: ${currentAsset.serialNumber.ifBlank { "Belirtilmemiş" }}",
                            fontSize = 13.sp,
                            color = TextSecondaryLight
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Zimmetli info banner
                        if (currentAsset.assignedUserName.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusAssigned.copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = StatusAssigned)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Zimmetli Personel: ${currentAsset.assignedUserName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StatusAssigned
                                    )
                                    Text(
                                        text = "Tarih: ${currentAsset.assignmentDate} • Tahmini İade: ${currentAsset.expectedReturnDate.ifBlank { "Süresiz" }}",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusAvailable.copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = StatusAvailable)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Demirbaş Boşta - Zimmete Tahsis Edilebilir",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = StatusAvailable
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Workflow Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (currentAsset.status == AssetStatus.AVAILABLE.name) {
                                Button(
                                    onClick = { showZimmetleDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusAssigned),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zimmetle")
                                }
                            } else if (currentAsset.status == AssetStatus.ASSIGNED.name) {
                                OutlinedButton(
                                    onClick = { showZimmettenDusDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zimmetten Düş")
                                }
                            }

                            Button(
                                onClick = { showAddMaintenanceDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bakım Ekle")
                            }
                        }
                    }
                }
            }

            // Scrollable Tab Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
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
                                    color = if (selectedTab == index) TurquoiseDark else TextSecondaryLight
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Finans & Satın Alma Detayları", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                            DetailRow("Satın Alma Bedeli", currencyFormatter.format(currentAsset.purchasePrice))
                            DetailRow("Satın Alma Tarihi", currentAsset.purchaseDate)
                            DetailRow("Tedarikçi Firma", currentAsset.supplier.ifBlank { "Doğrudan Temin" })
                            DetailRow("Fatura Numarası", currentAsset.invoiceNumber.ifBlank { "FTR-2024-XXXX" })
                            DetailRow("Garanti Başlangıç", currentAsset.warrantyStartDate)
                            DetailRow("Garanti Bitiş", currentAsset.warrantyEndDate)

                            HorizontalDivider(color = NeutralCardBorder)

                            Text("Teknik ve Kurumsal Bilgiler", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                            DetailRow("Kategori", currentAsset.category)
                            DetailRow("Departman", currentAsset.department)
                            DetailRow("Barkod", currentAsset.barcode.ifBlank { "-" })
                            DetailRow("Açıklama", currentAsset.description.ifBlank { "Kurumsal demirbaş kaydı." })
                            DetailRow("Notlar", currentAsset.notes.ifBlank { "Özel not bulunmuyor." })
                        }
                    }
                }
            }

            // Tab 1: Zimmet Geçmişi
            if (selectedTab == 1) {
                if (assetAssignments.isEmpty()) {
                    item {
                        EmptyTabBox("Kayıtlı zimmet geçmişi bulunmuyor.")
                    }
                } else {
                    items(assetAssignments) { rec ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(rec.employeeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        color = if (rec.returnDate.isBlank()) StatusAssigned.copy(alpha = 0.15f) else StatusAvailable.copy(alpha = 0.15f),
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
                                Text("Bölüm: ${rec.department} • Tahsis: ${rec.assignedDate}", fontSize = 12.sp, color = TextSecondaryLight)
                                if (rec.returnDate.isNotBlank()) {
                                    Text("İade Tarihi: ${rec.returnDate}", fontSize = 12.sp, color = StatusAvailable)
                                }
                                if (rec.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Not: ${rec.notes}", fontSize = 11.sp, color = TextSecondaryLight)
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Bakım
            if (selectedTab == 2) {
                if (assetMaintenance.isEmpty()) {
                    item {
                        EmptyTabBox("Kayıtlı bakım faaliyeti bulunmuyor.")
                    }
                } else {
                    items(assetMaintenance) { m ->
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
                                    Text(m.maintenanceType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(currencyFormatter.format(m.cost), fontWeight = FontWeight.Bold, color = TurquoiseDark)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(m.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Servis Sağlayıcı: ${m.serviceProvider} • Tarih: ${m.maintenanceDate}", fontSize = 11.sp, color = TextSecondaryLight)
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Ekli Belgeler ve Sözleşmeler", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            DocumentItem("Satın Alma Faturası.pdf", "1.4 MB • 2024", Icons.Default.Description)
                            DocumentItem("Garanti Belgesi & Sertifika.pdf", "840 KB • 2024", Icons.Default.Verified)
                            DocumentItem("Zimmet Teslim Tutanağı.pdf", "320 KB • İmzalı", Icons.Default.AssignmentTurnedIn)
                        }
                    }
                }
            }

            // Tab 4: Fotoğraflar
            if (selectedTab == 4) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavyDark.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("${currentAsset.assetName} Demirbaş Fotoğrafı", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(AppScreen.ASSET_ADD_EDIT) }
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Yeni Fotoğraf Yükle")
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Hiyerarşik Konum Bilgileri", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TextButton(onClick = { showLocationEditDialog = true }) {
                                    Text("Güncelle", color = TurquoiseDark)
                                }
                            }

                            DetailRow("Şube", currentAsset.branch)
                            DetailRow("Bina", currentAsset.building)
                            DetailRow("Kat", currentAsset.floor)
                            DetailRow("Oda / Alan", currentAsset.room)
                            DetailRow("Konum Açıklaması", currentAsset.locationDescription)
                            DetailRow("GPS Koordinatı", "${currentAsset.latitude}, ${currentAsset.longitude}")

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Pin Map Visualization
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = StatusFaulty, modifier = Modifier.size(36.dp))
                                    Text(
                                        text = "${currentAsset.building} - ${currentAsset.room}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                    Text("İstanbul Merkez Kampüsü", fontSize = 10.sp, color = TextSecondaryLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    viewModel.updateLocation(
                                        asset = currentAsset,
                                        branch = currentAsset.branch,
                                        building = currentAsset.building,
                                        floor = currentAsset.floor,
                                        room = currentAsset.room,
                                        lat = 41.0082 + (Math.random() - 0.5) * 0.01,
                                        lng = 28.9784 + (Math.random() - 0.5) * 0.01
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mevcut Konumu Al (GPS)")
                            }
                        }
                    }
                }
            }

            // Tab 6: İşlem Geçmişi (Full immutable audit log)
            if (selectedTab == 6) {
                if (assetActivities.isEmpty()) {
                    item {
                        EmptyTabBox("Bu demirbaşa ait kayıtlı işlem geçmişi bulunmuyor.")
                    }
                } else {
                    items(assetActivities) { act ->
                        ActivityFeedItem(log = act, onClick = {})
                    }
                }
            }

            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(56.dp))
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

    if (showAddMaintenanceDialog) {
        AddMaintenanceDialog(
            asset = currentAsset,
            onDismiss = { showAddMaintenanceDialog = false },
            onConfirm = { record ->
                viewModel.addMaintenance(currentAsset, record)
                showAddMaintenanceDialog = false
            }
        )
    }

    if (showLocationEditDialog) {
        EditLocationDialog(
            asset = currentAsset,
            onDismiss = { showLocationEditDialog = false },
            onConfirm = { bldg, fl, rm, desc ->
                viewModel.updateLocation(
                    currentAsset,
                    currentAsset.branch,
                    bldg,
                    fl,
                    rm,
                    currentAsset.latitude,
                    currentAsset.longitude
                )
                showLocationEditDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Demirbaşı Sil") },
            text = { Text("${currentAsset.assetCode} kodlu demirbaşı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAsset(currentAsset.assetCode)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFaulty)
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondaryLight)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DocumentItem(name: String, meta: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeutralCardBorder.copy(alpha = 0.3f))
            .clickable {
                ExportUtil.shareText(context, name, "AKYOL INVENTORY Belgesi: $name ($meta)")
            }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TurquoiseDark)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(meta, fontSize = 11.sp, color = TextSecondaryLight)
            }
        }
        Icon(Icons.Default.Download, contentDescription = "İndir", tint = TextSecondaryLight, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun EmptyTabBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, fontSize = 13.sp, color = TextSecondaryLight)
    }
}

@Composable
fun AddMaintenanceDialog(
    asset: Asset,
    onDismiss: () -> Unit,
    onConfirm: (MaintenanceRecord) -> Unit
) {
    var type by remember { mutableStateOf("Periyodik Bakım") }
    var desc by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("Yetkili Teknik Servis") }
    var costStr by remember { mutableStateOf("1500") }
    var nextDate by remember { mutableStateOf("15.12.2025") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Yeni Bakım / Onarım Kaydı", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Bakım Türü") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Açıklama / Yapılan İşlemler") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Servis Sağlayıcı Firma") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Maliyet (TL)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextDate,
                    onValueChange = { nextDate = it },
                    label = { Text("Sonraki Planlanan Bakım") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Vazgeç") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cost = costStr.toDoubleOrNull() ?: 0.0
                            val rec = MaintenanceRecord(
                                assetCode = asset.assetCode,
                                maintenanceType = type,
                                description = desc.ifBlank { "Standart servis bakımı yapıldı." },
                                serviceProvider = provider,
                                maintenanceDate = "Bugün",
                                nextMaintenanceDate = nextDate,
                                cost = cost,
                                performedBy = "Teknisyen"
                            )
                            onConfirm(rec)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark)
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun EditLocationDialog(
    asset: Asset,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var bldg by remember { mutableStateOf(asset.building) }
    var fl by remember { mutableStateOf(asset.floor) }
    var rm by remember { mutableStateOf(asset.room) }
    var desc by remember { mutableStateOf(asset.locationDescription) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Konum Güncelle", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(value = bldg, onValueChange = { bldg = it }, label = { Text("Bina") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fl, onValueChange = { fl = it }, label = { Text("Kat") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rm, onValueChange = { rm = it }, label = { Text("Oda") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Konum Açıklaması") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(bldg, fl, rm, desc) },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark)
                    ) {
                        Text("Güncelle")
                    }
                }
            }
        }
    }
}
