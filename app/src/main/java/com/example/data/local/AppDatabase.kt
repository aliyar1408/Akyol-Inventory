package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.Asset
import com.example.data.model.AssignmentRecord
import com.example.data.model.AuditScanItem
import com.example.data.model.Employee
import com.example.data.model.MaintenanceRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Asset::class,
        AssignmentRecord::class,
        MaintenanceRecord::class,
        ActivityLog::class,
        Employee::class,
        AuditScanItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akyol_inventory_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.assetDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: AssetDao) {
                dao.insertEmployees(DemoData.employees)
                dao.insertAssets(DemoData.assets)
                DemoData.assignments.forEach { dao.insertAssignment(it) }
                DemoData.maintenance.forEach { dao.insertMaintenance(it) }
                DemoData.activityLogs.forEach { dao.insertActivityLog(it) }
            }
        }
    }
}
