package com.yh.assistant.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import com.yh.assistant.data.model.CharacterDetail
import com.yh.assistant.data.model.GachaData
import java.io.File
import java.io.FileOutputStream

object ShareRenderUtil {
    private val BG = Color.parseColor("#0A0A1A")!!
    private val CARD = Color.parseColor("#13132B")!!
    private val TEXT_MAIN = Color.parseColor("#F1F1F9")!!
    private val TEXT_SUB = Color.parseColor("#94A3B8")!!
    private val GOLD = Color.parseColor("#F59E0B")!!
    private val PURPLE = Color.parseColor("#8B5CF6")!!
    private val RED = Color.parseColor("#EF4444")!!
    private val CYAN = Color.parseColor("#06B6D4")!!

    fun shareGacha(ctx: Context, data: GachaData, uid: String, name: String) {
        val w = 900f; val p = 30f
        val items = mutableListOf<RenderItem>()
        val bigP = Paint().apply { color = TEXT_MAIN; textSize = 26f; isAntiAlias = true }
        val subP = Paint().apply { color = TEXT_SUB; textSize = 20f; isAntiAlias = true }
        val goldP = Paint().apply { color = GOLD; textSize = 26f; isFakeBoldText = true; isAntiAlias = true }

        items += CardStart()
        items += TextLine("抽卡分析", Paint().apply { color = GOLD; textSize = 36f; isFakeBoldText = true; isAntiAlias = true })
        items += TextLine("UID: ${uid} · ${name}", subP)
        items += CardEnd()
        items += Spacer(12f)

        val totalDraws = data.gachaDetails.sumOf { it.drawCount }
        val totalGold = data.gachaDetails.sumOf { it.rareCount }
        val avgPity = if (totalGold > 0) totalDraws / totalGold else 0
        items += CardStart()
        items += TextLine("总抽数: ${totalDraws}    出货: ${totalGold}    平均: ${avgPity}抽/S", bigP)
        items += CardEnd()
        items += Spacer(16f)

        data.gachaDetails.forEachIndexed { poolIdx, pool ->
            items += CardStart()
            items += TextLine("${pool.tab}  ${pool.rareCount}S  平均${pool.average}抽  共${pool.drawCount}抽", goldP)
            items += CardEnd()
            items += Spacer(6f)
            if (pool.details.isEmpty()) {
                items += TextLine("暂无出货记录", subP)
            } else {
                pool.details.forEach { detail ->
                    val chName = CHAR_NAMES[detail.charid] ?: FORK_NAMES[detail.charid] ?: detail.charid
                    val pulls = detail.rareCount; val maxPity = if (pool.m > 0) pool.m else 90
                    val pct = (pulls.toFloat() / maxPity * 100).toInt().coerceIn(0, 100)
                    val tag = when { pulls <= 10 -> "欧皇"; pulls <= 40 -> "小欧"; pulls >= 70 -> "非"; else -> "" }
                    val tagColor = when (tag) { "欧皇" -> GOLD; "小欧" -> CYAN; "非" -> RED; else -> 0 }
                    items += CardStart()
                    val nameP = Paint().apply { color = TEXT_MAIN; textSize = 22f; isAntiAlias = true }
                    items += TextLine("${chName}  ${pulls}抽", nameP)
                    items += BarWithTag(w - p * 2 - 20f, 6f, pct, if (pulls <= 40) intArrayOf(CYAN, GOLD) else intArrayOf(GOLD, RED), tag, tagColor)
                    items += CardEnd()
                }
            }
            if (poolIdx < data.gachaDetails.size - 1) items += Spacer(10f)
        }
        items += Spacer(20f)
        renderAndShare(ctx, w, p, items, "gacha.png")
    }

