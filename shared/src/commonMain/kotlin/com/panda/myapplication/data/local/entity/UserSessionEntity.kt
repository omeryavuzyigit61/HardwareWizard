package com.panda.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cihaz üzerinde kalıcı kullanıcı oturumu (Local Persistent User Session).
 * Uygulama kapatılıp açıldığında oturumun korunmasını sağlar.
 */
@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = ACTIVE_SESSION_ID,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "birth_date_iso")
    val birthDateIso: String,

    @ColumnInfo(name = "gender")
    val gender: String,

    @ColumnInfo(name = "spirit_hardware")
    val spiritHardware: String,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
) {
    companion object {
        const val ACTIVE_SESSION_ID = "active_user_session"
    }
}
