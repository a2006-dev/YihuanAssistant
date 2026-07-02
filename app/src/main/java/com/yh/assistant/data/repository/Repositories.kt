package com.yh.assistant.data.repository

import com.yh.assistant.data.api.RetrofitClient
import com.yh.assistant.data.model.*
import com.yh.assistant.util.PreferenceUtil

object AuthRepository {
    private val api get() = RetrofitClient.api

    data class KeepAliveStatus(val registered: Boolean = false, val lastRefresh: Long = 0)
    var keepAliveStatus = KeepAliveStatus()

    suspend fun sendSms(phone: String): Result<String> = runCatching {
        val r = api.sendCaptcha(mapOf("phone" to phone))
        if (r.code == 0) "验证码已发送" else throw Exception(r.msg)
    }

    suspend fun login(phone: String, code: String): Result<LoginData> = runCatching {
        val r = api.login(mapOf("phone" to phone, "captcha" to code))
        if (r.code != 0 || r.data == null) throw Exception(r.msg)
        val data = r.data!!
        RetrofitClient.accessToken = data.accessToken
        PreferenceUtil.saveAccessToken(data.accessToken)
        var roleId = ""
        var roleName = ""
        val rolesResp = RoleRepository.getGameRoles()
        val first = rolesResp.getOrNull()?.roles?.firstOrNull()
        if (first != null) {
            roleId = first.roleId.toString()
            roleName = first.roleName
            PreferenceUtil.saveSelectedRole(roleId, roleName)
        }
        if (data.refreshToken.isNotEmpty()) {
            try {
                api.registerKeepAlive(mapOf("refreshToken" to data.refreshToken, "uid" to data.uid, "gameRoleId" to roleId))
                keepAliveStatus = KeepAliveStatus(true, System.currentTimeMillis())
            } catch (_: Exception) {}
        }
        val uid = if (roleId.isNotEmpty()) roleId.toLongOrNull() ?: 0L else 0L
        if (uid > 0L) {
            PreferenceUtil.addAccount(PreferenceUtil.AccountInfo(
                userId = uid, token = data.accessToken, accessToken = data.accessToken,
                gameRoleId = roleId, gameRoleName = roleName
            ))
        }
        data
    }

    fun restoreSession() {
        val token = PreferenceUtil.getCurrentAccessToken()
        if (token.isNotEmpty()) RetrofitClient.accessToken = token
    }

    fun logout() { RetrofitClient.accessToken = ""; PreferenceUtil.clearAccessToken() }
}

object RoleRepository {
    private val api get() = RetrofitClient.api
    private val token get() = "Bearer ${RetrofitClient.accessToken}"

    suspend fun getGameRoles(): Result<GameRolesData> = runCatching {
        val r = api.getGameRoles(token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getRoleHome(roleId: String): Result<RoleHome> = runCatching {
        val r = api.getRoleHome(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getCharacters(roleId: String): Result<List<CharacterDetail>> = runCatching {
        val r = api.getCharacters(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getAchieveProgress(roleId: String): Result<AchieveProgress> = runCatching {
        val r = api.getAchieveProgress(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getAreaProgress(roleId: String): Result<List<AreaProgress>> = runCatching {
        val r = api.getAreaProgress(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getRealEstate(roleId: String): Result<RealEstateData> = runCatching {
        val r = api.getRealEstate(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getVehicles(roleId: String): Result<VehiclesData> = runCatching {
        val r = api.getVehicles(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getGacha(roleId: String): Result<GachaData> = runCatching {
        val r = api.getGacha(roleId, token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun getSignState(roleId: String): Result<SignStateData> = runCatching {
        val r = api.getSignState(token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
    suspend fun doSign(roleId: String): Result<Unit> = runCatching {
        val r = api.doSign(mapOf("roleId" to roleId, "gameId" to "1289"), token = token)
        if (r.code != 0) throw Exception(r.msg)
    }
    suspend fun getSignRewards(roleId: String): Result<List<SignRewardItem>> = runCatching {
        val r = api.getSignRewards(token = token)
        if (r.code == 0 && r.data != null) r.data!! else throw Exception(r.msg)
    }
}