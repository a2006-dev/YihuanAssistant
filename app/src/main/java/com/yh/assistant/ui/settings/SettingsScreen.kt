package com.yh.assistant.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yh.assistant.MainActivity
import com.yh.assistant.data.model.GameRole
import com.yh.assistant.data.repository.RoleRepository
import com.yh.assistant.ui.*
import com.yh.assistant.util.PreferenceUtil
import com.yh.assistant.util.PreferenceUtil.AccountInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SettingsViewModel : ViewModel() {
    private val _r = MutableStateFlow<List<GameRole>>(emptyList()); val r: StateFlow<List<GameRole>> = _r
    private var loaded = false
    fun load() { if (loaded) return; loaded = true; viewModelScope.launch { RoleRepository.getGameRoles().onSuccess { _r.value = it.roles } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(forRole: Boolean = false, roleId: String = "", onRoleChanged: ((String, String) -> Unit)? = null, onLogout: (() -> Unit)? = null, onBack: (() -> Unit)? = null) {
    var showProxyConfig by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    if (showProxyConfig) { ProxyConfigScreen(onBack = { showProxyConfig = false }); return }
    if (showDisclaimer) { com.yh.assistant.ui.disclaimer.DisclaimerScreen(onAccepted = { showDisclaimer = false }, showConfirm = false); return }
    if (showFeedback) { FeedbackScreen(onBack = { showFeedback = false }); return }

    val ctx = LocalContext.current
    var darkMode by remember { mutableStateOf(PreferenceUtil.isDarkMode()) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }
    val accounts = remember { mutableStateListOf<AccountInfo>() }
    LaunchedEffect(Unit) { accounts.addAll(PreferenceUtil.getAccountList()) }
    val currentIdx = PreferenceUtil.getCurrentAccountIndex()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Text("设置", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

        SectionLabel("显示与外观")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("显示模式", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val modes = listOf("跟随系统" to 0, "深色" to 1, "浅色" to 2)
                    modes.forEach { (label, mode) ->
                        FilterChip(
                            selected = darkMode == mode,
                            onClick = {
                                darkMode = mode
                                PreferenceUtil.setDarkMode(mode)
                                ctx.startActivity(Intent(ctx, ctx::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.primary.copy(alpha = 0.15f),
                                selectedLabelColor = AppColors.primary
                            )
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionLabel("服务器")
        SettingsCard(
            icon = Icons.Default.Dns,
            title = "Token 保活",
            subtitle = "配置保活状态和 API Key",
            onClick = { showProxyConfig = true }
        )
        Spacer(Modifier.height(12.dp))

        SectionLabel("账号管理 (${accounts.size})")
        if (accounts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Text("暂无已保存的账号", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp).fillMaxWidth(), textAlign = TextAlign.Center)
            }
        } else {
            accounts.forEachIndexed { i, acc ->
                val isCurrent = i == currentIdx
                Card(
                    onClick = {
                        if (!isCurrent) {
                            PreferenceUtil.switchToAccount(i)
                            ctx.startActivity(Intent(ctx, ctx::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                    shape = AppShapes.medium,
                    colors = CardDefaults.cardColors(if (isCurrent) AppColors.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(if (isCurrent) AppColors.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isCurrent) AppColors.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (acc.gameRoleName.isNotEmpty()) acc.gameRoleName else "账号 ${i + 1}",
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                            )
                            if (acc.gameRoleId.isNotEmpty()) {
                                Text("UID: ${acc.gameRoleId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (isCurrent) {
                            Text("当前", fontSize = 11.sp, color = AppColors.primary, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.background(AppColors.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        if (accounts.size > 1) {
                            IconButton(onClick = { showDeleteConfirm = i }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, "删除", Modifier.size(18.dp), tint = AppColors.red)
                            }
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    ctx.startActivity(Intent(ctx, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("add_account", true)
                    })
                },
                modifier = Modifier.weight(1f), shape = AppShapes.medium
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("添加账号")
            }
            OutlinedButton(
                onClick = {
                    accounts.clear()
                    accounts.addAll(PreferenceUtil.getAccountList())
                    Toast.makeText(ctx, "已刷新", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f), shape = AppShapes.medium
            ) { Text("刷新列表") }
        }
        Spacer(Modifier.height(12.dp))

        if (forRole) {
            SectionLabel("当前角色 · ${PreferenceUtil.getSelectedRoleName()}")
            val vm = viewModel<SettingsViewModel>()
            LaunchedEffect(Unit) { vm.load() }
            val roles = vm.r.value
            if (roles.isNotEmpty()) {
                roles.forEach { role ->
                    val isSel = role.roleId.toString() == roleId
                    Card(
                        onClick = { onRoleChanged?.invoke(role.roleId.toString(), role.roleName) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                        shape = AppShapes.medium,
                        colors = CardDefaults.cardColors(if (isSel) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${role.roleName} Lv.${role.lev}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(role.serverName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSel) Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = AppColors.green)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        SectionLabel("其他")
        SettingsCard(
            icon = Icons.Default.Feedback,
            title = "问题反馈",
            subtitle = "提交建议或报告问题",
            onClick = { showFeedback = true }
        )
        Spacer(Modifier.height(6.dp))
        SettingsCard(
            icon = Icons.Default.Description,
            title = "免责声明 & 用户协议",
            subtitle = "查看应用声明与协议",
            onClick = { showDisclaimer = true }
        )
        Spacer(Modifier.height(12.dp))

        if (onLogout != null) {
            Button(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(AppColors.red),
                shape = AppShapes.large
            ) { Text("退出登录", fontSize = 15.sp) }
            Spacer(Modifier.height(8.dp))
        }

        Text(
            "海特洛档案室 v1.0",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("退出确认") },
            text = { Text("确定要退出登录吗？退出后需要重新输入手机号登录。") },
            confirmButton = { TextButton(onClick = { onLogout?.invoke(); showLogoutConfirm = false }) { Text("退出", color = AppColors.red) } },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("取消") } }
        )
    }
    showDeleteConfirm?.let { idx ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除账号") },
            text = { Text("确定要删除账号 ${idx + 1} 吗？") },
            confirmButton = { TextButton(onClick = { PreferenceUtil.removeAccount(idx); accounts.removeAt(idx); showDeleteConfirm = null; if (accounts.isEmpty()) onLogout?.invoke() }) { Text("删除", color = AppColors.red) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp), tint = AppColors.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackScreen(onBack: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        TopAppBar(
            title = { Text("问题反馈", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
        )

        Column(Modifier.padding(horizontal = 16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("告诉我们你的想法", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("反馈内容至少5个字，我们将尽快处理", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        content, { content = it },
                        label = { Text("反馈内容 *") },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = AppShapes.medium,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = AppColors.primary
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        contact, { contact = it },
                        label = { Text("联系方式（QQ/邮箱，选填）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.medium,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = AppColors.primary
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (content.length < 5) { status = "反馈内容至少5个字"; return@Button }
                            isSending = true; status = ""
                            scope.launch {
                                try {
                                    val json = withContext(Dispatchers.IO) {
                                        val url = URL("http://112.124.68.4:3000/api/feedback")
                                        val conn = url.openConnection() as HttpURLConnection
                                        conn.requestMethod = "POST"
                                        conn.doOutput = true
                                        conn.setRequestProperty("Content-Type", "application/json")
                                        conn.connectTimeout = 5000; conn.readTimeout = 5000
                                        val gameUid = PreferenceUtil.getSelectedRoleId()?.toString() ?: ""
                                        val body = JSONObject().apply { put("content", content); put("contact", contact); put("uid", gameUid) }
                                        conn.outputStream.write(body.toString().toByteArray())
                                        JSONObject(conn.inputStream.bufferedReader().readText())
                                    }
                                    if (json.optInt("code") == 0) {
                                        status = "✅ 反馈已提交，感谢你的建议！"
                                        content = ""; contact = ""
                                    } else {
                                        status = "❌ ${json.optString("msg", "提交失败")}"
                                    }
                                } catch (e: Exception) {
                                    status = "❌ 网络错误：${e.message}"
                                }
                                isSending = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = AppShapes.medium,
                        enabled = !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("提交反馈", fontSize = 15.sp)
                        }
                    }

                    if (status.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(status, fontSize = 13.sp, color = if (status.startsWith("✅")) AppColors.green else AppColors.red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyConfigScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf(PreferenceUtil.getServerKey()) }
    var bindStatus by remember { mutableStateOf("") }
    var bindColor by remember { mutableStateOf(AppColors.darkTextSecondary) }
    val gameRoleId = PreferenceUtil.getSelectedRoleId()?.toString() ?: ""
    val gameRoleName = PreferenceUtil.getSelectedRoleName()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        TopAppBar(
            title = { Text("Key 绑定", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
        )

        Column(Modifier.padding(horizontal = 16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = AppColors.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("当前账号", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("角色: ${gameRoleName}", fontSize = 13.sp)
                    Text("游戏UID: ${gameRoleId}", fontSize = 13.sp, color = AppColors.primaryLight)
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, null, Modifier.size(20.dp), tint = AppColors.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("绑定 API Key", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("输入后台生成的 Key 绑定到当前账号", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        apiKey, {
                            apiKey = it; PreferenceUtil.saveServerKey(it); bindStatus = ""
                        },
                        label = { Text("API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.medium,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AppColors.primary,
                            cursorColor = AppColors.primary
                        )
                    )

                    if (bindStatus.isNotEmpty()) {
                        Text(bindStatus, fontSize = 12.sp, color = bindColor, modifier = Modifier.padding(top = 6.dp))
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (gameRoleId.isEmpty()) {
                                bindStatus = "❌ 未获取到游戏UID，请先刷新首页"
                                bindColor = AppColors.red
                                return@Button
                            }
                            scope.launch {
                                bindStatus = "正在绑定..."
                                bindColor = AppColors.darkTextSecondary
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        val url = URL("http://112.124.68.4:3000/api/key/bind")
                                        val conn = url.openConnection() as HttpURLConnection
                                        conn.requestMethod = "POST"
                                        conn.doOutput = true
                                        conn.setRequestProperty("Content-Type", "application/json")
                                        conn.connectTimeout = 5000; conn.readTimeout = 5000
                                        val body = JSONObject().apply {
                                            put("key", apiKey)
                                            put("gameRoleId", gameRoleId)
                                        }
                                        conn.outputStream.write(body.toString().toByteArray())
                                        JSONObject(conn.inputStream.bufferedReader().readText())
                                    }
                                    if (result.optInt("code") == 0) {
                                        val keyName = result.optJSONObject("data")?.optString("keyName", "")
                                        bindStatus = "✅ 绑定成功！Key: ${keyName} → UID: ${gameRoleId}"
                                        bindColor = AppColors.green
                                        PreferenceUtil.saveServerKey(apiKey)
                                    } else {
                                        bindStatus = "❌ ${result.optString("msg", "绑定失败")}"
                                        bindColor = AppColors.red
                                    }
                                } catch (e: Exception) {
                                    bindStatus = "❌ 网络错误：${e.message}"
                                    bindColor = AppColors.red
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = AppShapes.medium,
                        enabled = apiKey.isNotEmpty()
                    ) { Text("绑定到当前账号", fontSize = 15.sp) }
                }
            }
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("为什么要绑定？", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("绑定后可以确保你的账号正常使用各项功能，同时方便我们为你提供更好的服务。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("只需要操作一次，后续会自动保持关联，不会影响你的正常使用。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}