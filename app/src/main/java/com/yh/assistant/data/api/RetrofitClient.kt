package com.yh.assistant.data.api

import com.yh.assistant.data.model.*
import com.yh.assistant.util.PreferenceUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val SERVER_BASE = "http://112.124.68.4:3000"

    var accessToken: String = ""
    var pendingNotice: String = ""

    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "YihuanAssistant/1.0")
                    .apply {
                        header("X-API-Key", "tjd_yh_2024_fixed")
                        if (accessToken.isNotEmpty()) header("Authorization", "Bearer $accessToken")
                    }
                    .build()
                val resp = chain.proceed(req)
                if (resp.isSuccessful) {
                    try {
                        val body = resp.peekBody(8192).string()
                        val json = org.json.JSONObject(body)
                        if (json.has("notice") && !json.isNull("notice")) {
                            val notice = json.getString("notice")
                            if (notice.isNotEmpty()) pendingNotice = notice
                        }
                    } catch (_: Exception) {}
                }
                resp
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(SERVER_BASE).client(client).addConverterFactory(GsonConverterFactory.create()).build()
    }

    val api: AppApi = retrofit.create(AppApi::class.java)
}