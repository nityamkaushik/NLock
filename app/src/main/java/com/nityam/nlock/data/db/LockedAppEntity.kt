package com.nityam.nlock.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app that the user has added to the lock list.
 *
 * Stored in Room. The [packageName] is the primary key because each app
 * can only appear once in the locked-apps list.
 */
@Entity(tableName = "locked_apps")
internal data class LockedAppEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = true,

    @ColumnInfo(name = "vault_display_name")
    val vaultDisplayName: String? = null,

    @ColumnInfo(name = "vault_icon_uri")
    val vaultIconUri: String? = null,

    @ColumnInfo(name = "grace_period_seconds")
    val gracePeriodSeconds: Int = 0,

    @ColumnInfo(name = "hide_notification_content")
    val hideNotificationContent: Boolean = false,
)
