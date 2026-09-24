package com.example.data.repository

import com.example.data.local.AssetDao
import com.example.data.model.ActivityLog
import com.example.data.model.Asset
import com.example.data.model.AssetStatus
import com.example.data.model.AssignmentRecord
import com.example.data.model.AuditScanItem
import com.example.data.model.Employee
import com.example.data.model.MaintenanceRecord
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AssetRepository(private val assetDao: AssetDao) {

    val allAssets: Flow<List<Asset>> = assetDao.getAllAssets()
    val allEmployees: Flow<List<Employee>> = assetDao.getAllEmployees()
    val allAssignments: Flow<List<AssignmentRecord>> = assetDao.getAllAssignments()
    val allMaintenance: Flow<List<MaintenanceRecord>> = assetDao.getAllMaintenance()
    val recentActivities: Flow<List<ActivityLog>> = assetDao.getRecentActivities()

    fun getAssetByCode(code: String): Flow<Asset?> = assetDao.getAssetByCode(code)

    fun searchAssets(query: String): Flow<List<Asset>> = assetDao.searchAssets(query)

    fun getAssetsByEmployee(empNumber: String): Flow<List<Asset>> =
        assetDao.getAssetsByEmployee(empNumber)

    fun getAssignmentsForAsset(code: String): Flow<List<AssignmentRecord>> =
        assetDao.getAssignmentsForAsset(code)

    fun getMaintenanceForAsset(code: String): Flow<List<MaintenanceRecord>> =
        assetDao.getMaintenanceForAsset(code)

    fun getActivitiesForAsset(code: String): Flow<List<ActivityLog>> =
        assetDao.getActivitiesForAsset(code)

    fun getAuditItems(sessionName: String): Flow<List<AuditScanItem>> =
        assetDao.getAuditItems(sessionName)

    suspend fun generateNextAssetCode(categoryPrefix: String? = null): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        return if (!categoryPrefix.isNullOrBlank()) {
            val count = assetDao.getCountByPrefix(categoryPrefix) + 1
            String.format(Locale.ROOT, "%s-%05d", categoryPrefix, count)
        } else {
            val count = assetDao.getCountByYear(currentYear) + 1
            String.format(Locale.ROOT, "AKY-%s-%06d", currentYear, count)
        }
    }

    suspend fun createAsset(asset: Asset, user: String) {
        assetDao.insertAsset(asset)
        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = asset.assetCode,
                action = "Demirbaş Oluşturuldu",
                user = user,
                oldValue = "-",
                newValue = "${asset.assetName} (${asset.assetCode})"
            )
        )
    }

    suspend fun updateAsset(asset: Asset, user: String, changeDesc: String = "Bilgiler güncellendi") {
        assetDao.updateAsset(asset)
        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = asset.assetCode,
                action = "Demirbaş Düzenlendi",
                user = user,
                oldValue = "",
                newValue = changeDesc
            )
        )
    }

    suspend fun deleteAsset(code: String, user: String) {
        assetDao.deleteAsset(code)
        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = code,
                action = "Demirbaş Silindi",
                user = user,
                oldValue = code,
                newValue = "Kayıt arşivden kaldırıldı"
            )
        )
    }

    suspend fun assignAsset(
        asset: Asset,
        employee: Employee,
        expectedReturnDate: String,
        notes: String,
        user: String
    ) {
        val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date())
        val updated = asset.copy(
            status = AssetStatus.ASSIGNED.name,
            assignedUserId = employee.employeeNumber,
            assignedUserName = employee.fullName,
            department = employee.department,
            assignmentDate = todayStr,
            expectedReturnDate = expectedReturnDate,
            updatedBy = user,
            updatedAt = System.currentTimeMillis()
        )
        assetDao.updateAsset(updated)

        val record = AssignmentRecord(
            assetCode = asset.assetCode,
            assetName = asset.assetName,
            employeeId = employee.employeeNumber,
            employeeName = employee.fullName,
            department = employee.department,
            assignedDate = todayStr,
            expectedReturnDate = expectedReturnDate,
            notes = notes,
            signatureReceived = true,
            performedBy = user
        )
        assetDao.insertAssignment(record)

        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = asset.assetCode,
                action = "Zimmetlendi",
                user = user,
                oldValue = asset.assignedUserName.ifBlank { "Boşta" },
                newValue = employee.fullName
            )
        )
    }

    suspend fun returnAsset(asset: Asset, notes: String, user: String) {
        val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).format(Date())
        val previousPerson = asset.assignedUserName

        val updated = asset.copy(
            status = AssetStatus.AVAILABLE.name,
            assignedUserId = "",
            assignedUserName = "",
            assignmentDate = "",
            expectedReturnDate = "",
            updatedBy = user,
            updatedAt = System.currentTimeMillis()
        )
        assetDao.updateAsset(updated)

        val record = AssignmentRecord(
            assetCode = asset.assetCode,
            assetName = asset.assetName,
            employeeId = asset.assignedUserId,
            employeeName = previousPerson,
            department = asset.department,
            assignedDate = asset.assignmentDate,
            returnDate = todayStr,
            notes = "Zimmet İadesi: $notes",
            signatureReceived = true,
            performedBy = user
        )
        assetDao.insertAssignment(record)

        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = asset.assetCode,
                action = "Zimmetten Düşüldü",
                user = user,
                oldValue = previousPerson,
                newValue = "Boşta"
            )
        )
    }

    suspend fun addMaintenance(asset: Asset, record: MaintenanceRecord, user: String) {
        assetDao.insertMaintenance(record)

        val updated = asset.copy(
            lastMaintenanceDate = record.maintenanceDate,
            nextMaintenanceDate = record.nextMaintenanceDate,
            status = if (record.status.contains("Devam", true)) AssetStatus.MAINTENANCE.name else asset.status,
            updatedBy = user,
            updatedAt = System.currentTimeMillis()
        )
        assetDao.updateAsset(updated)

        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = asset.assetCode,
                action = "Bakım Kaydı Eklendi",
                user = user,
                oldValue = "-",
                newValue = "${record.maintenanceType} - ${record.serviceProvider} (${record.cost} ₺)"
            )
        )
    }

    suspend fun updateAssetLocation(
        asset: Asset,
        branch: String,
        building: String,
        floor: String,
        room: String,
        lat: Double,
        lng: Double,
        user: String
    ) {
        val oldLoc = "${asset.building} ${asset.floor} ${asset.room}"
        val newLoc = "$building $floor $room"
        val updated = asset.copy(
            branch = branch,
            building = building,
            floor = floor,
            room = room,
            latitude = lat,
            longitude = lng,
            updatedBy = user,
            updatedAt = System.currentTimeMillis()
        )
        assetDao.updateAsset(updated)

        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = asset.assetCode,
                action = "Konum Güncellendi",
                user = user,
                oldValue = oldLoc,
                newValue = newLoc
            )
        )
    }

    suspend fun recordAuditScan(item: AuditScanItem) {
        assetDao.insertAuditItem(item)
    }

    suspend fun addEmployee(employee: Employee) {
        assetDao.insertEmployee(employee)
    }

    suspend fun importAssetsFromList(list: List<Asset>, user: String): Int {
        var count = 0
        list.forEach {
            assetDao.insertAsset(it)
            count++
        }
        assetDao.insertActivityLog(
            ActivityLog(
                assetCode = "-",
                action = "Toplu İçe Aktarım",
                user = user,
                oldValue = "-",
                newValue = "$count adet demirbaş Excel'den başarıyla aktarıldı"
            )
        )
        return count
    }
}
