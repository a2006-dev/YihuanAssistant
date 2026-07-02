package com.yh.assistant.data.model

data class LaohuResponse(val code: Int = -1, val message: String = "", val result: Map<String, Any>? = null)
data class LoginResponse(val code: Int = -1, val message: String = "", val result: LoginResult? = null)
data class LoginResult(val userId: Long = 0, val token: String = "", val cellphone: String = "")
data class TajiduoResponse<T>(val code: Int = -1, val msg: String = "", val ok: Boolean = false, val data: T? = null)
data class TajiduoSession(val accessToken: String = "", val refreshToken: String = "", val uid: Long = 0, val firstLogin: Boolean = false)
data class ProxyResponse(val code: Int = -1, val msg: String = "", val data: Map<String, Any>? = null)
data class ProxySessionResponse(val code: Int = -1, val msg: String = "", val data: ProxySessionData? = null)
data class ProxySessionData(val fwt: String = "", val uid: String = "", val accessToken: String = "", val refreshToken: String = "", val firstLogin: Boolean = false)

data class ServerResponse<T>(val code: Int = -1, val msg: String = "", val data: T? = null, val notice: String = "")
data class CaptchaData(val deviceId: String = "")
data class LoginData(val accessToken: String = "", val refreshToken: String = "", val uid: String = "")
data class KeepAliveData(val fwt: String = "", val uid: String = "", val updated: Boolean = false)
data class ServerStatusData(
    val machineName: String = "", val uptime: Double = 0.0,
    val userCount: Int = 0, val keyCount: Int = 0, val activeKeys: Int = 0,
    val totalRequests: Int = 0, val todayRequests: Int = 0,
    val refreshSuccess: Int = 0, val refreshFail: Int = 0,
    val memoryUsage: String = ""
)
data class SystemInfoData(
    val cpuModel: String = "", val cpuCores: Int = 0, val cpuPercent: Double = 0.0,
    val memTotal: String = "", val memUsed: String = "", val memPercent: Double = 0.0,
    val diskUsed: String = "", val diskTotal: String = "", val diskPercent: Int = 0,
    val dockerContainers: String = "", val dockerVersion: String = "",
    val uptime: Long = 0, val hostname: String = ""
)

data class GameRolesData(val bindRole: Long = 0, val roles: List<GameRole> = emptyList())
data class GameRole(val gameId: Int = 0, val gender: Int = 0, val lev: Int = 0, val roleId: Long = 0, val roleName: String = "", val serverId: Int = 0, val serverName: String = "")

data class RoleHome(
    val avatar: String = "",
    val characters: List<RoleCharacter> = emptyList(),
    val achieveProgress: AchieveProgress? = null,
    val areaProgress: List<AreaProgress>? = null,
    val staminaValue: Int = 0,
    val staminaMaxValue: Int = 0,
    val citystaminaValue: Int = 0,
    val citystaminaMaxValue: Int = 0,
    val dayvalue: Int = 0,
    val weekcopiesremainCnt: Int = 0,
    val rolename: String = "",
    val lev: Int = 0,
    val realestate: HomeEstateSummary? = null,
    val vehicle: HomeVehicleSummary? = null
)
data class HomeEstateSummary(val ownCnt: Int = 0, val total: Int = 0)
data class HomeVehicleSummary(val ownCnt: Int = 0, val total: Int = 0)
data class RoleCharacter(val id: String = "", val name: String = "", val alev: Int = 0, val awakenLev: Int = 0, val awakenEffect: List<String> = emptyList(), val slev: Int = 0, val elementType: String = "", val groupType: String = "", val quality: String = "", val likeabilitylev: Int = 0, val properties: List<Property> = emptyList())
data class Property(val id: String = "", val name: String = "", val value: String = "")

data class CharacterDetail(val id: String = "", val name: String = "", val alev: Int = 0, val awakenLev: Int = 0, val awakenEffect: List<String> = emptyList(), val slev: Int = 0, val elementType: String = "", val groupType: String = "", val quality: String = "", val likeabilitylev: Int = 0, val properties: List<Property> = emptyList(), val fork: Fork? = null, val suit: Suit? = null, val skills: List<SkillGroup> = emptyList(), val citySkills: List<CitySkill> = emptyList())

data class Fork(val alev: String = "", val blev: String = "", val id: String = "", val name: String = "", val des: String = "", val quality: String = "", val properties: List<Property> = emptyList(), val buffName: String = "", val buffDes: String = "", val lbd: List<String> = emptyList(), val groupType: String = "", val slev: String = "")

data class Suit(val id: String = "", val name: String = "", val des2: String = "", val des4: String = "", val suitActivateNum: Int = 0, val suitCondition: List<String> = emptyList(), val core: List<SuitCore> = emptyList(), val pie: List<SuitPiece> = emptyList())
data class SuitCore(val id: String = "", val name: String = "", val lev: Int = 0, val mainProperties: List<Property> = emptyList(), val properties: List<Property> = emptyList())
data class SuitPiece(val id: String = "", val name: String = "", val lev: Int = 0, val mainProperties: List<Property> = emptyList(), val properties: List<Property> = emptyList())

data class SkillGroup(val id: String = "", val name: String = "", val level: Int = 0, val type: String = "", val items: List<SkillItem> = emptyList())
data class SkillItem(val title: String = "", val desc: String = "")
data class CitySkill(val id: String = "", val name: String = "", val level: Int = 0, val type: String = "", val items: List<SkillItem> = emptyList())

data class AchieveProgress(val achievementCnt: Int = 0, val total: Int = 0)
data class AreaProgress(val id: String = "", val name: String = "", val progress: Int = 0, val total: Int = 0, val detail: List<AreaDetail> = emptyList())
data class AreaDetail(val id: String = "", val name: String = "", val progress: Int = 0, val total: Int = 0)

data class RealEstateData(val detail: List<Estate> = emptyList())
data class Estate(val id: String = "", val name: String = "", val own: Boolean = false, val chars: String = "", val fdetail: List<Furniture> = emptyList())
data class Furniture(val id: String = "", val name: String = "", val own: Boolean = false)

data class VehiclesData(val detail: List<Vehicle> = emptyList())
data class Vehicle(val id: String = "", val name: String = "", val own: Boolean = false, val base: List<Property> = emptyList(), val advanced: List<VehicleAdvanced> = emptyList(), val models: List<VehicleModel> = emptyList())
data class VehicleAdvanced(val name: String = "", val value: String = "", val max: String = "")
data class VehicleModel(val id: String = "", val type: String = "")

data class GachaData(val avatar: String = "", val rolename: String = "", val lev: Int = 0, val luckTitle: String = "", val luckType: Int = 0, val gachaDetails: List<GachaPool> = emptyList())
data class GachaPool(val tab: String = "", val average: String = "", val drawCount: Int = 0, val m: Int = 0, val playerOver: String = "", val rareCount: Int = 0, val details: List<GachaDetail> = emptyList())
data class GachaDetail(val charid: String = "", val luckyType: Int = 0, val rareCount: Int = 0, val time: String = "", val timeStamp: Long = 0)

data class SignStateData(val todaySign: Boolean = false, val day: Int = 0, val days: Int = 0, val month: Int = 0, val reSignCnt: Int = 0)
data class SignRewardItem(val name: String = "", val num: Int = 0, val icon: String = "")