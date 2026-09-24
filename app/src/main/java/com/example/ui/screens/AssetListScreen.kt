package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(viewModel: InventoryViewModel) {
    val assets by viewModel.filteredAssets.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var isGridView by remember { mutableStateOf(false) }
    var selectedAssetForQr by remember { mutableStateOf<Asset?>(null) }
    var selectedAssetForLabel by remember { mutableStateOf<Asset?>(null) }
    var selectedAssetForAssign by remember { mutableStateOf<Asset?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // RBAC Role Notice Banner for STAFF or VIEWER
        if (currentRole == UserRole.STAFF) {
            Surface(
                color = StatusAssigned.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAssigned, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Personel (STAFF) Görünümü: Yalnızca zimmetinizdeki ve Bilgi İşlem demirbaşları listelenmektedir.",
                        fontSize = 11.sp,
                        color = StatusAssigned,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (currentRole == UserRole.VIEWER) {
            Surface(
                color = WarningAmber.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "İzleyici (VIEWER) Modu: Salt okunur erişim. Ekleme, düzenleme ve zimmetleme devre dışıdır.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Search Bar and Quick Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Kod, ad, seri no, marka, model, personel, konum ara...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Ara", tint = TurquoiseDark)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TurquoisePrimary,
                        unfocusedBorderColor = NeutralCardBorder
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Advanced Filter Button with Counter Badge
                BadgedBox(
                    badge = {
                        if (activeCount > 0) {
                            Badge(containerColor = TurquoiseDark) {
                                Text("$activeCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeCount > 0) TurquoiseLight.copy(alpha = 0.25f) else NeutralCardBorder.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Gelişmiş Filtreler",
                            tint = if (activeCount > 0) TurquoiseDark else NavyDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Grid / Table View Toggle
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeutralCardBorder.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Görünüm Değiştir",
                        tint = NavyDark
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
                        label = { Text("Tümü (${allAssets.size})", fontSize = 12.sp) }
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
                            label = { Text(status.labelTr, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Active Filter Indicators Bar
        if (activeCount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TurquoiseDark
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
                            fontSize = 11.sp,
                            color = TextSecondaryLight,
                            maxLines = 1
                        )
                    }

                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Temizle", fontSize = 11.sp, color = CriticalCoral)
                    }
                }
            }
            HorizontalDivider(color = NeutralCardBorder)
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
                color = TextSecondaryLight,
                fontWeight = FontWeight.Medium
            )

            // Add Asset Button: Only ADMIN and MANAGER have permission to create
            if (currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ASSET_ADD_EDIT) },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Demirbaş Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Content: Grid or List View
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
                        modifier = Modifier.size(64.dp),
                        tint = TextMutedLight
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aranan Kriterlere Uygun Demirbaş Bulunamadı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Filtreleri veya arama terimini değiştirerek tekrar deneyin.",
                        fontSize = 13.sp,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearFilters() },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark)
                    ) {
                        Text("Tüm Filtreleri Temizle")
                    }
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(assets) { asset ->
                    AssetGridCard(
                        asset = asset,
                        onClick = { viewModel.navigateTo(AppScreen.ASSET_DETAIL, asset.assetCode) },
                        onQrClick = { selectedAssetForQr = asset }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(assets) { asset ->
                    AssetListCard(
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = TurquoiseDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gelişmiş Filtreleme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = NavyDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                HorizontalDivider(color = NeutralCardBorder)

                // Scrollable filter controls
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        Text("Garanti Bitiş Durumu", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
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
                                    onClick = { tempWarranty = wf }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(wf.labelTr, fontSize = 13.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = NeutralCardBorder)

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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sıfırla")
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
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Filtreleri Uygula")
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
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selected, fontSize = 13.sp, maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontSize = 13.sp) },
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

@Composable
fun AssetListCard(
    asset: Asset,
    canEdit: Boolean,
    onClick: () -> Unit,
    onQrClick: () -> Unit,
    onAssignClick: () -> Unit
) {
    val statusEnum = remember(asset.status) { AssetStatus.fromString(asset.status) }
    val conditionEnum = remember(asset.condition) { AssetCondition.fromString(asset.condition) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Code, Name, Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = TurquoiseLight.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
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
                                tint = TurquoiseDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.assetCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TurquoiseDark
                            )
                            if (asset.serialNumber.isNotBlank()) {
                                Text(
                                    text = " • SN: ${asset.serialNumber}",
                                    fontSize = 11.sp,
                                    color = TextMutedLight
                                )
                            }
                        }
                        Text(
                            text = asset.assetName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDark,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = statusEnum)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub info: Category, Brand/Model, Condition
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${asset.category} • ${asset.brand} ${asset.model}".trim().removePrefix("•"),
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    maxLines = 1
                )
                ConditionBadge(condition = conditionEnum)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = NeutralCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Department / Assigned person / Location & Action icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = TextMutedLight
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${asset.department} • ${asset.building} ${asset.room}",
                            fontSize = 11.sp,
                            color = TextSecondaryLight,
                            maxLines = 1
                        )
                    }

                    if (asset.assignedUserName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = StatusAssigned
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Zimmetli: ${asset.assignedUserName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = StatusAssigned,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR Kod",
                            tint = NavyDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (canEdit && statusEnum == AssetStatus.AVAILABLE) {
                        IconButton(
                            onClick = onAssignClick,
                            modifier = Modifier.size(34.dp)
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
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Detay",
                            tint = TextMutedLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssetGridCard(
    asset: Asset,
    onClick: () -> Unit,
    onQrClick: () -> Unit
) {
    val statusEnum = remember(asset.status) { AssetStatus.fromString(asset.status) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = TurquoiseLight.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (asset.category) {
                                "Dizüstü Bilgisayar", "Masaüstü Bilgisayar" -> Icons.Default.Computer
                                "Monitör" -> Icons.Default.Tv
                                "Yazıcı" -> Icons.Default.Print
                                "Ağ Ekipmanı" -> Icons.Default.Router
                                "Mobilya" -> Icons.Default.Chair
                                else -> Icons.Default.Inventory2
                            },
                            contentDescription = null,
                            tint = TurquoiseDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onQrClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = "QR Kod",
                        tint = NavyDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = asset.assetCode,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TurquoiseDark
            )

            Text(
                text = asset.assetName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDark,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${asset.brand} ${asset.model}".trim(),
                fontSize = 11.sp,
                color = TextSecondaryLight,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusBadge(status = statusEnum)

            if (asset.assignedUserName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = asset.assignedUserName,
                    fontSize = 10.sp,
                    color = StatusAssigned,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
