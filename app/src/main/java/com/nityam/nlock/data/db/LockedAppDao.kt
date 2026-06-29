package com.nityam.nlock.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the [LockedAppEntity] table.
 *
 * All observe* methods return [Flow] for reactive updates.
 * All write methods are suspend functions for structured concurrency.
 */
@Dao
internal interface LockedAppDao {

    @Query("SELECT * FROM locked_apps")
    fun observeAll(): Flow<List<LockedAppEntity>>

    @Query("SELECT * FROM locked_apps WHERE is_locked = 1")
    fun observeLockedApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT package_name FROM locked_apps WHERE is_locked = 1")
    fun observeLockedPackageNames(): Flow<List<String>>

    @Query("SELECT * FROM locked_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): LockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: LockedAppEntity)

    @Delete
    suspend fun delete(app: LockedAppEntity)
}
