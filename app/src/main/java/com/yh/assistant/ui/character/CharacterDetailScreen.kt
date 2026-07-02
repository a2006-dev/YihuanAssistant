package com.yh.assistant.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yh.assistant.data.model.*
import com.yh.assistant.ui.AppColors
import com.yh.assistant.ui.AppShapes
import com.yh.assistant.util.AssetUrl
import com.yh.assistant.util.PreferenceUtil
import com.yh.assistant.util.ShareRenderUtil

fun filterTags(s: String): String = s.replace(Regex("<[^>]*>"), "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(char: CharacterDetail, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val roleId = PreferenceUtil.getSelectedRoleId()?.toString() ?: ""
    val roleName = PreferenceUtil.getSelectedRoleName()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(char.name, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = {
                        ShareRenderUtil.shareCharacter(ctx, char, roleId, roleName)
                    }) { Icon(Icons.Default.Share, "分享") }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())) {

            Card(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                shape = AppShapes.large,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.padding(14.dp)) {
                    AsyncImage(
                        AssetUrl.characterDetail(char.id), char.name,
                        Modifier.width(130.dp).aspectRatio(0.7f).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(char.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(AssetUrl.elementIcon(char.elementType), null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(char.elementType.removePrefix("CHARACTER_ELEMENT_TYPE_"), fontSize = 12.sp, color = AppColors.primaryLight)
                            Spacer(Modifier.width(10.dp))
                            Text("Lv.${char.alev}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.secondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (char.awakenLev > 0) Text("觉醒 ${char.awakenLev}", fontSize = 11.sp, color = AppColors.accent)
                            Text("好感 ${char.likeabilitylev}", fontSize = 11.sp, color = AppColors.cyan)
                        }
                        Spacer(Modifier.height(8.dp))
                        char.properties.filter { p -> (p.value.replace("%","").toDoubleOrNull() ?: 0.0) > 0.0 }.forEach { prop ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(AssetUrl.propertyIcon(prop.id), null, Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(prop.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(prop.value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AppColors.secondary)
                            }
                        }
                    }
                }
            }

            char.fork?.let { fork ->
                Spacer(Modifier.height(6.dp))
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = AppShapes.large,
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("弧盘", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.primaryLight)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(AssetUrl.fork(fork.id), fork.name, Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(fork.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Lv.${fork.alev} · 突破${fork.blev} · ${fork.slev}星", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                fork.properties.forEach { p ->
                                    Text("${p.name} ${p.value}", fontSize = 11.sp, color = AppColors.secondary, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            char.suit?.let { suit ->
                Spacer(Modifier.height(6.dp))
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = AppShapes.large,
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("驱动套装", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.primaryLight)
                            Text("${suit.suitActivateNum}/4", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.secondary)
                        }
                        if (suit.des2.isNotEmpty()) Text(filterTags(suit.des2), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        if (suit.des4.isNotEmpty()) Text(filterTags(suit.des4), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(10.dp))
                        Text("核心", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        suit.core.forEach { core ->
                            Spacer(Modifier.height(4.dp))
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = AppShapes.medium,
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("${core.name} Lv.${core.lev}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    core.mainProperties.forEach { p -> Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("主 ${p.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(p.value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AppColors.secondary) } }
                                    core.properties.forEach { p -> Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(p.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(p.value, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                                }
                            }
                        }

                        Text("部件", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        suit.pie.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { piece ->
                                    Card(
                                        Modifier.weight(1f).padding(vertical = 3.dp),
                                        shape = AppShapes.medium,
                                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(Modifier.padding(10.dp)) {
                                            Text(piece.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            piece.mainProperties.forEach { p -> Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("主 ${p.name}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(p.value, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = AppColors.secondary) } }
                                            piece.properties.forEach { p -> Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(p.name, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(p.value, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                                        }
                                    }
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (char.skills.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = AppShapes.large,
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("技能", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.primaryLight)
                        char.skills.take(8).forEach { skill ->
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(AssetUrl.skillIcon(skill.id), null, Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("${skill.name} Lv.${skill.level}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    skill.items.forEach { item ->
                                        if (item.title.isNotEmpty()) Text(filterTags(item.title), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        if (item.desc.isNotEmpty()) Text(filterTags(item.desc), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}