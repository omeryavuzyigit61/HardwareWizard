package com.panda.myapplication.data.local

interface FastSettingsStore {
    fun getString(key: String, default: String? = null): String?
    fun setString(key: String, value: String)
}
