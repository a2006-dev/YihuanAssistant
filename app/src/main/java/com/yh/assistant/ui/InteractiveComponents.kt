package com.yh.assistant.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale"
    )

    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = shape,
        colors = colors,
        elevation = elevation,
        enabled = enabled,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Inbox,
    title: String = "暂无数据",
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))
            Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String = "加载失败",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CloudOff, null, Modifier.size(56.dp), tint = AppColors.red.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            Text(message, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            if (onRetry != null) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onRetry, shape = AppShapes.medium) {
                    Text("重试", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun shimmerColor(): Color {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.08f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse)
    )
    return MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
}

@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = AppShapes.large
) {
    Card(modifier = modifier, shape = shape, colors = CardDefaults.cardColors(shimmerColor())) {}
}

@Composable
fun QualityTag(quality: String, modifier: Modifier = Modifier) {
    val isA = quality.contains("A", ignoreCase = true) || quality.contains("BLUE", ignoreCase = true)
    val isS = quality.contains("S", ignoreCase = true) || quality.contains("PURPLE", ignoreCase = true) || quality.contains("ORANGE", ignoreCase = true)
    val color = when {
        isS -> AppColors.secondary       // S = 金色
        isA -> AppColors.primaryLight    // A = 紫色
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        isS -> "S"
        isA -> "A"
        else -> quality.take(2)
    }
    Text(
        label,
        fontSize = 9.sp, fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier.background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
fun DividerWithText(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Text(
            text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    }
}