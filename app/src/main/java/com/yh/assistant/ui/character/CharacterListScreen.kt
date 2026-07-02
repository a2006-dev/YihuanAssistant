package com.yh.assistant.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yh.assistant.data.model.*
import com.yh.assistant.data.repository.RoleRepository
import com.yh.assistant.ui.*
import com.yh.assistant.util.AssetUrl
import com.yh.assistant.util.CacheManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterViewModel : ViewModel() {
    private val _chars = MutableStateFlow<List<CharacterDetail>>(emptyList()); val chars: StateFlow<List<CharacterDetail>> = _chars
    private val L = MutableStateFlow(false); val l: StateFlow<Boolean> = L
    private val _error = MutableStateFlow(false); val error: StateFlow<Boolean> = _error
    private var loadedRid = ""
    fun load(roleId: String, force: Boolean = false) {
        if (roleId.isEmpty()) return
        loadedRid = roleId
        L.value = true; _error.value = false
        viewModelScope.launch {
            RoleRepository.getCharacters(roleId).onSuccess {
                _chars.value = sort(it)
                CacheManager.cacheCharacters(roleId, it)
                _error.value = false
            }.onFailure { _error.value = true }
            L.value = false
        }
    }
    private fun sort(l: List<CharacterDetail>) = l.sortedByDescending { it.alev }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(roleId: String, refreshTrigger: Int = 0, onDetailOpen: (Boolean) -> Unit = {}, vm: CharacterViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.load(roleId) }
    LaunchedEffect(refreshTrigger) { if (refreshTrigger > 0) vm.load(roleId, force = true) }
    var sel by remember { mutableStateOf<CharacterDetail?>(null) }
    LaunchedEffect(sel != null) { onDetailOpen(sel != null) }
    if (sel != null) { CharacterDetailScreen(char = sel!!, onBack = { sel = null }); return }

    val chars = vm.chars.value
    val loading = vm.l.value
    val hasError = vm.error.value

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("角色列表", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (chars.isNotEmpty()) Text("${chars.size} 个角色 · 按等级排序", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        }

        if (loading && chars.isEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(6) {
                    ShimmerBlock(Modifier.fillMaxWidth().height(170.dp), AppShapes.large)
                }
            }
        } else if (hasError && chars.isEmpty()) {
            ErrorState("加载角色列表失败", onRetry = { vm.load(roleId, force = true) })
        } else if (chars.isEmpty()) {
            EmptyState(title = "暂无角色数据", subtitle = "请确认游戏内已创建角色")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(chars) { ch ->
                    CharacterGridCard(ch, onClick = { sel = ch })
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterGridCard(ch: CharacterDetail, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().height(170.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AsyncImage(
                AssetUrl.characterAvatar(ch.id), ch.name,
                Modifier.size(68.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(10.dp))
            Text(ch.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Lv.${ch.alev}", fontSize = 12.sp, color = AppColors.secondary)
        }
    }
}