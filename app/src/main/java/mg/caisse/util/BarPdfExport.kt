package mg.caisse.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import mg.caisse.data.local.dao.bar.BarProductTotal
import java.io.File

fun createBarReportPdf(
    context: Context,
    barName: String,
    period: String,
    revenue: Long,
    purchases: Long,
    expenses: Long,
    balance: Long,
    stockValue: Long,
    lossQuantity: Long,
    topProducts: List<BarProductTotal>
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

    canvas.drawText("BAR — Rapport", left, y, titlePaint)
    y += 20f
    canvas.drawText(barName, left, y, labelPaint)
    y += 18f
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

    fun line(label: String, value: String, bold: Boolean = false) {
        newPageIfNeeded()
        val paint = if (bold) boldPaint else labelPaint
        canvas.drawText(label, left, y, paint)
        canvas.drawText(value, right, y, amountPaint)
        y += 20f
    }

    line("Recettes", formatAriary(revenue))
    line("Achats", formatAriary(purchases))
    line("Dépenses", formatAriary(expenses))
    line("Solde caisse", formatAriary(balance), bold = true)
    line("Valeur du stock", formatAriary(stockValue))
    line("Pertes (unités)", lossQuantity.toString())
    y += 14f

    if (topProducts.isNotEmpty()) {
        newPageIfNeeded()
        canvas.drawText("Top produits", left, y, boldPaint)
        y += 20f
        topProducts.forEach { product ->
            line("${product.name} × ${product.quantity}", formatAriary(product.total))
        }
    }

    doc.finishPage(page)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "rapport-bar.pdf")
    file.outputStream().use { doc.writeTo(it) }
    doc.close()
    return file
}
