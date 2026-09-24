package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.AiAssetSuggestion
import com.example.data.model.Asset
import com.example.data.model.AssetCondition
import com.example.data.model.AssetStatus
import com.example.data.model.AssignmentRecord
import com.example.data.model.AuditScanItem
import com.example.data.model.Employee
import com.example.data.model.MaintenanceRecord
import com.example.data.model.UserRole
import com.example.data.remote.GeminiService
import com.example.data.repository.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    DASHBOARD,
    ASSET_LIST,
    ASSET_DETAIL,
    ASSET_ADD_EDIT,
    AI_CONFIRMATION,
    QR_SCANNER,
    AUDIT,
    REPORTS,
    EMPLOYEES,
    PROFILE_SETTINGS
}

enum class WarrantyFilter(val labelTr: String) {
    ALL("Tüm Garantiler"),
    ACTIVE("Devam Eden Garantiler"),
    EXPIRING_SOON("Garantisi Yakında Bitecekler (6 Ay)"),
    EXPIRED("Garantisi Bitenler")
}

data class FilterState(
    val selectedStatus: String? = null,
    val selectedCategory: String? = null,
    val selectedDepartment: String? = null,
    val selectedAssignedPerson: String? = null,
    val selectedLocation: String? = null,
    val selectedBrand: String? = null,
    val purchaseYear: String? = null,
    val warrantyFilter: WarrantyFilter = WarrantyFilter.ALL
) {
    fun getActiveFilterCount(): Int {
        var count = 0
        if (selectedStatus != null) count++
        if (selectedCategory != null) count++
        if (selectedDepartment != null) count++
        if (selectedAssignedPerson != null) count++
        if (selectedLocation != null) count++
        if (selectedBrand != null) count++
        if (purchaseYear != null) count++
        if (warrantyFilter != WarrantyFilter.ALL) count++
        return count
    }
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AssetRepository(database.assetDao())
    private val geminiService = GeminiService()

    // Current Role
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Navigation Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected Asset for Detail
    private val _selectedAssetCode = MutableStateFlow<String?>(null)
    val selectedAssetCode: StateFlow<String?> = _selectedAssetCode.asStateFlow()

    // Selected Asset Entity
    val selectedAsset: StateFlow<Asset?> = _selectedAssetCode.combine(repository.allAssets) { code, list ->
        list.find { it.assetCode == code }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All Assets
    val allAssets: StateFlow<List<Asset>> = repository.allAssets.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // All Employees
    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // All Assignments
    val allAssignments: StateFlow<List<AssignmentRecord>> = repository.allAssignments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // All Maintenance
    val allMaintenance: StateFlow<List<MaintenanceRecord>> = repository.allMaintenance.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Recent Activities
    val recentActivities: StateFlow<List<ActivityLog>> = repository.recentActivities.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Filtered Assets
    val filteredAssets: StateFlow<List<Asset>> = combine(
        allAssets,
        _searchQuery,
        _filterState,
        _currentRole
    ) { assets, query, filters, role ->
        var list = assets

        // RBAC Filter: STAFF only sees assets assigned to them or their department
        if (role == UserRole.STAFF) {
            list = list.filter { it.assignedUserId == "PER-006" || it.department == "Bilgi İşlem" }
        }

        // Global Instant Search (asset code, name, serial number, brand, model, assigned person, department, location)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase(Locale("tr", "TR"))
            list = list.filter {
                it.assetCode.lowercase(Locale("tr", "TR")).contains(q) ||
                it.assetName.lowercase(Locale("tr", "TR")).contains(q) ||
                it.serialNumber.lowercase(Locale("tr", "TR")).contains(q) ||
                it.brand.lowercase(Locale("tr", "TR")).contains(q) ||
                it.model.lowercase(Locale("tr", "TR")).contains(q) ||
                it.assignedUserName.lowercase(Locale("tr", "TR")).contains(q) ||
                it.department.lowercase(Locale("tr", "TR")).contains(q) ||
                it.branch.lowercase(Locale("tr", "TR")).contains(q) ||
                it.building.lowercase(Locale("tr", "TR")).contains(q) ||
                it.floor.lowercase(Locale("tr", "TR")).contains(q) ||
                it.room.lowercase(Locale("tr", "TR")).contains(q) ||
                it.locationDescription.lowercase(Locale("tr", "TR")).contains(q)
            }
        }

        // Advanced Filter: Status
        filters.selectedStatus?.let { status ->
            list = list.filter { it.status == status }
        }

        // Advanced Filter: Category
        filters.selectedCategory?.let { cat ->
            list = list.filter { it.category == cat }
        }

        // Advanced Filter: Department
        filters.selectedDepartment?.let { dept ->
            list = list.filter { it.department == dept }
        }

        // Advanced Filter: Assigned Person
        filters.selectedAssignedPerson?.let { person ->
            list = list.filter { it.assignedUserName.equals(person, ignoreCase = true) || it.assignedUserId == person }
        }

        // Advanced Filter: Location (Building / Floor / Room)
        filters.selectedLocation?.let { loc ->
            list = list.filter {
                it.building.contains(loc, ignoreCase = true) ||
                it.floor.contains(loc, ignoreCase = true) ||
                it.room.contains(loc, ignoreCase = true)
            }
        }

        // Advanced Filter: Brand
        filters.selectedBrand?.let { brand ->
            list = list.filter { it.brand.equals(brand, ignoreCase = true) }
        }

        // Advanced Filter: Purchase Date / Year
        filters.purchaseYear?.let { year ->
            list = list.filter { it.purchaseDate.startsWith(year) }
        }

        // Advanced Filter: Warranty Expiration
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
        val sixMonthsLaterStr = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(cal.time)

        when (filters.warrantyFilter) {
            WarrantyFilter.ALL -> {}
            WarrantyFilter.ACTIVE -> {
                list = list.filter { it.warrantyEndDate.isNotBlank() && it.warrantyEndDate >= todayStr }
            }
            WarrantyFilter.EXPIRING_SOON -> {
                list = list.filter {
                    it.warrantyEndDate.isNotBlank() &&
                    it.warrantyEndDate >= todayStr &&
                    it.warrantyEndDate <= sixMonthsLaterStr
                }
            }
            WarrantyFilter.EXPIRED -> {
                list = list.filter { it.warrantyEndDate.isNotBlank() && it.warrantyEndDate < todayStr }
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Analysis State
    private val _isAnalyzingAi = MutableStateFlow(false)
    val isAnalyzingAi: StateFlow<Boolean> = _isAnalyzingAi.asStateFlow()

    private val _aiSuggestion = MutableStateFlow<AiAssetSuggestion?>(null)
    val aiSuggestion: StateFlow<AiAssetSuggestion?> = _aiSuggestion.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    // Active Toast / Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Active Audit Session
    val auditSessionName = "2026 Yıllık Demirbaş Sayımı"
    val auditItems: StateFlow<List<AuditScanItem>> = repository.getAuditItems(auditSessionName).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        // Ensure initial seed runs if database empty
        viewModelScope.launch {
            // Checked on database creation
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        showMessage("Kullanıcı rolü değiştirildi: ${role.labelTr}")
    }

    fun navigateTo(screen: AppScreen, assetCode: String? = null) {
        if (assetCode != null) {
            _selectedAssetCode.value = assetCode
        }
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _filterState.value = _filterState.value.copy(selectedStatus = status)
    }

    fun setCategoryFilter(category: String?) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
    }

    fun setDepartmentFilter(dept: String?) {
        _filterState.value = _filterState.value.copy(selectedDepartment = dept)
    }

    fun setAssignedPersonFilter(person: String?) {
        _filterState.value = _filterState.value.copy(selectedAssignedPerson = person)
    }

    fun setLocationFilter(loc: String?) {
        _filterState.value = _filterState.value.copy(selectedLocation = loc)
    }

    fun setBrandFilter(brand: String?) {
        _filterState.value = _filterState.value.copy(selectedBrand = brand)
    }

    fun setPurchaseYearFilter(year: String?) {
        _filterState.value = _filterState.value.copy(purchaseYear = year)
    }

    fun setWarrantyFilter(wf: WarrantyFilter) {
        _filterState.value = _filterState.value.copy(warrantyFilter = wf)
    }

    fun updateFilters(newFilters: FilterState) {
        _filterState.value = newFilters
    }

    fun clearFilters() {
        _filterState.value = FilterState()
        _searchQuery.value = ""
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // --- AI PHOTO SCAN FLOW ---
    fun analyzeAssetPhoto(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
        _isAnalyzingAi.value = true
        viewModelScope.launch {
            try {
                val suggestion = geminiService.analyzeAssetPhoto(bitmap)
                _aiSuggestion.value = suggestion
                _currentScreen.value = AppScreen.AI_CONFIRMATION
            } catch (e: Exception) {
                showMessage("Yapay zeka analizi yapılamadı: ${e.message}")
            } finally {
                _isAnalyzingAi.value = false
            }
        }
    }

    fun clearAiSuggestion() {
        _aiSuggestion.value = null
        _capturedBitmap.value = null
    }

    // --- ASSET CREATION & EDITING ---
    suspend fun generateNextAssetCode(category: String? = null): String {
        val prefix = when (category) {
            "Dizüstü Bilgisayar", "Masaüstü Bilgisayar" -> "AKY-PC"
            "Monitör" -> "AKY-MON"
            "Mobilya" -> "AKY-MOB"
            "Ağ Ekipmanı" -> "AKY-NET"
            "Yazıcı" -> "AKY-PRN"
            "Telefon", "Tablet" -> "AKY-MOB"
            else -> "AKY-2026"
        }
        return repository.generateNextAssetCode(prefix)
    }

    fun saveAsset(asset: Asset, isNew: Boolean) {
        viewModelScope.launch {
            val user = currentRole.value.name
            if (isNew) {
                repository.createAsset(asset, user)
                showMessage("Demirbaş başarıyla kaydedildi: ${asset.assetCode}")
            } else {
                repository.updateAsset(asset, user, "Demirbaş detayları güncellendi")
                showMessage("Demirbaş güncellendi: ${asset.assetCode}")
            }
            _selectedAssetCode.value = asset.assetCode
            _currentScreen.value = AppScreen.ASSET_DETAIL
            clearAiSuggestion()
        }
    }

    fun deleteAsset(code: String) {
        viewModelScope.launch {
            repository.deleteAsset(code, currentRole.value.name)
            showMessage("Demirbaş silindi: $code")
            _currentScreen.value = AppScreen.ASSET_LIST
        }
    }

    // --- ZİMMETLE / RETURN ---
    fun assignAsset(
        asset: Asset,
        employee: Employee,
        expectedReturnDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.assignAsset(
                asset = asset,
                employee = employee,
                expectedReturnDate = expectedReturnDate,
                notes = notes,
                user = currentRole.value.name
            )
            showMessage("${asset.assetCode} demirbaşı ${employee.fullName} personeline zimmetlendi.")
        }
    }

    fun returnAsset(asset: Asset, notes: String) {
        viewModelScope.launch {
            repository.returnAsset(asset, notes, currentRole.value.name)
            showMessage("${asset.assetCode} demirbaşı zimmetten düşüldü ve boşa çıkarıldı.")
        }
    }

    // --- MAINTENANCE ---
    fun addMaintenance(asset: Asset, record: MaintenanceRecord) {
        viewModelScope.launch {
            repository.addMaintenance(asset, record, currentRole.value.name)
            showMessage("Bakım kaydı başarıyla oluşturuldu.")
        }
    }

    // --- LOCATION UPDATE ---
    fun updateLocation(
        asset: Asset,
        branch: String,
        building: String,
        floor: String,
        room: String,
        lat: Double,
        lng: Double
    ) {
        viewModelScope.launch {
            repository.updateAssetLocation(
                asset = asset,
                branch = branch,
                building = building,
                floor = floor,
                room = room,
                lat = lat,
                lng = lng,
                user = currentRole.value.name
            )
            showMessage("Demirbaş konumu güncellendi: $building $floor $room")
        }
    }

    // --- AUDIT SCANNING ---
    fun processAuditScan(scannedCode: String, scannedLocation: String = "A Blok Kat 3") {
        viewModelScope.launch {
            val asset = allAssets.value.find { it.assetCode.equals(scannedCode, ignoreCase = true) || it.barcode == scannedCode }
            if (asset == null) {
                showMessage("Bilinmeyen demirbaş veya barkod: $scannedCode")
                return@launch
            }

            val expected = "${asset.building} ${asset.floor}"
            val status = if (expected.contains(scannedLocation, ignoreCase = true) || scannedLocation.contains(expected, ignoreCase = true)) {
                "BULUNAN"
            } else {
                "YANLIS_KONUM"
            }

            val auditItem = AuditScanItem(
                sessionName = auditSessionName,
                assetCode = asset.assetCode,
                assetName = asset.assetName,
                expectedLocation = expected,
                scannedLocation = scannedLocation,
                status = status,
                scannedBy = currentRole.value.name
            )
            repository.recordAuditScan(auditItem)
            showMessage("${asset.assetCode} tarandı: ${if (status == "BULUNAN") "Doğru Konumda Bulundu" else "Yanlış Konumda!"}")
        }
    }

    // --- EXCEL IMPORT ---
    fun importDemoBulkAssets() {
        viewModelScope.launch {
            val count = repository.importAssetsFromList(
                listOf(
                    Asset(
                        assetCode = "AKY-2026-000022",
                        assetName = "ViewSonic 24\" IPS Kurumsal Monitör",
                        category = "Monitör",
                        brand = "ViewSonic",
                        model = "VG2448",
                        serialNumber = "VS-24-99120",
                        barcode = "869000100022",
                        department = "Muhasebe",
                        status = AssetStatus.AVAILABLE.name,
                        condition = AssetCondition.GOOD.name,
                        branch = "Merkez Ofis",
                        building = "B Blok",
                        floor = "Kat 2",
                        room = "No: 204",
                        purchasePrice = 7500.0
                    ),
                    Asset(
                        assetCode = "AKY-2026-000023",
                        assetName = "Logitech MX Master 3S Kablosuz Mouse",
                        category = "Diğer",
                        brand = "Logitech",
                        model = "MX Master 3S",
                        serialNumber = "LOG-MX-88912",
                        barcode = "869000100023",
                        department = "Bilgi İşlem",
                        assignedUserId = "PER-006",
                        assignedUserName = "Canan Öztürk",
                        status = AssetStatus.ASSIGNED.name,
                        condition = AssetCondition.EXCELLENT.name,
                        branch = "Merkez Ofis",
                        building = "A Blok",
                        floor = "Kat 3",
                        room = "No: 302",
                        purchasePrice = 4200.0
                    )
                ),
                user = currentRole.value.name
            )
            showMessage("$count adet demirbaş içe aktarıldı.")
        }
    }
}
