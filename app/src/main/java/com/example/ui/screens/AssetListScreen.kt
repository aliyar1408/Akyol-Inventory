package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.ui.components.ConditionBadge
import com.example.ui.components.PrintLabelDialog
import com.example.ui.components.QrCodeDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.ZimmetleDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.WarrantyFilter
import com.example.util.ExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val assets by viewModel.filteredAssets.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var isTableView by remember { mutableStateOf(false) }
    var selectedAssetForQr by remember { mutableStateOf<Asset?>(null) }
    var selectedAssetForLabel by remember { mutableStateOf<Asset?>(null) }
    var selectedAssetForAssign by remember { mutableStateOf<Asset?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    val activeCount = filterState.getActiveFilterCount()

    // Distinct lists for dropdowns
    val categories = remember(allAssets) {
        listOf("Tümü") + allAssets.map { it.category }.distinct()
    }
    val departments = remember(allAssets) {
        listOf("Tümü") + allAssets.map { it.department }.distinct()
    }
    val brands = remember(allAssets) {
        listOf("Tümü") + allAssets.map { it.brand }.filter { it.isNotBlank() }.distinct()
    }
    val locations = remember(allAssets) {
        listOf("Tümü") + allAssets.map { it.building }.filter { it.isNotBlank() }.distinct()
    }
    val years = remember(allAssets) {
        listOf("Tümü") + allAssets.mapNotNull {
            it.purchaseDate.takeIf { d -> d.length >= 4 }?.substring(0, 4)
        }.distinct().sortedDescending()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // RBAC Role Notice Banner for STAFF or VIEWER
        if (currentRole == UserRole.STAFF) {
            Surface(
                color = CardSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAssigned, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Personel (STAFF): Yalnızca zimmetinizdeki ve biriminizdeki demirbaşlar listelenmektedir.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (currentRole == UserRole.VIEWER) {
            Surface(
                color = CardSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = StatusMaintenance, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "İzleyici (VIEWER) Modu: Salt-okunur erişim. Değişiklik yapılamaz.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Top Search Bar and Action Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Kod, ad, seri no, marka, model, personel ara...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Ara", tint = AccentTeal)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CardSurfaceDark,
                        unfocusedContainerColor = CardSurfaceDark
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Advanced Filter Button with Counter Badge
                BadgedBox(
                    badge = {
                        if (activeCount > 0) {
                            Badge(containerColor = AccentTeal) {
                                Text("$activeCount", color = BackgroundDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeCount > 0) AccentTeal.copy(alpha = 0.2f) else CardSurfaceDark)
                            .border(1.dp, if (activeCount > 0) AccentTeal else BorderDark, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtreler",
                            tint = if (activeCount > 0) AccentTeal else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Excel Export Options Menu Button
                Box {
                    IconButton(
                        onClick = { showExportMenu = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardSurfaceDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Dışa Aktar",
                            tint = TextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false },
                        modifier = Modifier.background(CardSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Filtrelenmiş Listeyi Excel'e Aktar (${assets.size})", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = AccentTeal) },
                            onClick = {
                                showExportMenu = false
                                val result = ExportUtil.exportToXlsx(context, assets, "Filtrelenmiş Liste")
                                result.onSuccess { file ->
                                    viewModel.showMessage("Excel dosyası hazırlandı.")
                                    ExportUtil.shareFile(
                                        context,
                                        file,
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "Demirbaş Listesi Excel"
                                    )
                                }.onFailure { err ->
                                    viewModel.showMessage("Excel oluşturulamadı: ${err.localizedMessage}")
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Tüm Demirbaşları Excel'e Aktar (${allAssets.size})", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = AccentBlue) },
                            onClick = {
                                showExportMenu = false
                                val result = ExportUtil.exportToXlsx(context, allAssets, "Tüm Demirbaşlar")
                                result.onSuccess { file ->
                                    viewModel.showMessage("Excel dosyası hazırlandı.")
                                    ExportUtil.shareFile(
                                        context,
                                        file,
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "Tüm Demirbaşlar Excel"
                                    )
                                }.onFailure { err ->
                                    viewModel.showMessage("Excel oluşturulamadı: ${err.localizedMessage}")
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Card / Table View Toggle
                IconButton(
                    onClick = { isTableView = !isTableView },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardSurfaceDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = if (isTableView) Icons.Default.ViewAgenda else Icons.Default.TableRows,
                        contentDescription = "Görünüm Değiştir",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = filterState.selectedStatus == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("Tümü (${allAssets.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentTeal,
                            selectedLabelColor = BackgroundDark,
                            containerColor = CardSurfaceDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filterState.selectedStatus == null,
                            borderColor = BorderDark,
                            selectedBorderColor = AccentTeal
                        )
                    )
                }

                AssetStatus.entries.forEach { status ->
                    item {
                        FilterChip(
                            selected = filterState.selectedStatus == status.name,
                            onClick = {
                                if (filterState.selectedStatus == status.name) {
                                    viewModel.setStatusFilter(null)
                                } else {
                                    viewModel.setStatusFilter(status.name)
                                }
                            },
                            label = { Text(status.labelTr, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = BackgroundDark,
                                containerColor = CardSurfaceDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterState.selectedStatus == status.name,
                                borderColor = BorderDark,
                                selectedBorderColor = AccentTeal
                            )
                        )
                    }
                }
            }
        }

        // Active Filter Indicators Bar
        if (activeCount > 0) {
            Surface(
                color = CardSurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Aktif Filtre ($activeCount): ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                        Text(
                            text = listOfNotNull(
                                filterState.selectedCategory?.let { "Kategori: $it" },
                                filterState.selectedDepartment?.let { "Bölüm: $it" },
                                filterState.selectedBrand?.let { "Marka: $it" },
                                filterState.selectedAssignedPerson?.let { "Personel: $it" },
                                filterState.selectedLocation?.let { "Konum: $it" },
                                filterState.purchaseYear?.let { "Yıl: $it" },
                                filterState.warrantyFilter.takeIf { it != WarrantyFilter.ALL }?.labelTr
                            ).joinToString(", "),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Temizle", fontSize = 12.sp, color = StatusFaulty)
                    }
                }
            }
            HorizontalDivider(color = BorderDark)
        }

        // Header Count & Add Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${assets.size} demirbaş listeleniyor",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            // Add Asset Button: Only ADMIN and MANAGER have permission to create
            if (currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ASSET_ADD_EDIT) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Demirbaş Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                }
            }
        }

        // Content: Empty State, Table View, or Mobile Card View
        if (assets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kriterlere Uygun Demirbaş Bulunamadı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Arama terimini veya filtre seçimlerini temizleyerek tekrar deneyin.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearFilters() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Text("Filtreleri Temizle", color = BackgroundDark)
                    }
                }
            }
        } else if (isTableView) {
            // Clean Horizontal Scroll Table View (for desktop / tablets / landscape)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    val scrollState = rememberScrollState()
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.horizontalScroll(scrollState)) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .background(SurfaceDark)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text("Kod", style = CodeTextStyle, modifier = Modifier.width(130.dp))
                                Text("Demirbaş Adı", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(190.dp))
                                Text("Kategori", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(140.dp))
                                Text("Marka / Model", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(150.dp))
                                Text("Zimmetli Kişi", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(160.dp))
                                Text("Konum", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(130.dp))
                                Text("Durum", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(110.dp))
                            }
                            HorizontalDivider(color = BorderDark)

                            // Table Rows
                            assets.forEach { asset ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.navigateTo(AppScreen.ASSET_DETAIL, asset.assetCode) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(asset.assetCode, style = CodeTextStyle, modifier = Modifier.width(130.dp))
                                    Text(asset.assetName, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.width(190.dp))
                                    Text(asset.category, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(140.dp))
                                    Text("${asset.brand} ${asset.model}".trim(), fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(150.dp))
                                    Text(asset.assignedUserName.ifBlank { "Boşta" }, fontSize = 13.sp, color = if (asset.assignedUserName.isNotBlank()) StatusAssigned else TextSecondary, modifier = Modifier.width(160.dp))
                                    Text("${asset.building} ${asset.room}", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(130.dp))
                                    Box(modifier = Modifier.width(110.dp)) {
                                        StatusBadge(status = AssetStatus.fromString(asset.status))
                                    }
                                }
                                HorizontalDivider(color = BorderDark.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        } else {
            // Clean Mobile Card View (Requirement 4: Photo/icon, Name, Code, Category, Location, Assigned Person, Status)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(assets) { asset ->
                    AssetCleanCard(
                        asset = asset,
                        canEdit = currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER,
                        onClick = { viewModel.navigateTo(AppScreen.ASSET_DETAIL, asset.assetCode) },
                        onQrClick = { selectedAssetForQr = asset },
                        onAssignClick = { selectedAssetForAssign = asset }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }

    // Modal Comprehensive Advanced Filter Dialog
    if (showFilterDialog) {
        AdvancedFilterDialog(
            currentFilters = filterState,
            categories = categories,
            departments = departments,
            brands = brands,
            locations = locations,
            years = years,
            employees = employees,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilters ->
                viewModel.updateFilters(newFilters)
                showFilterDialog = false
            },
            onReset = {
                viewModel.clearFilters()
                showFilterDialog = false
            }
        )
    }

    // QR & Assignment Dialogs
    selectedAssetForQr?.let { asset ->
        QrCodeDialog(
            asset = asset,
            onDismiss = { selectedAssetForQr = null },
            onPrintLabel = { selectedAssetForLabel = asset }
        )
    }

    selectedAssetForLabel?.let { asset ->
        PrintLabelDialog(
            asset = asset,
            onDismiss = { selectedAssetForLabel = null }
        )
    }

    selectedAssetForAssign?.let { asset ->
        ZimmetleDialog(
            asset = asset,
            employees = employees,
            onDismiss = { selectedAssetForAssign = null },
            onConfirm = { emp, retDate, notes ->
                viewModel.assignAsset(asset, emp, retDate, notes)
                selectedAssetForAssign = null
            }
        )
    }
}

// Clean Asset Card matching Requirement 4
@Composable
fun AssetCleanCard(
    asset: Asset,
    canEdit: Boolean,
    onClick: () -> Unit,
    onQrClick: () -> Unit,
    onAssignClick: () -> Unit
) {
    val statusEnum = remember(asset.status) { AssetStatus.fromString(asset.status) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category Icon, Name, Code, and Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (asset.category) {
                                "Dizüstü Bilgisayar", "Masaüstü Bilgisayar" -> Icons.Default.Computer
                                "Monitör" -> Icons.Default.Tv
                                "Yazıcı" -> Icons.Default.Print
                                "Ağ Ekipmanı" -> Icons.Default.Router
                                "Mobilya" -> Icons.Default.Chair
                                "Telefon", "Tablet" -> Icons.Default.Smartphone
                                else -> Icons.Default.Inventory2
                            },
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = asset.assetName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.assetCode,
                                style = SmallCodeTextStyle
                            )
                            Text(
                                text = " • ${asset.category}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = statusEnum)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderDark.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Sub row: Location & Assigned Person & Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Location: Building, Room
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(13.dp), tint = TextMuted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${asset.department} • ${asset.building} ${asset.room}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    // Assigned Person
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (asset.assignedUserName.isNotBlank()) StatusAssigned else TextMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (asset.assignedUserName.isNotBlank()) "Zimmetli: ${asset.assignedUserName}" else "Boşta (Zimmetsiz)",
                            fontSize = 12.sp,
                            color = if (asset.assignedUserName.isNotBlank()) StatusAssigned else TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR Kod",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (canEdit && statusEnum == AssetStatus.AVAILABLE) {
                        IconButton(
                            onClick = onAssignClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.AssignmentInd,
                                contentDescription = "Zimmetle",
                                tint = StatusAssigned,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Detay",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedFilterDialog(
    currentFilters: FilterState,
    categories: List<String>,
    departments: List<String>,
    brands: List<String>,
    locations: List<String>,
    years: List<String>,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit,
    onReset: () -> Unit
) {
    var tempStatus by remember { mutableStateOf(currentFilters.selectedStatus) }
    var tempCategory by remember { mutableStateOf(currentFilters.selectedCategory) }
    var tempDepartment by remember { mutableStateOf(currentFilters.selectedDepartment) }
    var tempBrand by remember { mutableStateOf(currentFilters.selectedBrand) }
    var tempPerson by remember { mutableStateOf(currentFilters.selectedAssignedPerson) }
    var tempLocation by remember { mutableStateOf(currentFilters.selectedLocation) }
    var tempYear by remember { mutableStateOf(currentFilters.purchaseYear) }
    var tempWarranty by remember { mutableStateOf(currentFilters.warrantyFilter) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gelişmiş Filtreleme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextPrimary)
                    }
                }

                HorizontalDivider(color = BorderDark)

                // Scrollable filter controls
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. Kategori
                    item {
                        FilterDropdownSelector(
                            label = "Kategori",
                            options = categories,
                            selected = tempCategory ?: "Tümü",
                            onSelect = { tempCategory = if (it == "Tümü") null else it }
                        )
                    }

                    // 2. Departman
                    item {
                        FilterDropdownSelector(
                            label = "Bölüm / Departman",
                            options = departments,
                            selected = tempDepartment ?: "Tümü",
                            onSelect = { tempDepartment = if (it == "Tümü") null else it }
                        )
                    }

                    // 3. Marka
                    item {
                        FilterDropdownSelector(
                            label = "Marka",
                            options = brands,
                            selected = tempBrand ?: "Tümü",
                            onSelect = { tempBrand = if (it == "Tümü") null else it }
                        )
                    }

                    // 4. Zimmetli Personel
                    item {
                        val personOptions = remember(employees) {
                            listOf("Tümü") + employees.map { it.fullName }
                        }
                        FilterDropdownSelector(
                            label = "Zimmetli Personel",
                            options = personOptions,
                            selected = tempPerson ?: "Tümü",
                            onSelect = { tempPerson = if (it == "Tümü") null else it }
                        )
                    }

                    // 5. Konum / Bina
                    item {
                        FilterDropdownSelector(
                            label = "Bina / Konum",
                            options = locations,
                            selected = tempLocation ?: "Tümü",
                            onSelect = { tempLocation = if (it == "Tümü") null else it }
                        )
                    }

                    // 6. Satın Alma Yılı
                    item {
                        FilterDropdownSelector(
                            label = "Satın Alma Yılı",
                            options = years,
                            selected = tempYear ?: "Tümü",
                            onSelect = { tempYear = if (it == "Tümü") null else it }
                        )
                    }

                    // 7. Garanti Durumu
                    item {
                        Text("Garanti Bitiş Durumu", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        WarrantyFilter.entries.forEach { wf ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tempWarranty = wf }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = tempWarranty == wf,
                                    onClick = { tempWarranty = wf },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentTeal)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(wf.labelTr, fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderDark)

                // Bottom Actions: Reset & Apply
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Text("Sıfırla", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val newFilters = FilterState(
                                selectedStatus = tempStatus,
                                selectedCategory = tempCategory,
                                selectedDepartment = tempDepartment,
                                selectedBrand = tempBrand,
                                selectedAssignedPerson = tempPerson,
                                selectedLocation = tempLocation,
                                purchaseYear = tempYear,
                                warrantyFilter = tempWarranty
                            )
                            onApply(newFilters)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Filtreleri Uygula", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selected, fontSize = 13.sp, color = TextPrimary, maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .background(CardSurfaceElevated)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontSize = 13.sp, color = TextPrimary) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
