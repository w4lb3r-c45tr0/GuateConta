package com.example.guateconta.domain

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.guateconta.data.AdminSQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GeneradorPdf {

    fun generarYAbrirReporte(context: Context, nombreUsuario: String, transacciones: List<AdminSQLiteOpenHelper.Transaccion>, balanceTotal: Double) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Tamaño A4 Estándar
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()

        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // --- ENCABEZADO ---
        titlePaint.color = Color.parseColor("#2E7D32")
        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        canvas.drawText("GUATECONTA - REPORTE CONTABLE FISCAL", 40f, 50f, titlePaint)

        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText("Contribuyente / Empresa: $nombreUsuario", 40f, 80f, paint)
        canvas.drawText("Fecha de Emisión: $fechaActual", 40f, 98f, paint)
        canvas.drawText("Régimen Fiscal: General IVA (12% Guatemala)", 40f, 116f, paint)

        // Línea divisora
        paint.strokeWidth = 2f
        canvas.drawLine(40f, 130f, 555f, 130f, paint)

        // --- TABLA DE ASIENTOS CONTABLES ---
        var y = 160f
        paint.isFakeBoldText = true
        paint.textSize = 10f

        canvas.drawText("Descripción / Cuenta Contable", 40f, y, paint)
        canvas.drawText("Tipo", 280f, y, paint)
        canvas.drawText("Base Imponible", 340f, y, paint)
        canvas.drawText("IVA (12%)", 430f, y, paint)
        canvas.drawText("Monto Total", 500f, y, paint)

        paint.isFakeBoldText = false
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 20f

        var totalBase = 0.0
        var totalIva = 0.0

        transacciones.forEach { item ->
            if (y > 750f) return@forEach // Límite simple de página

            totalBase += item.baseImponible
            totalIva += item.impuesto

            val descCorta = if (item.descripcion.length > 25) item.descripcion.substring(0, 22) + "..." else item.descripcion

            canvas.drawText(descCorta, 40f, y, paint)
            canvas.drawText(item.tipo, 280f, y, paint)
            canvas.drawText("Q.${String.format("%.2f", item.baseImponible)}", 340f, y, paint)
            canvas.drawText("Q.${String.format("%.2f", item.impuesto)}", 430f, y, paint)
            canvas.drawText("Q.${String.format("%.2f", item.monto)}", 500f, y, paint)

            y += 15f
            paint.color = Color.GRAY
            paint.textSize = 8f
            canvas.drawText(item.categoria, 40f, y, paint)

            paint.color = Color.BLACK
            paint.textSize = 10f
            y += 20f
        }

        // --- RESUMEN DE CIERRE ---
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 25f

        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN GENERAL FISCAL:", 40f, y, paint)
        y += 18f
        paint.isFakeBoldText = false
        canvas.drawText("• Total Base Imponible Generada: Q.${String.format("%.2f", totalBase)}", 50f, y, paint)
        y += 16f
        canvas.drawText("• Total IVA Débito/Crédito Fiscal: Q.${String.format("%.2f", totalIva)}", 50f, y, paint)
        y += 16f
        paint.isFakeBoldText = true
        canvas.drawText("• Balance Neto Registrado: Q.${String.format("%.2f", balanceTotal)}", 50f, y, paint)

        // Pie de página
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("Documento generado automáticamente por GuateConta v1.0", 40f, 810f, paint)

        pdfDocument.finishPage(page)

        // Guardar archivo temporal en cache y abrirlo
        val file = File(context.cacheDir, "Reporte_Contable_GuateConta.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}