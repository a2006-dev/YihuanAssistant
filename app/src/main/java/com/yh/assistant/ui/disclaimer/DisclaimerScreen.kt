package com.yh.assistant.ui.disclaimer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerScreen(onAccepted: () -> Unit, showConfirm: Boolean = true) {
    var agreed by remember { mutableStateOf(false) }
var tab by remember { mutableStateOf(0) } 
    val scrollState = rememberScrollState()
    var scrollAtBottom by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        scrollAtBottom = scrollState.value >= scrollState.maxValue - 10
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A1A))) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(20.dp))
            Text("海特洛档案室", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
            Spacer(Modifier.height(4.dp))
            Text("请仔细阅读以下协议", fontSize = 14.sp, color = Color(0xFF94A3B8))
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, modifier = Modifier.weight(1f)) { Text("免责声明", fontSize = 13.sp) }
                Tab(selected = tab == 1, onClick = { tab = 1 }, modifier = Modifier.weight(1f)) { Text("用户协议", fontSize = 13.sp) }
            }
            Spacer(Modifier.height(12.dp))

            Card(Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color(0xFF13132B))) {
                Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
                    val text = if (tab == 0) disclaimerText else agreementText
                    Text(text, fontSize = 13.sp, lineHeight = 20.sp, color = Color(0xFFC8C8E0))
                }
            }

            if (tab == 0 && !scrollAtBottom) {
                Spacer(Modifier.height(8.dp))
                Text("请滑动到底部", fontSize = 12.sp, color = Color(0xFFF59E0B))
            }

            Spacer(Modifier.height(12.dp))
            if (showConfirm) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { agreed = !agreed }) {
                    Checkbox(checked = agreed, onCheckedChange = { agreed = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF8B5CF6), uncheckedColor = Color(0xFF64748B)))
                    Spacer(Modifier.width(8.dp))
                    Text("我已阅读并同意以上条款", fontSize = 14.sp, color = if (agreed) Color(0xFFE2E8F0) else Color(0xFF94A3B8))
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAccepted, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = agreed,
                    shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6), disabledContainerColor = Color(0xFF2D2D4A))) {
                    Text("确认并进入", color = if (agreed) Color.White else Color(0xFF94A3B8))
                }
            } else {
                Button(onClick = onAccepted, modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))) {
                    Text("关闭")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private val disclaimerText = """
【免责声明】

1. 非官方声明
本应用是第三方开发的非官方工具，与完美世界、Hotta Studio、塔吉多社区及其关联公司不存在任何关联或授权关系。所有游戏内容及商标归各自权利人所有。

2. 数据安全
本应用不收集、不上传您的任何敏感信息。登录Token仅用于API请求，存储在本地设备中。您的手机号、验证码等隐私数据不会传输至任何第三方服务器。

3. 使用风险
本应用按"现状"提供，不提供任何明示或暗示的保证。开发者不对因使用本应用造成的任何直接或间接损失承担责任。

4. 合规性
用户应自行确保使用本应用符合当地法律法规。如游戏官方禁止使用第三方工具，用户应自行承担相关风险。

5. 协议变更
本免责声明可能随时更新，更新后的版本将在应用内展示。继续使用即表示接受修改后的条款。
""".trimIndent()

private val agreementText = """
【用户协议】

1. 账号与数据
1.1 您需要自行获取塔吉多社区账号及登录凭据。
1.2 本应用仅提供数据展示功能，不存储您的账号密码。
1.3 您的游戏数据通过塔吉多官方API获取，本应用不做任何修改。

2. 使用规范
2.1 您承诺不会利用本应用从事任何违法违规活动。
2.2 您承诺不会利用本应用对游戏服务器进行攻击、干扰或滥用API。
2.3 您承诺不会将本应用用于商业用途。

3. 知识产权
3.1 本应用代码以开源方式提供，遵循相应开源协议。
3.2 游戏相关的所有数据、图片、图标等知识产权归游戏开发商所有。

4. 免责条款
4.1 开发者不对因API接口变更、游戏更新等原因导致的功能失效承担责任。
4.2 开发者不对因第三方（包括但不限于网络服务商、云服务提供商）导致的服务中断承担责任。
4.3 您理解并同意，使用本应用需要您自行承担相关风险。

5. 其他
5.1 本协议适用中华人民共和国法律。
5.2 如本协议的任何条款被认定为无效或不可执行，其余条款仍然有效。
""".trimIndent()