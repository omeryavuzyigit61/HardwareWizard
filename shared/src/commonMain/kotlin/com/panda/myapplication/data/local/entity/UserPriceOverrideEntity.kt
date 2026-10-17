package com.panda.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local Price Override Entity (BÖLÜM 2: Kullanıcı Özel Fiyatı Motoru).
 * Independent of `hardware_items` cascade deletes so user custom prices survive remote catalog refreshes.
 */
@Entity(tableName = "user_price_overrides")
data class UserPriceOverrideEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "custom_price_try") val customPriceTry: Double,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
