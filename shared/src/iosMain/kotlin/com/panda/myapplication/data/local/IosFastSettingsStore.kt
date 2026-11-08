package com.panda.myapplication.data.local

import platform.Foundation.NSUserDefaults

class IosFastSettingsStore : FastSettingsStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String, default: String?): String? {
        return defaults.stringForKey(key) ?: default
    }

    override fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}
