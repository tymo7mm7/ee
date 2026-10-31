package com.tymo.kalendarz

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

object Export {
    private const val W = 842f   // A4 landscape
    private const val H = 595f
    private const val M = 28f
    private val hm = DateTimeFormatter.ofPattern("HH:mm")

    fun share(context: Context, file: File, mime: String, subject: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, subject))
    }

    private fun outFile(context: Context, name: String) =
        File(File(context.cacheDir, "export").apply { mkdirs() }, name)

    fun monthTitle(m: YearMonth) =
        m.month.getDisplayName(TextStyle.FULL_STANDALONE, PL).replaceFirstChar { it.uppercase() } + " " + m.year

    fun weekTitle(start: LocalDate): String {
        val end = start.plusDays(6)
        val f = DateTimeFormatter.ofPattern("d MMM", PL)
        return "Tydzień ${start.format(f)} – ${end.format(f)} ${end.year}"
    }

    private fun paint(size: Float, bold: Boolean = false, color: Int = Color.rgb(32, 33, 36)) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
        this.color = color
    }

    private fun fit(p: Paint, text: String, width: Float): String {
        val n = p.breakText(text, true, width, null)
        return if (n >= text.length) text else text.take((n - 1).coerceAtLeast(0)) + "…"
    }

    private fun Canvas.dot(x: Float, y: Float, argb: Long) {
        drawCircle(x, y, 3f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = argb.toInt() })
    }

    private fun label(o: Occurrence) =
        if (o.event.allDay || o.multiDay) o.event.title else "${o.start.format(hm)} ${o.event.title}"

    fun monthPdf(context: Context, data: AppData, month: YearMonth): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), 1).create())
        val c = page.canvas
        val title = paint(20f, true)
        val head = paint(10f, true, Color.rgb(95, 99, 104))
        val num = paint(10f, true)
        val small = paint(8f)
        val grid = Paint().apply { color = Color.rgb(218, 220, 224); style = Paint.Style.STROKE; strokeWidth = 0.7f }

        c.drawText(monthTitle(month), M, M + 16, title)
        val first = weekStart(month.atDay(1))
        val last = weekStart(month.atEndOfMonth()).plusDays(6)
        val weeks = ((last.toEpochDay() - first.toEpochDay() + 1) / 7).toInt()
        val occ = occurrences(data.events, first, last, data.settings.holidays)

        val top = M + 44
        val cw = (W - 2 * M) / 7
        val ch = (H - top - M) / weeks
        for (i in 0 until 7) {
            val name = java.time.DayOfWeek.of(i + 1).getDisplayName(TextStyle.SHORT_STANDALONE, PL).uppercase()
            c.drawText(name, M + i * cw + 4, top - 6, head)
        }
        for (w in 0 until weeks) for (d in 0 until 7) {
            val day = first.plusDays((w * 7 + d).toLong())
            val x = M + d * cw
            val y = top + w * ch
            c.drawRect(RectF(x, y, x + cw, y + ch), grid)
            val dayOcc = occ.filter { it.coversDay(day) }
            val holiday = dayOcc.any { it.isHoliday }
            num.color = when {
                day.month != month.month -> Color.rgb(170, 170, 170)
                holiday || d == 6 -> Color.rgb(217, 48, 37)
                else -> Color.rgb(32, 33, 36)
            }
            c.drawText("${day.dayOfMonth}", x + 4, y + 12, num)
            val maxLines = ((ch - 18) / 10).toInt()
            dayOcc.take(if (dayOcc.size > maxLines) maxLines - 1 else maxLines).forEachIndexed { i, o ->
                val ly = y + 24 + i * 10
                c.dot(x + 7, ly - 3, calColors[o.event.color].argb)
                c.drawText(fit(small, label(o), cw - 16), x + 13, ly, small)
            }
            if (dayOcc.size > maxLines) c.drawText("+${dayOcc.size - maxLines + 1} więcej", x + 13, y + 24 + (maxLines - 1) * 10, small)
        }
        doc.finishPage(page)
        return write(doc, outFile(context, "Kalendarz ${month}.pdf"))
    }

    fun weekPdf(context: Context, data: AppData, start: LocalDate): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(W.toInt(), H.toInt(), 1).create())
        val c = page.canvas
        val title = paint(18f, true)
        val head = paint(10f, true)
        val time = paint(8f, true, Color.rgb(95, 99, 104))
        val text = paint(9f)
        val grid = Paint().apply { color = Color.rgb(218, 220, 224); style = Paint.Style.STROKE; strokeWidth = 0.7f }
        val today = LocalDate.now()

        c.drawText(weekTitle(start), M, M + 16, title)
        val top = M + 36
        val cw = (W - 2 * M) / 7
        val occ = occurrences(data.events, start, start.plusDays(6), data.settings.holidays)
        for (d in 0 until 7) {
            val day = start.plusDays(d.toLong())
            val x = M + d * cw
            c.drawRect(RectF(x, top, x + cw, H - M), grid)
            val dayOcc = occ.filter { it.coversDay(day) }
            head.color = if (dayOcc.any { it.isHoliday } || d == 6) Color.rgb(217, 48, 37) else if (day == today) Color.rgb(26, 115, 232) else Color.rgb(32, 33, 36)
            val name = day.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, PL) + " " + day.format(DateTimeFormatter.ofPattern("d.MM"))
            c.drawText(name, x + 6, top + 14, head)
            c.drawLine(x, top + 20, x + cw, top + 20, grid)
            var y = top + 32
            for (o in dayOcc) {
                if (y > H - M - 20) { c.drawText("…", x + 6, y, text); break }
                val bar = Paint().apply { color = calColors[o.event.color].argb.toInt() }
                val t = when {
                    o.event.allDay -> "Cały dzień"
                    o.multiDay -> "${o.start.format(hm)} → ${o.end.format(DateTimeFormatter.ofPattern("d.MM HH:mm"))}"
                    else -> "${o.start.format(hm)}–${o.end.format(hm)}"
                }
                val lines = wrap(text, o.event.title, cw - 18).take(3)
                val extra = if (o.event.location.isNotBlank()) 1 else 0
                val h = 11f + lines.size * 11f + extra * 10f
                c.drawRect(RectF(x + 5, y - 8, x + 7.5f, y - 8 + h), bar)
                c.drawText(t, x + 11, y, time)
                lines.forEachIndexed { i, l -> c.drawText(l, x + 11, y + 11 + i * 11, text) }
                if (extra == 1) c.drawText(fit(time, "• " + o.event.location, cw - 18), x + 11, y + 11 + lines.size * 11, time)
                y += h + 8
            }
        }
        doc.finishPage(page)
        return write(doc, outFile(context, "Kalendarz tydzien ${start}.pdf"))
    }

    private fun wrap(p: Paint, text: String, width: Float): List<String> {
        val out = mutableListOf<String>()
        var line = ""
        text.split(" ").forEach { w ->
            val cand = if (line.isEmpty()) w else "$line $w"
            if (p.measureText(cand) > width && line.isNotEmpty()) { out += line; line = w } else line = cand
        }
        if (line.isNotEmpty()) out += line
        return out.map { fit(p, it, width) }
    }

    private fun write(doc: PdfDocument, file: File): File {
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ---- iCalendar (.ics), importable into Google Calendar, Outlook, Apple Calendar ----

    private fun esc(s: String) = s.replace("\\", "\\\\").replace(";", "\;").replace(",", "\\,").replace("\n", "\\n")
    private val icsDate = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val icsDateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")

    fun ics(context: Context, data: AppData, from: LocalDate, to: LocalDate, name: String): File {
        val stamp = LocalDateTime.now().format(icsDateTime)
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Moj Kalendarz//PL\r\nCALSCALE:GREGORIAN\r\n")
        occurrences(data.events, from, to, holidays = false).forEach { o ->
            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:${o.event.id}-${o.start.format(icsDateTime)}@moj-kalendarz\r\n")
            sb.append("DTSTAMP:$stamp\r\n")
            if (o.event.allDay) {
                sb.append("DTSTART;VALUE=DATE:${o.firstDay.format(icsDate)}\r\n")
                sb.append("DTEND;VALUE=DATE:${o.lastDay.plusDays(1).format(icsDate)}\r\n")
            } else {
                sb.append("DTSTART:${o.start.format(icsDateTime)}\r\n")
                sb.append("DTEND:${o.end.format(icsDateTime)}\r\n")
            }
            sb.append("SUMMARY:${esc(o.event.title)}\r\n")
            if (o.event.location.isNotBlank()) sb.append("LOCATION:${esc(o.event.location)}\r\n")
            if (o.event.description.isNotBlank()) sb.append("DESCRIPTION:${esc(o.event.description)}\r\n")
            o.event.reminder?.let {
                sb.append("BEGIN:VALARM\r\nACTION:DISPLAY\r\nDESCRIPTION:${esc(o.event.title)}\r\nTRIGGER:-PT${it}M\r\nEND:VALARM\r\n")
            }
            sb.append("END:VEVENT\r\n")
        }
        sb.append("END:VCALENDAR\r\n")
        val file = outFile(context, "$name.ics")
        file.writeText(sb.toString())
        return file
    }
}
