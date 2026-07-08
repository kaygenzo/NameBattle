package com.telen.namebattle.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.telen.namebattle.R
import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.Session
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BattleReportPdfGenerator(private val context: Context) {

    private val pageW = 595f
    private val pageH = 842f
    private val margin = 28f

    private val blush = Color.parseColor("#F4D8D8")
    private val cream = Color.parseColor("#F6EFE4")
    private val finalBg = Color.parseColor("#FBEFEA")
    private val sage = Color.parseColor("#8FAE94")
    private val brown = Color.parseColor("#5A4A42")
    private val brownLight = Color.parseColor("#8A7A72")
    private val terracotta = Color.parseColor("#B5836B")

    private val serif = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    private val serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    private val serifItalic = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    private val sansBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

    private lateinit var document: PdfDocument
    private lateinit var session: Session
    private var page: PdfDocument.Page? = null
    private lateinit var canvas: Canvas
    private var y = 0f
    private var pageNumber = 0

    fun generate(session: Session, battle: BattleState, namesById: Map<Long, String>): File {
        this.document = PdfDocument()
        this.session = session
        this.pageNumber = 0

        startNewPage(withMainTitle = true)
        battle.rounds.forEachIndexed { index, round ->
            drawRoundCard(index + 1, round, namesById)
        }
        drawFinalistsCard(battle.finalists.mapNotNull { namesById[it] })
        finishCurrentPage()

        val dir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val file = File(dir, "prenombattle_${session.id}_$stamp.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun startNewPage(withMainTitle: Boolean) {
        finishCurrentPage()
        pageNumber++
        val info = PdfDocument.PageInfo.Builder(pageW.toInt(), pageH.toInt(), pageNumber).create()
        val newPage = document.startPage(info)
        page = newPage
        canvas = newPage.canvas

        canvas.drawRect(0f, 0f, pageW, pageH, Paint().apply { color = blush })
        canvas.drawRoundRect(
            RectF(margin, margin, pageW - margin, pageH - margin), 18f, 18f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        )

        y = margin + 46f
        if (withMainTitle) drawMainHeader() else drawContinuationHeader()
    }

    private fun drawMainHeader() {
        val p1 = session.parent1.name
        val p2 = session.parent2?.name
        val sep = context.getString(R.string.pdf_heart_separator)
        val names = if (p2 != null) "$p1 $sep $p2" else p1

        canvas.drawText(
            context.getString(R.string.pdf_header_tagline), pageW / 2f, y,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = sage; typeface = serifItalic; textSize = 13f; textAlign = Paint.Align.CENTER
            })
        y += 22f
        canvas.drawText(
            context.getString(R.string.pdf_header_title), pageW / 2f, y,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = brown; typeface = serifBold; textSize = 22f; textAlign = Paint.Align.CENTER
            })
        y += 20f
        canvas.drawText(
            names, pageW / 2f, y,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = brownLight; typeface = serifItalic; textSize = 10f; textAlign =
                Paint.Align.CENTER
            })
        y += 30f
    }

    private fun drawContinuationHeader() {
        canvas.drawText(
            context.getString(R.string.pdf_header_title_continued), pageW / 2f, y,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = brown; typeface = serifBold; textSize = 16f; textAlign = Paint.Align.CENTER
            })
        y += 26f
    }

    private val roundEmojis = listOf("🌸", "🌿", "🍼")

    private fun drawRoundCard(roundNumber: Int, round: BattleRound, namesById: Map<Long, String>) {
        val lineH = 22f
        val headerH = 26f
        val cardH = headerH + round.duels.size * lineH + 10f

        if (y + cardH + 16f > pageH - margin - 40f) startNewPage(withMainTitle = false)

        val left = margin + 24f
        val right = pageW - margin - 24f
        canvas.drawRoundRect(
            RectF(left, y, right, y + cardH), 10f, 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cream })

        val emoji = roundEmojis[(roundNumber - 1) % roundEmojis.size]
        canvas.drawText(
            "$emoji  ${context.getString(R.string.pdf_round_label, roundNumber)}",
            left + 12f,
            y + 17f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = terracotta; typeface = sansBold; textSize = 11f
            })

        var ry = y + headerH + 10f
        round.duels.forEach { duel ->
            val n1 = namesById[duel.firstName1Id] ?: "?"
            val n2 = duel.firstName2Id?.let { namesById[it] }
            val winner = duel.winnerId?.let { namesById[it] } ?: "?"
            val dot = context.getString(R.string.pdf_dot_separator)
            val label =
                if (n2 != null) {
                    "$n1  $dot  $n2"
                } else {
                    "$n1 ${context.getString(R.string.pdf_auto_qualified)}"
                }
            canvas.drawText(
                label, left + 16f, ry,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = brown; typeface = serif; textSize = 10.5f
                })
            canvas.drawText(
                context.getString(R.string.pdf_winner_chosen, winner), right - 12f, ry,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = sage; typeface = serifItalic; textSize = 9.5f; textAlign =
                    Paint.Align.RIGHT
                })
            ry += lineH
        }
        y += cardH + 14f
    }

    private fun drawFinalistsCard(finalistNames: List<String>) {
        if (y + 38f > pageH - margin - 40f) startNewPage(withMainTitle = false)
        val left = margin + 24f
        val right = pageW - margin - 24f
        canvas.drawRoundRect(
            RectF(left, y, right, y + 28f), 10f, 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = finalBg })
        val deco = context.getString(R.string.pdf_finalist_decoration)
        val dotSep = context.getString(R.string.pdf_dot_separator)
        canvas.drawText(
            "$deco " + finalistNames.joinToString("  $dotSep  ") + " $deco",
            pageW / 2f, y + 18f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = terracotta; typeface = serifBold; textSize = 13f; textAlign =
                Paint.Align.CENTER
            })
        y += 38f
    }

    private fun finishCurrentPage() {
        val p = page ?: return
        canvas.drawText(
            context.getString(R.string.pdf_footer_label), margin + 12f, pageH - margin - 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#999999"); textSize = 7.5f
            })
        canvas.drawText(
            context.getString(R.string.pdf_page_number, pageNumber),
            pageW - margin - 12f,
            pageH - margin - 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#999999"); textSize = 7.5f; textAlign = Paint.Align.RIGHT
            })
        document.finishPage(p)
        page = null
    }
}
