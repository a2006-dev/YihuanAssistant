package com.yh.assistant.ui.main

import android.app.AlertDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yh.assistant.ui.AppColors
import com.yh.assistant.ui.home.HomeScreen
import com.yh.assistant.ui.character.CharacterListScreen
import com.yh.assistant.ui.gacha.GachaScreen
import com.yh.assistant.ui.settings.SettingsScreen
import com.yh.assistant.data.api.RetrofitClient
import com.yh.assistant.util.PreferenceUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val navItems = listOf(
    Triple("首页", Icons.Default.Home, 0),
    Triple("角色", Icons.Default.Person, 1),
    Triple("抽卡", Icons.Default.Star, 2),
    Triple("设置", Icons.Default.Settings, 3)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit, onSettings: () -> Unit) {
    var selected by remember { mutableStateOf(0) }
    var roleId by remember { mutableStateOf(PreferenceUtil.getSelectedRoleId()?.toString() ?: "") }
    var roleName by remember { mutableStateOf(PreferenceUtil.getSelectedRoleName()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var charDetailOpen by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    var showNoticeDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (roleId.isEmpty()) {
            val result = com.yh.assistant.data.repository.RoleRepository.getGameRoles()
            result.onSuccess { data ->
                if (data.roles.isNotEmpty()) {
                    val first = data.roles.first()
                    roleId = first.roleId.toString()
                    roleName = first.roleName
                    PreferenceUtil.saveSelectedRole(roleId, roleName)
                }
            }
            result.getOrNull()?.let { data ->
                if (data.roles.isEmpty()) {
                    AlertDialog.Builder(ctx as android.app.Activity)
                        .setTitle("未绑定游戏角色")
                        .setMessage("该账号下没有找到游戏角色，请确认已在游戏中创建角色")
                        .setPositiveButton("确定") { _, _ -> onLogout() }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        var lastNotice = ""
        while (true) {
            val notice = RetrofitClient.pendingNotice
            if (notice.isNotEmpty() && notice != lastNotice) {
                lastNotice = notice
                showNoticeDialog = notice
                RetrofitClient.pendingNotice = ""
            }
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val url = java.net.URL("http://112.124.68.4:3000/health")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("X-API-Key", "tjd_yh_2024_fixed")
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val code = conn.responseCode
                    if (code == 200) {
                        val body = conn.inputStream.bufferedReader().readText()
                        try {
                            val json = org.json.JSONObject(body)
                            if (json.has("notice") && !json.isNull("notice")) {
                                val n = json.getString("notice")
                                if (n.isNotEmpty()) RetrofitClient.pendingNotice = n
                            }
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
            delay(6000)
        }
    }

    showNoticeDialog?.let { msg ->
        AlertDialog(
            onDismissRequest = { showNoticeDialog = null },
            title = { Text("📢 系统通知", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { showNoticeDialog = null }) { Text("知道了") } }
        )
    }

    BackHandler(enabled = charDetailOpen) { charDetailOpen = false }
    BackHandler(enabled = !charDetailOpen && selected != 0) { selected = 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("海特洛档案室", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (roleId.isNotEmpty()) {
                            Text("UID: ${roleId} · ${roleName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (!isRefreshing) {
                            isRefreshing = true
                            refreshTrigger++
                            Toast.makeText(ctx, "正在刷新...", Toast.LENGTH_SHORT).show()
                            MainScope().launch {
                                delay(800)
                                isRefreshing = false
                                Toast.makeText(ctx, "刷新完成", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = if (isRefreshing) AppColors.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                navItems.forEach { (label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, idx: Int) ->
                    NavigationBarItem(
                        selected = selected == idx,
                        onClick = { selected = idx },
                        icon = {
                            Icon(icon, contentDescription = label,
                                tint = if (selected == idx) AppColors.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        label = {
                            Text(label, fontSize = 11.sp,
                                color = if (selected == idx) AppColors.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AppColors.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selected) {
                0 -> HomeScreen(roleId, refreshTrigger, { id, name -> roleId = id; roleName = name; PreferenceUtil.saveSelectedRole(id, name) })
                1 -> CharacterListScreen(roleId, refreshTrigger, onDetailOpen = { charDetailOpen = it })
                2 -> GachaScreen(roleId, refreshTrigger)
                3 -> SettingsScreen(forRole = true, roleId = roleId, onRoleChanged = { id, name -> roleId = id; roleName = name; PreferenceUtil.saveSelectedRole(id, name) }, onLogout = onLogout)
            }
        }
    }
}