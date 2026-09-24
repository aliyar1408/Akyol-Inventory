package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLog
import com.example.data.model.Asset
import com.example.data.model.AssignmentRecord
import com.example.data.model.AuditScanItem
import com.example.data.model.Employee
import com.example.data.model.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    // --- ASSETS ---
    @Query("SELECT * FROM assets WHERE archived = 0 ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE assetCode = :code LIMIT 1")
    fun getAssetByCode(code: String): Flow<Asset?>

    @Query("SELECT * FROM assets WHERE assetCode = :code LIMIT 1")
    suspend fun getAssetByCodeDirect(code: String): Asset?

    @Query("""
        SELECT * FROM assets 
        WHERE archived = 0 
        AND (assetCode LIKE '%' || :query || '%' 
          OR assetName LIKE '%' || :query || '%' 
          OR serialNumber LIKE '%' || :query || '%'
          OR brand LIKE '%' || :query || '%'
          OR model LIKE '%' || :query || '%'
          OR assignedUserName LIKE '%' || :query || '%'
          OR department LIKE '%' || :query || '%'
          OR room LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchAssets(query: String): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE status = :status AND archived = 0 ORDER BY createdAt DESC")
    fun getAssetsByStatus(status: String): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE department = :dept AND archived = 0 ORDER BY createdAt DESC")
    fun getAssetsByDepartment(dept: String): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE assignedUserId = :employeeNumber AND archived = 0")
    fun getAssetsByEmployee(employeeNumber: String): Flow<List<Asset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: Asset)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<Asset>)

    @Update
    suspend fun updateAsset(asset: Asset)

    @Query("DELETE FROM assets WHERE assetCode = :code")
    suspend fun deleteAsset(code: String)

    @Query("SELECT COUNT(*) FROM assets WHERE assetCode LIKE 'AKY-' || :year || '-%'")
    suspend fun getCountByYear(year: String): Int

    @Query("SELECT COUNT(*) FROM assets WHERE assetCode LIKE :prefix || '-%'")
    suspend fun getCountByPrefix(prefix: String): Int

    // --- ASSIGNMENTS ---
    @Query("SELECT * FROM assignment_history WHERE assetCode = :assetCode ORDER BY timestamp DESC")
    fun getAssignmentsForAsset(assetCode: String): Flow<List<AssignmentRecord>>

    @Query("SELECT * FROM assignment_history ORDER BY timestamp DESC")
    fun getAllAssignments(): Flow<List<AssignmentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(record: AssignmentRecord)

    // --- MAINTENANCE ---
    @Query("SELECT * FROM maintenance_records WHERE assetCode = :assetCode ORDER BY timestamp DESC")
    fun getMaintenanceForAsset(assetCode: String): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records ORDER BY timestamp DESC")
    fun getAllMaintenance(): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(record: MaintenanceRecord)

    // --- ACTIVITY LOGS ---
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentActivities(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE assetCode = :assetCode ORDER BY timestamp DESC")
    fun getActivitiesForAsset(assetCode: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog)

    // --- EMPLOYEES ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    // --- AUDIT ITEMS ---
    @Query("SELECT * FROM audit_items WHERE sessionName = :sessionName ORDER BY scanTime DESC")
    fun getAuditItems(sessionName: String): Flow<List<AuditScanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditItem(item: AuditScanItem)

    @Query("DELETE FROM audit_items WHERE sessionName = :sessionName")
    suspend fun clearAuditSession(sessionName: String)
}
