package com.nityam.nlock

import android.app.Application
import androidx.room.Room
import com.nityam.nlock.data.db.NLockDatabase
import com.nityam.nlock.data.preferences.AppPreferences
import com.nityam.nlock.data.preferences.dataStore
import com.nityam.nlock.data.repository.AppLockRepository
import com.nityam.nlock.security.PinHashManager

/**
 * Application class that initializes all singletons.
 *
 * Manual dependency injection — no DI framework. Every component
 * that needs shared dependencies receives them from here.
 */
internal class NLockApplication : Application() {

    internal lateinit var database: NLockDatabase
        private set

    internal lateinit var preferences: AppPreferences
        private set

    internal lateinit var repository: AppLockRepository
        private set

    internal lateinit var pinHashManager: PinHashManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            context = applicationContext,
            klass = NLockDatabase::class.java,
            name = DATABASE_NAME,
        ).build()

        preferences = AppPreferences(dataStore = applicationContext.dataStore)

        repository = AppLockRepository(
            dao = database.lockedAppDao(),
            preferences = preferences,
        )

        pinHashManager = PinHashManager()
    }

    private companion object {
        const val DATABASE_NAME = "nlock_database"
    }
}
