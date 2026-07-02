package com.yh.assistant.ui.home

import androidx.compose.foundation.layout.*
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yh.assistant.data.api.RetrofitClient
import com.yh.assistant.data.model.*
import com.yh.assistant.data.repository.RoleRepository
import com.yh.assistant.ui.AppColors
import com.yh.assistant.ui.AppShapes
import com.yh.assistant.util.AssetUrl
import com.yh.assistant.util.CacheManager
import com.yh.assistant.util.PreferenceUtil
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeViewModel : ViewModel() {
    private val _home = MutableStateFlow<RoleHome?>(null); val home: StateFlow<RoleHome?> = _home
    private val _estates = MutableStateFlow<List<Estate>>(emptyList()); val estates: StateFlow<List<Estate>> = _estates
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList()); val vehicles: StateFlow<List<Vehicle>> = _vehicles
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading
    fun load(roleId: String, force: Boolean = false) {
        val rid = if (roleId.isEmpty()) PreferenceUtil.getSelectedRoleId()?.toString() ?: "" else roleId
        if (rid.isEmpty()) return
        _loading.value = true
        viewModelScope.launch {
            RoleRepository.getRoleHome(rid).onSuccess { _home.value = it; CacheManager.cacheHome(rid, it) }
            RoleRepository.getRealEstate(rid).onSuccess { _estates.value = it.detail; CacheManager.cacheEstates(rid, it.detail) }
            RoleRepository.getVehicles(rid).onSuccess { _vehicles.value = it.detail; CacheManager.cacheVehicles(rid, it.detail) }
            _loading.value = false
        }
    }
}

private fun staminaRecoveryTime(value: Int, max: Int): String {
    if (value >= max || max <= 0) return "已满"
    val need = max - value; val minutes = need * 6; val h = minutes / 60; val m = minutes % 60
    return if (h > 0) "${h}小时${m}分" else "${m}分"
}

private fun weeklyResetTime(): String {
    val now = System.currentTimeMillis()
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 5); cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0)
    if (cal.timeInMillis <= now) cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
    val diff = cal.timeInMillis - now; val days = diff / 86400000; val hours = (diff % 86400000) / 3600000; val mins = (diff % 3600000) / 60000
    return if (days > 0) "${days}天${hours}小时" else "${hours}小时${mins}分"
}

