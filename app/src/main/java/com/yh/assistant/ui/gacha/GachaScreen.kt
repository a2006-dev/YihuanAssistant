package com.yh.assistant.ui.gacha

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.yh.assistant.data.model.*
import com.yh.assistant.data.repository.RoleRepository
import com.yh.assistant.ui.*
import com.yh.assistant.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val nameCache = mutableMapOf<String, String>()

private fun resolveName(id: String): String {
    val cached = nameCache[id]
    if (cached != null && cached.isNotEmpty()) return cached
    return if (id.length > 20) id.take(16) + "..." else id
}

private suspend fun loadNameMap(roleId: String) {
    val oldCache = HashMap(nameCache)
    try {
        nameCache.clear()
        var hasData = false
        try {
            val url = URL("http://112.124.68.4:3000/api/name-map")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000; conn.readTimeout = 8000
            if (conn.responseCode == 200) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                val map = json.optJSONObject("data")?.optJSONObject("map")
                if (map != null) {
                    val keys = map.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = map.optString(k, "")
                        if (v.isNotEmpty()) { nameCache[k] = v; hasData = true }
                    }
                }
            }
        } catch (_: Exception) {}

        RoleRepository.getCharacters(roleId).onSuccess { list ->
            list.forEach { c ->
                if (c.name.isNotEmpty()) { nameCache[c.id] = c.name; hasData = true }
                c.fork?.let { f -> if (f.name.isNotEmpty()) { nameCache[f.id] = f.name; hasData = true } }
            }
        }

        if (!hasData && oldCache.isNotEmpty()) {
            nameCache.putAll(oldCache)
        }
    } catch (_: Exception) {
        nameCache.clear()
        nameCache.putAll(oldCache)
    }
}

class GachaViewModel : ViewModel() {
    private val _g = MutableStateFlow<GachaData?>(null); val g: StateFlow<GachaData?> = _g
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow(false); val error: StateFlow<Boolean> = _error
    private var loadedRid = ""
    fun load(rid: String, force: Boolean = false) {
        if (rid.isEmpty() || (rid == loadedRid && !force)) return
        loadedRid = rid
        CacheManager.getGacha(rid)?.let { _g.value = it.data }
        _loading.value = true; _error.value = false
        viewModelScope.launch {
            RoleRepository.getGacha(rid).onSuccess { _g.value = it; CacheManager.cacheGacha(rid, it); _error.value = false }.onFailure { _error.value = true }
            loadNameMap(rid)
            _loading.value = false
        }
    }
}

private fun isForkId(id: String) = id.startsWith("fork_")
private fun luckTag(pulls: Int): Pair<String, Color> = when {
    pulls <= 10 -> Pair("欧皇", Color(0xFFF59E0B))
    pulls <= 40 -> Pair("小欧", Color(0xFF06B6D4))
    pulls in 50..69 -> Pair("", Color.Transparent)
    pulls >= 70 -> Pair("非", Color(0xFFEF4444))
    else -> Pair("", Color.Transparent)
}

@Composable
fun GachaScreen(roleId: String, refreshTrigger: Int = 0) {
    val ctx = LocalContext.current
    val vm = viewModel<GachaViewModel>()
    LaunchedEffect(Unit) { vm.load(roleId) }
    LaunchedEffect(refreshTrigger) { if (refreshTrigger > 0) vm.load(roleId) }
    val data = vm.g.collectAsState().value
    val loading = vm.loading.collectAsState().value
    val hasError = vm.error.value
    var selectedPool by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        if (hasError && data == null) {
            ErrorState("加载抽卡数据失败", onRetry = { vm.load(roleId, force = true) })
        } else if (data == null) {
            if (!loading) EmptyState(title = "暂无抽卡数据", subtitle = "请确认游戏内已有抽卡记录")
        } else {
            GachaContent(data, selectedPool, { selectedPool = it }, ctx)
        }
    }
}

@Composable
private fun GachaContent(d: GachaData, selectedPool: Int, onPoolChange: (Int) -> Unit, ctx: android.content.Context) {
    val totalDraws = d.gachaDetails.sumOf { it.drawCount }
    val totalGold = d.gachaDetails.sumOf { it.rareCount }
    val avgPity = if (totalGold > 0) totalDraws / totalGold else 0

    Card(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            StatItem("总抽数", "$totalDraws", AppColors.secondary)
            StatItem("S 数", "$totalGold", AppColors.primary)
            StatItem("平均", "$avgPity", AppColors.cyan)
            IconButton(onClick = {
                ShareRenderUtil.shareGacha(ctx, d, PreferenceUtil.getSelectedRoleId()?.toString() ?: "", PreferenceUtil.getSelectedRoleName())
            }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Share, "分享", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    ScrollableTabRow(selectedTabIndex = selectedPool, containerColor = Color.Transparent, edgePadding = 0.dp) {
        d.gachaDetails.forEachIndexed { i, pool ->
            Tab(selected = selectedPool == i, onClick = { onPoolChange(i) }, text = {
                Text("${pool.tab}", fontSize = 13.sp, fontWeight = if (selectedPool == i) FontWeight.Bold else FontWeight.Normal)
            })
        }
    }

    val pool = d.gachaDetails.getOrNull(selectedPool) ?: return
    val maxPity = if (pool.m > 0) pool.m else 90

    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), shape = AppShapes.large, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pool.tab, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("${pool.rareCount}S", fontSize = 13.sp, color = AppColors.primary, modifier = Modifier.background(AppColors.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Text("${pool.drawCount}抽", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    val pct = pool.playerOver.replace("%","").toFloatOrNull() ?: 50f
                    LinearProgressIndicator(
                        progress = pct / 100f,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = when { pct >= 70 -> AppColors.secondary; pct >= 40 -> AppColors.green; else -> AppColors.red },
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text("已超过 ${pool.playerOver} 玩家 · 平均 ${pool.average}抽/S", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            DividerWithText("${pool.details.size} 条记录")
        }

        items(pool.details) { item ->
            val pulls = item.rareCount
            val progressPct = if (maxPity > 0) (pulls.toFloat() / maxPity * 100).toInt().coerceIn(0, 100) else 0
            val (tag, tagColor) = luckTag(pulls)
            val isFork = isForkId(item.charid)
            val imgUrl = if (isFork) AssetUrl.fork(item.charid) else AssetUrl.characterAvatar(item.charid)

            Card(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = AppShapes.medium,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(formatGachaDate(item.timeStamp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp))
                    Spacer(Modifier.width(8.dp))
                    AsyncImage(imgUrl, null, Modifier.size(42.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(resolveName(item.charid), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${pulls}抽", fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (tag.isNotEmpty()) tagColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            if (tag.isNotEmpty()) {
                                Spacer(Modifier.width(4.dp))
                                Text(tag, fontSize = 9.sp, color = tagColor, modifier = Modifier.background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Column(Modifier.width(56.dp), horizontalAlignment = Alignment.End) {
                        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(progressPct / 100f).clip(RoundedCornerShape(2.dp)).background(
                                if (pulls <= 40) Brush.horizontalGradient(listOf(AppColors.cyan, AppColors.accent))
                                else Brush.horizontalGradient(listOf(AppColors.accent, AppColors.red))
                            ))
                        }
                        Text("${progressPct}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatGachaDate(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val t = if (timestamp < 1e12) timestamp * 1000 else timestamp
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = t }
    return "${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
}