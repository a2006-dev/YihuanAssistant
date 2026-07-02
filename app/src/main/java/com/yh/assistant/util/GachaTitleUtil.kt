package com.yh.assistant.util
import com.yh.assistant.data.model.GachaPool
object GachaTitleUtil {
    fun genTitle(pools: List<GachaPool>): String {
        val allPools = pools.filter {
            val tab = it.tab
            tab.contains("限定") || tab.contains("常驻") || tab.contains("弧盘")
        }
        if (allPools.isEmpty()) return "暂无数据"
        var totalDraws = 0
        var totalBlues = 0.0
        var totalRare = 0
        var lastPoolEarly = false
        for (pool in allPools) {
            totalDraws += pool.drawCount
            totalRare += pool.rareCount
            pool.details.forEach { d ->
                if (d.luckyType == 0) totalBlues++
            }
            val avg = pool.average.toDoubleOrNull() ?: pool.m.toDouble()
            if (avg < pool.m * 0.7) lastPoolEarly = true
        }
        val overallAvg = if (totalRare > 0) totalDraws.toDouble() / totalRare else 0.0
        val blueRatio = if (totalRare > 0) totalBlues / totalRare else 0.0
        return when {
            totalRare <= 2 -> "初入异象"
            overallAvg < 30 && blueRatio < 0.2 -> "天命所归"
            overallAvg < 45 && blueRatio < 0.3 -> "欧气爆棚"
            overallAvg < 55 && blueRatio < 0.4 -> "好运连连"
            overallAvg < 65 && blueRatio < 0.5 -> "概率守恒"
            blueRatio > 0.7 -> "蓝天白云"
            blueRatio > 0.5 -> "大保底专业户"
            overallAvg > 70 -> "非酋之王"
            else -> "异象调查员"
        }
    }
}