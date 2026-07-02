package com.yh.assistant.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    val loginEvents = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    val tokenEvents = MutableSharedFlow<TokenEvent>(extraBufferCapacity = 5)
    val refreshEvents = MutableSharedFlow<RefreshEvent>(extraBufferCapacity = 5)

    data class LoginEvent(val type: LoginType, val phone: String = "", val error: String = "")
    enum class LoginType { SUCCESS, FAILED, LOGOUT }

    data class TokenEvent(
        val type: TokenType,
        val fwt: String = "",
        val accessToken: String = "",
        val lastRefresh: Long = 0
    )
    enum class TokenType { VALID, EXPIRED, REFRESHED, REGISTERED }

data class RefreshEvent(val page: String) 

    suspend fun loginSuccess(phone: String) { loginEvents.emit(LoginEvent(LoginType.SUCCESS, phone)) }
    suspend fun loginFailed(error: String) { loginEvents.emit(LoginEvent(LoginType.FAILED, error = error)) }
    suspend fun logout() { loginEvents.emit(LoginEvent(LoginType.LOGOUT)) }

    suspend fun tokenValid(fwt: String, lastRefresh: Long) { tokenEvents.emit(TokenEvent(TokenType.VALID, fwt, lastRefresh = lastRefresh)) }
    suspend fun tokenExpired(fwt: String) { tokenEvents.emit(TokenEvent(TokenType.EXPIRED, fwt)) }
    suspend fun tokenRefreshed(accessToken: String) { tokenEvents.emit(TokenEvent(TokenType.REFRESHED, accessToken = accessToken)) }

    suspend fun refreshData(page: String) { refreshEvents.emit(RefreshEvent(page)) }
}