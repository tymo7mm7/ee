package com.tymo.scenariusz

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

/** Renders the screenplay in a classic layout (A4, monospace) and opens the share sheet. */
object PdfExport {
    private const val W = 595
    private const val H = 842
    private const val TOP = 64f
    private const val BOTTOM = 790f
    private const val LEFT = 72f
    private const val LINE = 14f

    fun share(context: Context, project: Project) {
        val file = render(context, project)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, project.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Udostępnij scenariusz (PDF)"))
    }

    private fun render(context: Context, project: Project): File {
        val doc = PdfDocument()
        val mono = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        val monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; typeface = mono }
        val charW = paint.measureText("M")

        var pageNo = 0
        var page: PdfDocument.Page? = null
        var y = TOP

        fun newPage() {
            page?.let { doc.finishPage(it) }
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            y = TOP
            if (pageNo > 1) {
                paint.typeface = mono
                page!!.canvas.drawText("$pageNo.", W - LEFT - charW * 3, 40f, paint)
            }
        }

        fun wrap(text: String, maxChars: Int): List<String> = text.split("\n").flatMap { para ->
            val out = mutableListOf<String>()
            var line = StringBuilder()
            para.split(" ").forEach { word ->
                if (line.isNotEmpty() && line.length + 1 + word.length > maxChars) {
                    out += line.toString(); line = StringBuilder()
                }
                if (line.isNotEmpty()) line.append(' ')
                line.append(word)
            }
            out += line.toString()
            out
        }

        fun write(text: String, x: Float, widthChars: Int, bold: Boolean = false, gapBefore: Float = 0f, center: Boolean = false) {
            paint.typeface = if (bold) monoBold else mono
            val lines = wrap(text, widthChars)
            y += gapBefore
            if (y + LINE > BOTTOM) newPage()
            lines.forEach { l ->
                if (y + LINE > BOTTOM) newPage()
                val lx = if (center) (W - paint.measureText(l)) / 2 else x
                page!!.canvas.drawText(l, lx, y, paint)
                y += LINE
            }
        }

        val fullWidth = ((W - 2 * LEFT) / charW).toInt()
        newPage()
        project.scriptParts().forEach { p ->
            when (p) {
                is ScriptPart.Title -> { y = 300f; paint.textSize = 18f; write(p.text, LEFT, 40, bold = true, center = true); paint.textSize = 11f }
                is ScriptPart.Info -> write(p.text, LEFT, fullWidth - 10, gapBefore = 8f, center = true)
                is ScriptPart.Heading -> {
                    if (p.number == 1) newPage()
                    write("${p.number}. ${p.text}", LEFT, fullWidth, bold = true, gapBefore = LINE)
                }
                is ScriptPart.Action -> write(p.text, LEFT, fullWidth, gapBefore = LINE * 0.6f)
                is ScriptPart.Speaker -> write(p.name, LEFT + charW * 20, 30, bold = true, gapBefore = LINE * 0.6f)
                is ScriptPart.Paren -> write(p.text, LEFT + charW * 15, 25)
                is ScriptPart.Dialog -> write(p.text, LEFT + charW * 10, 35)
            }
        }
        page?.let { doc.finishPage(it) }

        val dir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val safe = project.title.ifBlank { "scenariusz" }.replace(Regex("[^\\p{L}\\p{N} _-]"), "").trim().ifBlank { "scenariusz" }
        val file = File(dir, "$safe.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
