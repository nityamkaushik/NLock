package com.nityam.nlock.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for NLock.
 *
 * Contains a single table: [LockedAppEntity].
 * Schema is exported to `app/schemas/` for migration testing.
 */
@Database(
    entities = [LockedAppEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class NLockDatabase : RoomDatabase() {
    internal abstract fun lockedAppDao(): LockedAppDao
}
