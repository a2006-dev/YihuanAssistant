package com.yh.assistant

import android.app.Application
import com.yh.assistant.util.PreferenceUtil
import com.yh.assistant.util.CacheManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceUtil.init(this)
        CacheManager.init(this)
        try {
            val nomedia = java.io.File(cacheDir, ".nomedia")
            if (!nomedia.exists()) nomedia.createNewFile()
            val coilDir = java.io.File(cacheDir, "coil")
            if (coilDir.exists()) {
                val n = java.io.File(coilDir, ".nomedia")
                if (!n.exists()) n.createNewFile()
            }
        } catch (_: Exception) {}
    }
}