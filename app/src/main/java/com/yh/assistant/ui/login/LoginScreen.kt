package com.yh.assistant.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yh.assistant.data.repository.AuthRepository
import com.yh.assistant.ui.AppColors
import com.yh.assistant.ui.AppShapes
import com.yh.assistant.ui.PressableCard
import com.yh.assistant.util.PreferenceUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _phone = MutableStateFlow(""); val phone: StateFlow<String> = _phone
    private val _code = MutableStateFlow(""); val code: StateFlow<String> = _code
    private val _loading = MutableStateFlow(false); val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error
    private val _cd = MutableStateFlow(0); val cd: StateFlow<Int> = _cd

    fun setPhone(p: String) { _phone.value = p }
    fun setCode(c: String) { _code.value = c }

    fun sendSms() {
        val key = PreferenceUtil.getServerKey()
        if (key.isEmpty()) { _error.value = "请先在输入框填写 API Key"; return }
        if (_cd.value > 0) return; _error.value = null
        viewModelScope.launch {
            AuthRepository.sendSms(_phone.value)
                .onSuccess {
                    _cd.value = 60
                    launch {
                        while (_cd.value > 0) {
                            kotlinx.coroutines.delay(1000)
                            _cd.value -= 1
                        }
                    }
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun login(onSuccess: () -> Unit) {
        _loading.value = true; _error.value = null
        viewModelScope.launch {
            AuthRepository.login(_phone.value, _code.value)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(vm: LoginViewModel = viewModel(), onLoggedIn: () -> Unit, onBack: (() -> Unit)? = null) {
    var showPhoneLogin by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(PreferenceUtil.getServerKey()) }
    val accounts = remember { mutableStateListOf<PreferenceUtil.AccountInfo>() }
    LaunchedEffect(Unit) { accounts.addAll(PreferenceUtil.getAccountList()) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Color(0xFF0A0A1A))))) {
        if (showPhoneLogin) {
            Column(Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showPhoneLogin = false }) {
                        Text("< 返回", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("手机号登录", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.width(48.dp))
                }
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(apiKey, {
                    apiKey = it; PreferenceUtil.saveServerKey(it)
                }, label = { Text("API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline))
                Spacer(Modifier.height(16.dp))
                Icon(painterResource(com.yh.assistant.R.mipmap.ic_launcher), null, modifier = Modifier.size(64.dp).clip(CircleShape), tint = Color.Unspecified)
                Spacer(Modifier.height(36.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(vm.phone.collectAsState().value, { vm.setPhone(it) },
                            label = { Text("手机号") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.medium, textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, cursorColor = MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(vm.code.collectAsState().value, { vm.setCode(it) },
                                label = { Text("验证码") }, singleLine = true, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp), textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, cursorColor = MaterialTheme.colorScheme.primary))
                            Button({ vm.sendSms() }, enabled = vm.cd.collectAsState().value == 0,
                                shape = RoundedCornerShape(12.dp), modifier = Modifier.height(56.dp).width(120.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContainerColor = MaterialTheme.colorScheme.surface)) {
                                Text(if (vm.cd.collectAsState().value > 0) "${vm.cd.collectAsState().value}s" else "获取验证码",
                                    color = if (vm.cd.collectAsState().value > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Button({ vm.login { onLoggedIn() } }, Modifier.fillMaxWidth().height(50.dp), enabled = !vm.loading.collectAsState().value,
                            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                            if (vm.loading.collectAsState().value) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("登录", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        vm.error.collectAsState().value?.let { err ->
                            Spacer(Modifier.height(10.dp))
                            Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("登录即代表同意用户协议和隐私政策", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.weight(1f))
                Icon(painterResource(com.yh.assistant.R.mipmap.ic_launcher), null, modifier = Modifier.size(72.dp).clip(CircleShape), tint = Color.Unspecified)
                Spacer(Modifier.height(12.dp))
                Text("海特洛档案室", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                Text(if (accounts.isEmpty()) "暂无已保存的账号" else "选择账号登录", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Column(Modifier.padding(horizontal = 32.dp)) {
                    if (accounts.isEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("点击下方按钮添加账号", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        accounts.forEachIndexed { i, acc ->
                            val isCurrent = i == PreferenceUtil.getCurrentAccountIndex()
                            PressableCard(onClick = {
                                PreferenceUtil.switchToAccount(i)
                                AuthRepository.restoreSession()
                                onLoggedIn()
                            }, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
                                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                        Text("${i + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(if (acc.gameRoleName.isNotEmpty()) acc.gameRoleName else "账号 ${i + 1}", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        if (acc.gameRoleId.isNotEmpty()) Text("${acc.gameRoleId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isCurrent) Text("当前", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showPhoneLogin = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                        Icon(Icons.Default.Phone, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (accounts.isEmpty()) "添加账号" else "其他方式登录", fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Spacer(Modifier.weight(1f))
            }
        }
    }
}