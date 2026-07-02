package com.yh.assistant.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yh.assistant.data.model.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CacheManager {
    private const val DIR = "cache"
    private lateinit var cacheDir: File
    private val gson = Gson()

    data class CacheEntry<T>(val data: T, val timestamp: Long, val ttlMinutes: Long = 30) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttlMinutes * 60 * 1000
        fun age(): String {
            val diff = System.currentTimeMillis() - timestamp
            val min = diff / 60000
            return when {
                min < 1 -> "刚刚"
                min < 60 -> "${min}分钟前"
                else -> "${min / 60}小时前"
            }
        }
    }

    fun init(ctx: Context) {
        cacheDir = File(ctx.cacheDir, DIR)
        cacheDir.mkdirs()
    }

    fun <T> get(key: String, type: java.lang.reflect.Type): CacheEntry<T>? {
        val file = File(cacheDir, "${key}.json")
        if (!file.exists()) return null
        return try {
            val text = file.readText()
            val entry: CacheEntry<T> = gson.fromJson(text, type)
            entry
        } catch (e: Exception) { null }
    }

    fun <T> put(key: String, data: T, ttlMinutes: Long = 30) {
        val file = File(cacheDir, "${key}.json")
        try {
            val entry = CacheEntry(data, System.currentTimeMillis(), ttlMinutes)
            file.writeText(gson.toJson(entry))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun remove(key: String) {
        File(cacheDir, "${key}.json").delete()
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }


    fun cacheGacha(roleId: String, data: GachaData) = put("gacha_$roleId", data, 30)
    fun getGacha(roleId: String): CacheEntry<GachaData>? {
        val type = object : TypeToken<CacheEntry<GachaData>>() {}.type
        return get("gacha_$roleId", type)
    }

    fun cacheHome(roleId: String, data: RoleHome) = put("home_$roleId", data, 5)
    fun getHome(roleId: String): CacheEntry<RoleHome>? {
        val type = object : TypeToken<CacheEntry<RoleHome>>() {}.type
        return get("home_$roleId", type)
    }

    fun cacheCharacters(roleId: String, data: List<CharacterDetail>) = put("chars_$roleId", data, 10)
    fun getCharacters(roleId: String): CacheEntry<List<CharacterDetail>>? {
        val type = object : TypeToken<CacheEntry<List<CharacterDetail>>>() {}.type
        return get("chars_$roleId", type)
    }

    fun cacheEstates(roleId: String, data: List<Estate>) = put("estates_$roleId", data, 30)
    fun getEstates(roleId: String): CacheEntry<List<Estate>>? {
        val type = object : TypeToken<CacheEntry<List<Estate>>>() {}.type
        return get("estates_$roleId", type)
    }

    fun cacheVehicles(roleId: String, data: List<Vehicle>) = put("vehicles_$roleId", data, 30)
    fun getVehicles(roleId: String): CacheEntry<List<Vehicle>>? {
        val type = object : TypeToken<CacheEntry<List<Vehicle>>>() {}.type
        return get("vehicles_$roleId", type)
    }
}