@Composable
fun HomeScreen(roleId: String, refreshTrigger: Int = 0, onRoleChanged: (String, String) -> Unit = { _, _ -> }) {
    val vm = viewModel<HomeViewModel>()
    var isFirstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { if (isFirstLoad) { vm.load(roleId, force = true); isFirstLoad = false } }
    LaunchedEffect(refreshTrigger) { if (refreshTrigger > 0) vm.load(roleId, force = true) }
    val home = vm.home.collectAsState().value
    val estates = vm.estates.collectAsState().value
    val vehicles = vm.vehicles.collectAsState().value
    val loading = vm.loading.collectAsState().value
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val signPrefs = ctx.getSharedPreferences("sign_records", android.content.Context.MODE_PRIVATE)
    val cal = java.util.Calendar.getInstance()
    val todayStr = "${cal.get(java.util.Calendar.YEAR)}${cal.get(java.util.Calendar.MONTH)+1}${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
    val todayKey = "sign_${roleId}_${todayStr}"
    var isSigned by remember { mutableStateOf(signPrefs.getBoolean(todayKey, false)) }
    var signLoading by remember { mutableStateOf(false) }
    var signReward by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var notices by remember { mutableStateOf<List<NoticeItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                val url = URL("http://112.124.68.4:3000/api/notices")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000; conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    val arr = json.optJSONArray("data")
                    if (arr != null) {
                        val list = mutableListOf<NoticeItem>()
                        for (i in 0 until arr.length()) {
                            val item = arr.getJSONObject(i)
                            list.add(NoticeItem(
                                title = item.optString("title", ""),
                                content = item.optString("content", ""),
                                level = item.optString("level", "info")
                            ))
                        }
                        notices = list
                    }
                }
            }
        } catch (_: Exception) {}
    }

    if (loading && home == null) {
        ShimmerHomeScreen()
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        if (notices.isNotEmpty()) {
            item {
                notices.forEach { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        shape = AppShapes.medium,
                        colors = CardDefaults.cardColors(
                            if (notice.level == "warning") AppColors.secondary.copy(alpha = 0.1f)
                            else AppColors.primary.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text(
                                if (notice.level == "warning") "⚠️" else "📌",
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(notice.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(notice.content, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        AssetUrl.characterAvatar(home?.characters?.firstOrNull()?.id ?: ""),
                        null, Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(home?.rolename ?: "海特洛档案室", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Lv.${home?.lev ?: 0} · UID: ${roleId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("在线", fontSize = 11.sp, color = AppColors.green)
                        Text(weeklyResetTime(), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, null, Modifier.size(18.dp), tint = AppColors.cyan)
                        Spacer(Modifier.width(6.dp))
                        Text("资源状态", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        val s = home?.staminaValue ?: 0; val sm = home?.staminaMaxValue ?: 0
                        val c = home?.citystaminaValue ?: 0; val cm = home?.citystaminaMaxValue ?: 0
                        CompactStatItem("本性像素", s, sm, AppColors.cyan, staminaRecoveryTime(s, sm))
                        CompactStatItem("都市活力", c, cm, AppColors.primary, weeklyResetTime())
                        CompactStatItem("活跃度", home?.dayvalue ?: 0, 100, AppColors.secondary, "")
                        CompactStatItem("周本", home?.weekcopiesremainCnt ?: 0, 3, AppColors.accent, "")
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventNote, null, Modifier.size(18.dp), tint = AppColors.secondary)
                            Spacer(Modifier.width(6.dp))
                            Text("塔吉多签到", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (signReward.isNotEmpty()) {
                            Text("奖励: $signReward", fontSize = 11.sp, color = AppColors.green, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    Button(
                        onClick = {
                            if (!signLoading && roleId.isNotEmpty() && !isSigned) {
                                signLoading = true
                                scope.launch {
                                    val state = RoleRepository.getSignState(roleId)
                                    if (state.getOrNull()?.todaySign == true) {
                                        isSigned = true
                                        signReward = "已发送至邮箱"
                                        signPrefs.edit().putBoolean(todayKey, true).apply()
                                    } else {
                                        RoleRepository.doSign(roleId).onSuccess {
                                            isSigned = true
                                            signReward = "已发送至邮箱"
                                            signPrefs.edit().putBoolean(todayKey, true).apply()
                                        }.onFailure {
                                            if (RoleRepository.getSignState(roleId).getOrNull()?.todaySign == true) {
                                                isSigned = true
                                                signReward = "已发送至邮箱"
                                                signPrefs.edit().putBoolean(todayKey, true).apply()
                                            }
                                        }
                                    }
                                    signLoading = false
                                }
                            }
                        },
                        shape = AppShapes.medium,
                        enabled = !signLoading && !isSigned,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSigned) AppColors.green.copy(alpha = 0.15f) else AppColors.primary.copy(alpha = 0.2f)
                        )
                    ) {
                        if (signLoading) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.primary)
                        } else {
                            Text(
                                if (isSigned) "✅ 已签到" else "立即签到",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (isSigned) AppColors.green else AppColors.primaryLight
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        home?.characters?.let { chars ->
            if (chars.isNotEmpty()) {
                item {
                    SectionHeader("编队角色", Icons.Default.Groups)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(chars.take(8)) { ch -> CharacterChip(ch) }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val ach = home?.achieveProgress; val es = home?.realestate; val vs = home?.vehicle
                StatCard("成就", "${ach?.achievementCnt ?: 0}/${ach?.total ?: 0}", AppColors.primary, Icons.Default.EmojiEvents, Modifier.weight(1f))
                StatCard("房产", "${es?.ownCnt ?: 0}", AppColors.cyan, Icons.Default.House, Modifier.weight(1f))
                StatCard("载具", "${vs?.ownCnt ?: 0}/${vs?.total ?: 0}", AppColors.secondary, Icons.Default.DirectionsCar, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }

        home?.achieveProgress?.let { ach ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.large,
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, Modifier.size(20.dp), tint = AppColors.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("成就进度", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        val pct = if (ach.total > 0) ach.achievementCnt.toFloat() / ach.total else 0f
                        LinearProgressIndicator(
                            progress = pct,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(AppShapes.small),
                            color = AppColors.secondary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("${ach.achievementCnt}/${ach.total} (${(pct * 100).toInt()}%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        home?.areaProgress?.let { areas ->
            item {
                SectionHeader("探索进度", Icons.Default.Explore)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(areas) { area -> AreaCard(area) }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        if (estates.isNotEmpty()) {
            item {
                SectionHeader("房产 (${estates.count { it.own }}/${estates.size})", Icons.Default.House)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(estates.take(5)) { e -> EstateCard(e) }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        if (vehicles.isNotEmpty()) {
            item {
                SectionHeader("载具 (${vehicles.count { it.own }}/${vehicles.size})", Icons.Default.DirectionsCar)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    vehicles.take(5).forEach { v -> VehicleRow(v) }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun CompactStatItem(label: String, value: Int, max: Int, color: Color, subText: String) {
    val pct = if (max > 0) value.toFloat() / max else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Text("$value/$max", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = pct,
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        if (subText.isNotEmpty()) {
            Text("${subText}后满", fontSize = 7.sp, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = AppColors.primary)
        Spacer(Modifier.width(6.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun CharacterChip(ch: RoleCharacter) {
    Card(
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(80.dp)
    ) {
        Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(AssetUrl.characterAvatar(ch.id), ch.name, Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(4.dp))
            Text(ch.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Lv.${ch.alev}", fontSize = 9.sp, color = AppColors.secondary)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = AppShapes.large, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(24.dp), tint = color)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AreaCard(area: AreaProgress) {
    Card(shape = AppShapes.medium, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.width(140.dp)) {
        Column {
            AsyncImage(AssetUrl.areaBanner(area.id), null, Modifier.fillMaxWidth().height(80.dp), contentScale = ContentScale.Crop)
            Column(Modifier.padding(8.dp)) {
                Text(area.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val pct = if (area.total > 0) area.progress.toFloat() / area.total else 0f
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(progress = pct, modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = AppColors.primary, trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Text("${area.progress}/${area.total}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EstateCard(estate: Estate) {
    Card(shape = AppShapes.medium, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.width(160.dp)) {
        Column {
            AsyncImage(AssetUrl.realEstate(estate.id), null, Modifier.fillMaxWidth().height(90.dp), contentScale = ContentScale.Crop)
            Column(Modifier.padding(8.dp)) {
                Text(estate.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (estate.own) "✓ 已拥有" else "未拥有", fontSize = 10.sp, color = if (estate.own) AppColors.green else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VehicleRow(vehicle: Vehicle) {
    Card(shape = AppShapes.medium, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsCar, null, Modifier.size(32.dp), tint = if (vehicle.own) AppColors.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(vehicle.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(if (vehicle.own) "已拥有" else "未拥有", fontSize = 10.sp, color = if (vehicle.own) AppColors.green else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (vehicle.own) Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = AppColors.green)
        }
    }
}

private data class NoticeItem(val title: String, val content: String, val level: String)

@Composable
private fun ShimmerHomeScreen() {
    val shimmerColor = MaterialTheme.colorScheme.surface
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Card(Modifier.fillMaxWidth().height(80.dp), shape = AppShapes.large, colors = CardDefaults.cardColors(shimmerColor)) {}
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth().height(80.dp), shape = AppShapes.large, colors = CardDefaults.cardColors(shimmerColor)) {}
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth().height(60.dp), shape = AppShapes.large, colors = CardDefaults.cardColors(shimmerColor)) {}
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { Card(Modifier.weight(1f).height(80.dp), shape = AppShapes.large, colors = CardDefaults.cardColors(shimmerColor)) {} }
            }
        }
    }
}