package com.yh.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    val primary = Color(0xFF8B5CF6)
    val primaryLight = Color(0xFFA78BFA)
    val primaryDark = Color(0xFF7C3AED)
    val secondary = Color(0xFFF59E0B)
    val accent = Color(0xFFEC4899)
    val cyan = Color(0xFF06B6D4)
    val green = Color(0xFF22C55E)
    val red = Color(0xFFEF4444)
    val orange = Color(0xFFF97316)

    val darkBg = Color(0xFF0A0A1A)
    val darkSurface = Color(0xFF13132B)
    val darkSurfaceVariant = Color(0xFF1C1C3A)
    val darkBorder = Color(0xFF2D2D4A)
    val darkText = Color(0xFFF1F1F9)
    val darkTextSecondary = Color(0xFF94A3B8)

    val lightBg = Color(0xFFFFF8F0)
    val lightSurface = Color(0xFFFFFFFF)
    val lightSurfaceVariant = Color(0xFFF1F5F9)
    val lightBorder = Color(0xFFE2E8F0)
    val lightText = Color(0xFF1E293B)
    val lightTextSecondary = Color(0xFF64748B)

    val rarityColors = listOf(
        Color(0xFF94A3B8),
        Color(0xFF22C55E),
        Color(0xFF06B6D4),
        Color(0xFF8B5CF6),
        Color(0xFFF59E0B)
    )

    val cardGradient = Brush.linearGradient(
        colors = listOf(darkSurface, darkSurfaceVariant),
        start = Offset(0f, 0f), end = Offset(300f, 300f)
    )
    val headerGradient = Brush.linearGradient(
        colors = listOf(primary, primaryDark),
        start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
    val accentGradient = Brush.linearGradient(
        colors = listOf(primary, accent),
        start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
}

object AppShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(20.dp)
    val round = RoundedCornerShape(50)
}