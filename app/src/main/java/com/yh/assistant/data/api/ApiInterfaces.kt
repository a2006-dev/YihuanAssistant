package com.yh.assistant.data.api

import com.yh.assistant.data.model.*
import retrofit2.http.*

interface AppApi {
    @POST("/api/login/captcha")
    suspend fun sendCaptcha(@Body body: Map<String, String>): ServerResponse<CaptchaData>

    @POST("/api/login/verify")
    suspend fun login(@Body body: Map<String, String>): ServerResponse<LoginData>

    @GET("/api/game/getGameRoles")
    suspend fun getGameRoles(@Query("gameId") gameId: String = "1289", @Header("Authorization") token: String): ServerResponse<GameRolesData>

    @GET("/api/game/roleHome")
    suspend fun getRoleHome(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<RoleHome>

    @GET("/api/game/characters")
    suspend fun getCharacters(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<List<CharacterDetail>>

    @GET("/api/game/achieveProgress")
    suspend fun getAchieveProgress(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<AchieveProgress>

    @GET("/api/game/areaProgress")
    suspend fun getAreaProgress(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<List<AreaProgress>>

    @GET("/api/game/realestate")
    suspend fun getRealEstate(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<RealEstateData>

    @GET("/api/game/vehicles")
    suspend fun getVehicles(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<VehiclesData>

    @GET("/api/game/gacha")
    suspend fun getGacha(@Query("roleId") roleId: String, @Header("Authorization") token: String): ServerResponse<GachaData>

    @GET("/api/game/signin/state")
    suspend fun getSignState(@Query("gameId") gameId: String = "1289", @Header("Authorization") token: String): ServerResponse<SignStateData>

    @POST("/api/game/sign")
    suspend fun doSign(@Body body: Map<String, String>, @Header("Authorization") token: String): ServerResponse<Any>

    @GET("/api/game/sign/rewards")
    suspend fun getSignRewards(@Query("gameId") gameId: String = "1289", @Header("Authorization") token: String): ServerResponse<List<SignRewardItem>>

    @POST("/api/keepalive/register")
    suspend fun registerKeepAlive(@Body body: Map<String, String>): ServerResponse<KeepAliveData>

    @GET("/admin/status")
    suspend fun serverStatus(@Header("X-Master-Key") key: String): ServerResponse<ServerStatusData>

    @GET("/api/system/info")
    suspend fun systemInfo(): ServerResponse<SystemInfoData>
}