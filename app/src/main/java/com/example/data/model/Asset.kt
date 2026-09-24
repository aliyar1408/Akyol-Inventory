package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AssetStatus(val labelTr: String) {
    AVAILABLE("Boşta"),
    ASSIGNED("Zimmetli"),
    MAINTENANCE("Bakımda"),
    FAULTY("Arızalı"),
    LOST("Kayıp"),
    RETIRED("Hurda");

    companion object {
        fun fromString(value: String): AssetStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.labelTr.equals(value, ignoreCase = true) }
                ?: AVAILABLE
    }
}

enum class AssetCondition(val labelTr: String) {
    EXCELLENT("Mükemmel / Sıfır"),
    GOOD("İyi"),
    FAIR("Orta / Kullanılmış"),
    POOR("Kötü"),
    DAMAGED("Hasarlı");

    companion object {
        fun fromString(value: String): AssetCondition =
            entries.find { it.name.equals(value, ignoreCase = true) || it.labelTr.equals(value, ignoreCase = true) }
                ?: GOOD
    }
}

enum class UserRole(val labelTr: String, val description: String) {
    ADMIN("Yönetici (ADMIN)", "Tam yetki: Demirbaş, zimmet, kullanıcı ve sistem yönetimi"),
    MANAGER("Bölüm Yöneticisi (MANAGER)", "Demirbaş ekleme/düzenleme, zimmetleme, bakım ve raporlama"),
    STAFF("Personel (STAFF)", "Zimmetli demirbaşları görüntüleme ve sayım taraması"),
    VIEWER("İzleyici (VIEWER)", "Yalnızca görüntüleme ve rapor inceleme yetkisi")
}

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey val assetCode: String, // AKY-2026-000001
    val assetName: String,
    val category: String,
    val subcategory: String = "",
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val barcode: String = "",
    val description: String = "",
    val photoUrl: String = "", // Local drawable or URI

    val department: String = "Bilgi İşlem",
    val subDepartment: String = "",

    val assignedUserId: String = "",
    val assignedUserName: String = "",
    val assignmentDate: String = "",
    val expectedReturnDate: String = "",

    val status: String = AssetStatus.AVAILABLE.name,
    val condition: String = AssetCondition.GOOD.name,

    val branch: String = "Merkez Ofis",
    val building: String = "A Blok",
    val floor: String = "Kat 3",
    val room: String = "No: 304",
    val locationDescription: String = "Bilgi İşlem Operasyon Odası",

    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,

    val purchaseDate: String = "2024-01-15",
    val purchasePrice: Double = 0.0,
    val supplier: String = "",
    val invoiceNumber: String = "",

    val warrantyStartDate: String = "2024-01-15",
    val warrantyEndDate: String = "2027-01-15",

    val lastMaintenanceDate: String = "",
    val nextMaintenanceDate: String = "",

    val notes: String = "",
    val createdBy: String = "Sistem Yöneticisi",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "Sistem Yöneticisi",
    val updatedAt: Long = System.currentTimeMillis(),

    val archived: Boolean = false,
    val archiveReason: String = ""
) {
    fun getStatusEnum(): AssetStatus = AssetStatus.fromString(status)
    fun getConditionEnum(): AssetCondition = AssetCondition.fromString(condition)
}

@Entity(tableName = "assignment_history")
data class AssignmentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetCode: String,
    val assetName: String,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val assignedDate: String,
    val returnDate: String = "",
    val expectedReturnDate: String = "",
    val notes: String = "",
    val signatureReceived: Boolean = true,
    val performedBy: String = "Yönetici",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "maintenance_records")
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetCode: String,
    val maintenanceType: String, // Periyodik Bakım, Onarım, Yazılım Güncellemesi, Parça Değişimi
    val description: String,
    val serviceProvider: String,
    val maintenanceDate: String,
    val nextMaintenanceDate: String = "",
    val cost: Double = 0.0,
    val invoiceNumber: String = "",
    val performedBy: String = "",
    val status: String = "Tamamlandı",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetCode: String = "",
    val action: String, // Demirbaş Eklendi, Zimmetlendi, Zimmetten Düşüldü, Konum Güncellendi, Bakım Yapıldı, vb.
    val user: String,
    val oldValue: String = "",
    val newValue: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
        return sdf.format(Date(timestamp))
    }
}

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeNumber: String,
    val name: String,
    val surname: String,
    val email: String,
    val phone: String,
    val department: String,
    val title: String,
    val active: Boolean = true
) {
    val fullName: String get() = "$name $surname"
}

@Entity(tableName = "audit_items")
data class AuditScanItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionName: String,
    val assetCode: String,
    val assetName: String,
    val expectedLocation: String,
    val scannedLocation: String,
    val status: String, // BULUNAN, BEKLEYEN, YANLIS_KONUM, BULUNAMAYAN
    val scannedBy: String,
    val scanTime: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class AiAssetSuggestion(
    val objectType: String? = null,
    val assetName: String? = null,
    val category: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val visibleSerialNumber: String? = null,
    val visibleModelNumber: String? = null,
    val color: String? = null,
    val conditionDescription: String? = null,
    val suggestedDescription: String? = null,
    val confidenceScore: Float = 0.92f
)