    fun shareCharacter(ctx: Context, char: CharacterDetail, uid: String, name: String) {
        val w = 900; val p = 30; val gap = 10; val cr = 14f; val lh = 36; val sh = 30
        val goldP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD; textSize = 26f; isFakeBoldText = true }
        val subP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TEXT_SUB; textSize = 18f }
        val mainP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TEXT_MAIN; textSize = 20f }
        val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TEXT_MAIN; textSize = 15f }
        val goldS = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD; textSize = 15f; isFakeBoldText = true }
        val purpleP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PURPLE; textSize = 17f; isFakeBoldText = true }
        val cardP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARD }
        fun Int.f() = toFloat()

        val infoH = 28 + lh * 2 + gap
        val fLines = if (char.fork != null) 1 + 1 + 1 + char.fork!!.properties.size else 0
        val fH = 28 + fLines * sh + gap
        val propC = char.properties.count { (it.value.replace("%","").toDoubleOrNull() ?: 0.0) > 0.0 }
        val pH = 28 + propC * sh + gap
        val colH = maxOf(fH, pH, 80); val colW = (w - p * 2 - gap) / 2
        val cores = char.suit?.core ?: emptyList()
        var coreH = 0; var coreColW = 0
        if (cores.isNotEmpty()) {
            coreColW = (w - p * 2 - gap) / 2; var maxC = 0
            for (c in cores) { val ch = 28 + sh * 2 + c.mainProperties.size * sh + c.properties.size * sh + gap; if (ch > maxC) maxC = ch }
            coreH = maxOf(maxC, 70)
        }
        val pies = char.suit?.pie ?: emptyList(); var pieH = 0; var pieW = 0
        if (pies.isNotEmpty()) {
            pieW = (w - p * 2 - gap * 2) / 3; var maxP = 0
            for (pi in pies) { val ph = 28 + sh * 2 + pi.mainProperties.size * sh + pi.properties.size * sh + gap; if (ph > maxP) maxP = ph }
            pieH = maxOf(maxP, 50)
        }
        val sCount = char.skills?.size ?: 0; val sH = if (sCount > 0) 28 + sCount * (sh + 2) + gap else 0
        val pieRows = (pies.size + 2) / 3
        val totalH = p * 2 + infoH + gap + colH + gap + coreH + gap + pieRows * (pieH + gap) + sH + gap + 80
        val bitmap = Bitmap.createBitmap(w, totalH, Bitmap.Config.ARGB_8888); val canvas = Canvas(bitmap); canvas.drawColor(BG)
        var y = p

        val avatarBm = try { val s = java.net.URL(AssetUrl.characterAvatar(char.id)).openStream(); val b = android.graphics.BitmapFactory.decodeStream(s); s.close(); if (b != null) android.graphics.Bitmap.createScaledBitmap(b, 42, 42, true) else null } catch (_: Exception) { null }
        val headX = p + gap + (if (avatarBm != null) 50 else 0)
        canvas.drawRoundRect(p.f(), y.f(), (w - p).f(), (y + infoH).f(), cr, cr, cardP)
        if (avatarBm != null) { val cp = android.graphics.Path().apply { addRoundRect(0f, 0f, 42f, 42f, 10f, 10f, android.graphics.Path.Direction.CW) }; canvas.save(); canvas.translate((p + gap).f(), (y + gap + 2).f()); canvas.clipPath(cp); canvas.drawBitmap(avatarBm, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG)); canvas.restore() }
        y += gap; drawT(canvas, "${char.name} Lv.${char.alev}", goldP, headX, y); y += lh; drawT(canvas, "UID: ${uid} · ${name}", subP, headX, y); y += lh
        drawT(canvas, "元素: ${char.elementType.removePrefix("CHARACTER_ELEMENT_TYPE_")}    好感: ${char.likeabilitylev}", subP, headX, y)
        y = p + infoH + gap

        val forkBm = char.fork?.let { try { val s = java.net.URL(AssetUrl.fork(it.id)).openStream(); val b = android.graphics.BitmapFactory.decodeStream(s); s.close(); if (b != null) android.graphics.Bitmap.createScaledBitmap(b, 32, 32, true) else null } catch (_: Exception) { null } }
        val fxOff = p + gap + (if (forkBm != null) 40 else 0)
        char.fork?.let { fork -> canvas.drawRoundRect(p.f(), y.f(), (p + colW).f(), (y + colH).f(), cr, cr, cardP); var fy = y + gap
            if (forkBm != null) { val cp = android.graphics.Path().apply { addRoundRect(0f, 0f, 32f, 32f, 8f, 8f, android.graphics.Path.Direction.CW) }; canvas.save(); canvas.translate((p + gap).f(), fy.f()); canvas.clipPath(cp); canvas.drawBitmap(forkBm, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG)); canvas.restore() }
            drawT(canvas, "武器: ${fork.name}", mainP, fxOff, fy); fy += sh; drawT(canvas, "Lv.${fork.alev} 突破${fork.blev} ${fork.slev}星", subP, fxOff, fy); fy += sh; fork.properties.forEach { drawT(canvas, "${it.name}: ${it.value}", whiteP, fxOff, fy); fy += sh } }
        canvas.drawRoundRect((p + colW + gap).f(), y.f(), (w - p).f(), (y + colH).f(), cr, cr, cardP); var py = y + gap
        drawT(canvas, "面板", mainP, p + colW + gap * 2, py); py += sh
        char.properties.filter { (it.value.replace("%","").toDoubleOrNull() ?: 0.0) > 0.0 }.forEach { drawT(canvas, "${it.name}: ${it.value}", whiteP, p + colW + gap * 2, py); py += sh }
        y += colH + gap
        if (cores.isNotEmpty()) { cores.forEachIndexed { idx, core -> val cx = p + idx * (coreColW + gap); canvas.drawRoundRect(cx.f(), y.f(), (cx + coreColW).f(), (y + coreH).f(), cr, cr, cardP); var cy = y + gap; drawT(canvas, "核心: ${core.name}", goldS, cx + gap, cy); cy += sh; drawT(canvas, "Lv.${core.lev}", subP, cx + gap, cy); cy += sh; core.mainProperties.forEach { drawT(canvas, "主 ${it.name}: ${it.value}", whiteP, cx + gap, cy); cy += sh }; core.properties.forEach { drawT(canvas, "副 ${it.name}: ${it.value}", subP, cx + gap, cy); cy += sh } }; y += coreH + gap }
        if (pies.isNotEmpty()) { pies.chunked(3).forEach { chunk -> chunk.forEachIndexed { idx, piece -> val px = p + idx * (pieW + gap); canvas.drawRoundRect(px.f(), y.f(), (px + pieW).f(), (y + pieH).f(), cr, cr, cardP); var py2 = y + gap; drawT(canvas, "${piece.name}", goldS, px + gap, py2); py2 += sh; drawT(canvas, "Lv.${piece.lev}", subP, px + gap, py2); py2 += sh; piece.mainProperties.forEach { drawT(canvas, "主 ${it.name}: ${it.value}", whiteP, px + gap, py2); py2 += sh }; piece.properties.forEach { drawT(canvas, "副 ${it.name}: ${it.value}", subP, px + gap, py2); py2 += sh } }; y += pieH + gap } }
        if (sCount > 0) { val sH2 = 28 + sCount * (sh + 2) + gap; canvas.drawRoundRect(p.f(), y.f(), (w - p).f(), (y + sH2).f(), cr, cr, cardP); var sy = y + gap; drawT(canvas, "技能", mainP, p + gap, sy); sy += sh + 2; char.skills.take(6).forEach { drawT(canvas, "${it.name} Lv.${it.level}", purpleP, p + gap, sy); sy += sh + 2 }; y += sH2 + gap }
        drawWatermark(canvas, w, p, y.toFloat(), ctx); saveAndShare(ctx, bitmap, "character_${char.id}.png")
    }

    private fun drawT(c: Canvas, t: String, p: Paint, x: Int, y: Int) { c.drawText(t, x.toFloat(), (y + 26).toFloat(), p) }
    private fun drawWatermark(canvas: Canvas, w: Int, p: Int, y: Float, ctx: Context) {
        val wp = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 24f; isFakeBoldText = true; textSkewX = -0.25f; shader = LinearGradient(0f, 0f, 400f, 0f, intArrayOf(Color.parseColor("#8B5CF6")!!, Color.parseColor("#F59E0B")!!, Color.parseColor("#EC4899")!!), null, Shader.TileMode.CLAMP) }
        val iconSize = 36f; val tw = wp.measureText("海特洛档案室"); val totalW = iconSize + 8f + tw; val startX = w - p - totalW
        val iconBm = try { val res = ctx.resources; val id = res.getIdentifier("ic_launcher", "mipmap", ctx.packageName); val b = android.graphics.BitmapFactory.decodeResource(res, id); if (b != null) android.graphics.Bitmap.createScaledBitmap(b, iconSize.toInt(), iconSize.toInt(), true) else null } catch (_: Exception) { null }
        if (iconBm != null) { val cp = android.graphics.Path().apply { addRoundRect(0f, 0f, iconSize, iconSize, 8f, 8f, android.graphics.Path.Direction.CW) }; canvas.save(); canvas.translate(startX, y); canvas.clipPath(cp); canvas.drawBitmap(iconBm, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG)); canvas.restore() }
        canvas.drawText("海特洛档案室", startX + iconSize + 8f, y + 28f, wp)
    }

    private sealed class RenderItem
    private class TextLine(val text: String, val paint: Paint) : RenderItem()
    private class CardStart : RenderItem(); private class CardEnd : RenderItem()
    private class BarWithTag(val barW: Float, val barH: Float, val percent: Int, val colors: IntArray, val tag: String, val tagColor: Int) : RenderItem()
    private class Spacer(val height: Float) : RenderItem()

    private fun renderAndShare(ctx: Context, w: Float, p: Float, items: List<RenderItem>, fileName: String) {
        val lineH = 40f; val cardPad = 8f; val cardStarts = mutableListOf<Int>(); val cardEnds = mutableListOf<Int>()
        items.forEachIndexed { i, it -> if (it is CardStart) cardStarts.add(i); if (it is CardEnd) cardEnds.add(i) }
        fun calcH(s: Int, e: Int): Float { var h = cardPad * 2; for (j in s + 1 until e) { when (items[j]) { is TextLine -> h += lineH; is Spacer -> h += (items[j] as Spacer).height; is BarWithTag -> h += 28f; is CardStart -> {}; is CardEnd -> {} } }; return h }
        var estY = p * 2f; var ci = 0; var ii = 0
        while (ii < items.size) { when (items[ii]) { is CardStart -> { val ei = cardEnds[ci]; ci++; estY += calcH(ii, ei) + 12f; ii = ei }; is Spacer -> estY += (items[ii] as Spacer).height; is TextLine -> estY += lineH; is BarWithTag -> estY += 28f; else -> {} }; ii++ }
        estY += 100f; val h = estY.toInt().coerceAtLeast(400)
        val bitmap = Bitmap.createBitmap(w.toInt(), h, Bitmap.Config.ARGB_8888); val canvas = Canvas(bitmap); canvas.drawColor(BG)
        var y = p; ci = 0; ii = 0
        while (ii < items.size) {
            when (items[ii]) {
                is CardStart -> { val ei = cardEnds[ci]; ci++; val ch = calcH(ii, ei); val cr = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARD }; canvas.drawRoundRect(p, y, w - p, y + ch, 16f, 16f, cr); y += cardPad; ii++; while (ii < ei) { when (val it = items[ii]) { is TextLine -> { canvas.drawText(it.text, p + 14f, y + 32f, it.paint); y += lineH }; is Spacer -> y += it.height; is BarWithTag -> { val bt = it; val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bt.tagColor; textSize = 18f; isFakeBoldText = true }; val tw = tp.measureText(bt.tag) + 10f; val tagP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bt.tagColor and 0x00FFFFFF or 0x26000000.toInt() }; val barX = p + 14f + bt.barW - tw - 8f; val barStart = p + 14f; val barEnd = barX - 4f; if (barEnd > barStart) { val bp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF.toInt(); strokeCap = Paint.Cap.ROUND }; val bw = barEnd - barStart; canvas.drawRoundRect(barStart, y + 8f, barEnd, y + 8f + bt.barH, bt.barH / 2, bt.barH / 2, bp); val fillPct = bt.percent / 100f; if (fillPct > 0) { val fillW = bw * fillPct; val gs = LinearGradient(0f, 0f, fillW, 0f, bt.colors, null, Shader.TileMode.CLAMP); val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gs; strokeCap = Paint.Cap.ROUND }; canvas.drawRoundRect(barStart, y + 8f, barStart + fillW, y + 8f + bt.barH, bt.barH / 2, bt.barH / 2, fp) } }; canvas.drawRoundRect(barX, y + 6f, barX + tw, y + 24f, 8f, 8f, tagP); canvas.drawText(bt.tag, barX + 5f, y + 21f, tp); y += 28f }; else -> {} }; ii++ }; y += cardPad }
                is Spacer -> y += (items[ii] as Spacer).height
                is TextLine -> { canvas.drawText((items[ii] as TextLine).text, p, y + 32f, (items[ii] as TextLine).paint); y += lineH }
                is BarWithTag -> y += 28f; else -> {}
            }; ii++
        }
        val wp = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 28f; isFakeBoldText = true; textSkewX = -0.25f; shader = LinearGradient(0f, 0f, 400f, 0f, intArrayOf(Color.parseColor("#8B5CF6")!!, Color.parseColor("#F59E0B")!!, Color.parseColor("#EC4899")!!), null, Shader.TileMode.CLAMP) }
        val tw = wp.measureText("海特洛档案室"); val iconSize = 44f; val totalW = iconSize + 8f + tw; val startX = w - p - totalW
        val iconBm = try { val res = ctx.resources; val id = res.getIdentifier("ic_launcher", "mipmap", ctx.packageName); val b = android.graphics.BitmapFactory.decodeResource(res, id); if (b != null) android.graphics.Bitmap.createScaledBitmap(b, iconSize.toInt(), iconSize.toInt(), true) else null } catch (_: Exception) { null }
        if (iconBm != null) { val cp = android.graphics.Path().apply { addRoundRect(0f, 0f, iconSize, iconSize, 10f, 10f, android.graphics.Path.Direction.CW) }; canvas.save(); canvas.translate(startX, y); canvas.clipPath(cp); canvas.drawBitmap(iconBm, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG)); canvas.restore() }
        canvas.drawText("海特洛档案室", startX + iconSize + 8f, y + 32f, wp); saveAndShare(ctx, bitmap, fileName)
    }

    private fun saveAndShare(ctx: Context, bitmap: Bitmap, fileName: String) {
        val dir = File(ctx.cacheDir, "share"); dir.mkdirs(); val file = File(dir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "分享到"))
    }

    private val CHAR_NAMES = mapOf("1004" to "安魂曲", "1003" to "早雾", "1010" to "娜娜莉", "1039" to "法帝娅", "1051" to "零(女)", "1052" to "浔", "1055" to "九原", "1033" to "阿德勒", "1054" to "达芙蒂尔", "1071" to "卡厄斯", "1019" to "薄荷", "1020" to "哈尼娅", "1008" to "翳", "1021" to "埃德嘉", "1070" to "海月", "1073" to "小吱")
    private val FORK_NAMES = mapOf("fork_rose" to "最后一朵玫瑰", "fork_butterfly" to "斑蝶", "fork_blackbook" to "黑书", "fork_jingmotingyuan" to "镜默庭院", "fork_mofeikesi" to "好狗狗走四方", "fork_arachne" to "永恒圆舞曲", "fork_whale" to "鲸之歌", "fork_rishi" to "休息日", "fork_wuhuakuang" to "被遗忘者", "fork_yuren" to "勿忘伞", "fork_paperplane" to "开始净空")
}