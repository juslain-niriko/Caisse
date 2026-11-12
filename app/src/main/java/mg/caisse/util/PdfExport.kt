package mg.caisse.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

data class ReportLine(val label: String, val amount: Long)

/** Génère un PDF de rapport dans cacheDir/exports et retourne le fichier. */
fun createReportPdf(
    context: Context,
    title: String,
    period: String,
    totalIn: Long,
    totalOut: Long,
    balance: Long,
    byCategory: List<ReportLine>,
    byStudent: List<ReportLine>
): File {
    val doc = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    var pageNumber = 1

    var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas

    val titlePaint = Paint().apply {
        textSize = 22f
        isFakeBoldText = true
        color = 0xFF0F766E.toInt()
    }
    val labelPaint = Paint().apply {
        textSize = 12f
        color = 0xFF111827.toInt()
    }
    val boldPaint = Paint().apply {
        textSize = 13f
        isFakeBoldText = true
        color = 0xFF111827.toInt()
    }
    val amountPaint = Paint().apply {
        textSize = 12f
        color = 0xFF111827.toInt()
        textAlign = Paint.Align.RIGHT
    }

    val left = 40f
    val right = (pageWidth - 40).toFloat()
    var y = 54f

    canvas.drawText(title, left, y, titlePaint)
    y += 22f
    canvas.drawText(period, left, y, labelPaint)
    y += 34f

    fun newPageIfNeeded() {
        if (y > pageHeight - 70) {
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = 54f
        }
    }

    fun line(label: String, amount: Long, bold: Boolean = false) {
        newPageIfNeeded()
        val paint = if (bold) boldPaint else labelPaint
        canvas.drawText(label, left, y, paint)
        canvas.drawText(formatAriary(amount), right, y, amountPaint)
        y += 20f
    }

    line("Total entrées", totalIn)
    line("Total sorties", totalOut)
    line("Solde", balance, bold = true)
    y += 14f

    if (byCategory.isNotEmpty()) {
        newPageIfNeeded()
        canvas.drawText("Sorties par catégorie", left, y, boldPaint)
        y += 20f
        byCategory.forEach { line(it.label, it.amount) }
        y += 14f
    }

    if (byStudent.isNotEmpty()) {
        newPageIfNeeded()
        canvas.drawText("Écolage par élève", left, y, boldPaint)
        y += 20f
        byStudent.forEach { line(it.label, it.amount) }
    }

    doc.finishPage(page)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "rapport.pdf")
    file.outputStream().use { doc.writeTo(it) }
    doc.close()
    return file
}

fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Rapport Caisse")
        clipData = android.content.ClipData.newRawUri("rapport.pdf", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Partager le rapport"))
}
