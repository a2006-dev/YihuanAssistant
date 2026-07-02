package com.yh.assistant.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ProxyManager {
    const val SERVER_URL = "http://112.124.68.4:3000"

    data class KeyInfo(
        val valid: Boolean = false,
        val name: String = "",
        val useCount: Int = 0,
        val error: String = ""
    )

    data class ServerStatus(
        val online: Boolean = false,
        val name: String = "",
        val userCount: Int = 0,
        val totalRequests: Int = 0,
        val uptime: String = ""
    )

    suspend fun verifyKey(key: String): KeyInfo = withContext(Dispatchers.IO) {
        try {
            val conn = URL("$SERVER_URL/api/key/verify").openConnection() as HttpURLConnection
            conn.setRequestProperty("X-API-Key", key)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode == 200) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                val data = json.optJSONObject("data")
                if (data != null && json.optInt("code") == 0) {
                    KeyInfo(valid = true, name = data.optString("name", ""), useCount = data.optInt("useCount", 0))
                } else KeyInfo(error = "验证失败")
            } else KeyInfo(error = "连接失败")
        } catch (e: Exception) { KeyInfo(error = e.message ?: "未知错误") }
    }

    suspend fun getServerStatus(key: String): ServerStatus = withContext(Dispatchers.IO) {
        try {
            val conn = URL("$SERVER_URL/api/key/verify").openConnection() as HttpURLConnection
            conn.setRequestProperty("X-API-Key", key)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode == 200) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                if (json.optInt("code") == 0) {
                    val data = json.optJSONObject("data")
                    ServerStatus(name = "阿里云", online = true, userCount = 0, totalRequests = 0, uptime = "")
                } else ServerStatus()
            } else ServerStatus()
        } catch (_: Exception) { ServerStatus() }
    }

    private fun fmt(s: Long): String {
        val d = s / 86400; val h = (s % 86400) / 3600; val m = (s % 3600) / 60
        return (if (d > 0) "${d}d " else "") + "${h}h ${m}m"
    }
}