package com.yh.assistant.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object PreferenceUtil {
    private const val PREFS = "yh_assistant"
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private var accountsFile: File? = null

    data class AccountInfo(
        val userId: Long = 0,
        val token: String = "",
        val accessToken: String = "",
        val refreshToken: String = "",
        val gameRoleName: String = "",
        val gameRoleId: String = ""
    )

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        accountsFile = File(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS),
            ".yh_accounts.json"
        )
        val oldFile = File(ctx.noBackupFilesDir, "yh_accounts.json")
        if (oldFile.exists() && accountsFile != null && !accountsFile!!.exists()) {
            try { oldFile.copyTo(accountsFile!!, overwrite = true) } catch (_: Exception) { }
        }
    }

    fun getPrefs(): SharedPreferences? = if (::prefs.isInitialized) prefs else null

    private const val KEY_PROXY_URL = "proxy_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_USE_OUR_API = "use_our_api"

    fun getProxyUrl(): String = prefs.getString(KEY_PROXY_URL, "") ?: ""
    fun saveProxyUrl(url: String) { prefs.edit().putString(KEY_PROXY_URL, url).apply() }

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "") ?: ""
    fun saveApiKey(key: String) { prefs.edit().putString(KEY_API_KEY, key).apply() }

    fun isUseOurApi(): Boolean = prefs.getBoolean(KEY_USE_OUR_API, true)
    fun setUseOurApi(v: Boolean) { prefs.edit().putBoolean(KEY_USE_OUR_API, v).apply() }

    private const val KEY_MACHINE1_KEY = "machine1_key"
    private const val KEY_MACHINE2_KEY = "machine2_key"

    fun getMachine1Key(): String = prefs.getString(KEY_MACHINE1_KEY, "") ?: ""
    fun saveMachine1Key(k: String) { prefs.edit().putString(KEY_MACHINE1_KEY, k).apply() }
    fun getMachine2Key(): String = prefs.getString(KEY_MACHINE2_KEY, "") ?: ""
    fun saveMachine2Key(k: String) { prefs.edit().putString(KEY_MACHINE2_KEY, k).apply() }

    fun saveFwt(fwt: String) { prefs.edit().putString("fwt", fwt).apply() }
    fun getSavedFwt(): String = prefs.getString("fwt", "") ?: ""
    fun clearFwt() { prefs.edit().remove("fwt").apply() }

    private const val KEY_SERVER_KEY = "server_key"
    fun getServerKey(): String = prefs.getString(KEY_SERVER_KEY, "") ?: ""
    fun saveServerKey(k: String) { prefs.edit().putString(KEY_SERVER_KEY, k).apply() }

    fun getCurrentAccessToken(): String = prefs.getString("accessToken", "") ?: ""
    fun saveAccessToken(t: String) { prefs.edit().putString("accessToken", t).apply() }
    fun clearAccessToken() { prefs.edit().remove("accessToken").apply() }
    fun getCurrentRefreshToken(): String = prefs.getString("refreshToken", "") ?: ""

    private fun getAccounts(): MutableList<AccountInfo> {
        val file = accountsFile
        if (file != null && file.exists()) {
            try {
                val text = file.readText()
                val type = object : TypeToken<MutableList<AccountInfo>>() {}.type
                val list: MutableList<AccountInfo> = gson.fromJson(text, type) ?: mutableListOf()
                if (list.isNotEmpty()) { prefs.edit().putString("account_list", gson.toJson(list)).apply() }
                return list
            } catch (_: Exception) { }
        }
        val json = prefs.getString("account_list", null)
        if (json != null) {
            return try {
                val type = object : TypeToken<MutableList<AccountInfo>>() {}.type
                gson.fromJson(json, type) ?: mutableListOf()
            } catch (_: Exception) { mutableListOf() }
        }
        return mutableListOf()
    }

    private fun saveAccounts(accounts: List<AccountInfo>) {
        val json = gson.toJson(accounts)
        prefs.edit().putString("account_list", json).apply()
        try { accountsFile?.writeText(json) } catch (_: Exception) { }
    }

    fun addAccount(info: AccountInfo) {
        val accounts = getAccounts().toMutableList()
        val idx = accounts.indexOfFirst { it.userId == info.userId }
        if (idx >= 0) accounts[idx] = info else accounts.add(info)
        saveAccounts(accounts)
        setCurrentIdx(accounts.size - 1)
    }

    fun getAccountList(): List<AccountInfo> = getAccounts()
    fun getAccountCount(): Int = getAccounts().size
    fun hasAccounts(): Boolean = getAccountCount() > 0

    private fun getCurrentIdx(): Int = prefs.getInt("current_account", 0)
    private fun setCurrentIdx(idx: Int) { prefs.edit().putInt("current_account", idx).apply() }
    fun getCurrentAccountIndex(): Int = getCurrentIdx()

    fun switchToAccount(index: Int): Boolean {
        val accounts = getAccounts()
        if (index < 0 || index >= accounts.size) return false
        setCurrentIdx(index)
        return true
    }

    fun removeAccount(index: Int): Boolean {
        val accounts = getAccounts().toMutableList()
        if (index < 0 || index >= accounts.size) return false
        accounts.removeAt(index)
        saveAccounts(accounts)
        if (accounts.isEmpty()) { prefs.edit().clear().apply(); return true }
        val ci = getCurrentIdx().coerceIn(0, accounts.size - 1)
        setCurrentIdx(ci)
        return true
    }

    fun saveSelectedRole(id: String, name: String) {
        prefs.edit().putString("roleId", id).putString("roleName", name).apply()
        val idx = getCurrentIdx()
        prefs.edit().putString("roleId_${idx}", id).putString("roleName_${idx}", name).apply()
    }
    fun getSelectedRoleId(): Long? = prefs.getString("roleId", null)?.toLongOrNull()
    fun getSelectedRoleName(): String = prefs.getString("roleName", "") ?: ""

    fun isDisclaimerAccepted(): Boolean = prefs.getBoolean("disclaimerAccepted", false)
    fun setDisclaimerAccepted(accepted: Boolean) { prefs.edit().putBoolean("disclaimerAccepted", accepted).apply() }

    fun isDarkMode(): Int = try { prefs.getInt("darkMode", 0) } catch (_: ClassCastException) {
        val old = prefs.getBoolean("darkMode", false); val m = if (old) 1 else 0
        prefs.edit().remove("darkMode").putInt("darkMode", m).apply(); m
    }
    fun setDarkMode(mode: Int) { prefs.edit().putInt("darkMode", mode).apply() }

    fun clearAll() { prefs.edit().clear().apply(); accountsFile?.delete() }
    fun clearAllExceptDisclaimer() { val a = isDisclaimerAccepted(); prefs.edit().clear().apply(); if (a) setDisclaimerAccepted(true); accountsFile?.delete() }
}