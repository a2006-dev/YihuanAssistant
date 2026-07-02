package com.yh.assistant.data

interface LoginCallback {
    fun onLoginSuccess(phone: String)
    fun onLoginFailed(error: String)
}

interface TokenStatusCallback {
    fun onTokenValid(fwt: String, lastRefresh: Long)
    fun onTokenExpired(fwt: String)
    fun onTokenRefreshed(newAccessToken: String)
}

interface DataRefreshCallback {
fun onDataRefreshed(type: String) 
}