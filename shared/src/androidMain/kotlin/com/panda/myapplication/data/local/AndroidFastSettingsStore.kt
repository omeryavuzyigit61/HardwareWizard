package com.panda.myapplication.data.local

import android.content.Context
import androidx.core.content.edit

class AndroidFastSettingsStore(context: Context) : FastSettingsStore {
    private val prefs = context.getSharedPreferences("hardware_wizard_fast_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String, default: String?): String? {
        return prefs.getString(key, default)
    }

    override fun setString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }
}
