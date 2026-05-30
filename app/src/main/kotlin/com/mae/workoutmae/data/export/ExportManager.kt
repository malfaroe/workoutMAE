package com.mae.workoutmae.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import com.mae.workoutmae.data.db.entity.Sesion
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

object ExportManager {

    // ── CSV ───────────────────────────────────────────────────────────────────

    fun sesionesACsv(sesiones: List<Sesion>): String {
        val header = "fecha,tipo,horaInicio,horaFin," +
                "dolorDurante,dolorPost,descripcionDolor," +
                "slantSeries,slantSensacion,stepupReps,spanishSeries,spanishTempo," +
                "wallsitSeg,bulgSeries,bulgTensionDorsal,calfSeries,flutterSeries,pushups," +
                "escaladaDuracion,escaladaTipo,escaladaGradoMax,escaladaDolorRodilla,escaladaDolorTendon," +
                "energiaGeneral,calidadSuenio,tensionDorsal,pinchazoRotula," +
                "colageno,vitaminaC,creatina,notas"
        val rows = sesiones.sortedByDescending { it.fecha }.joinToString("\n") { s ->
            listOf(
                s.fecha, s.tipo, s.horaInicio ?: "", s.horaFin ?: "",
                s.dolorDurante, s.dolorPost ?: "", csv(s.descripcionDolor),
                s.slantSeries ?: "", csv(s.slantSensacion), s.stepupReps ?: "",
                s.spanishSeries ?: "", boolCsv(s.spanishTempo),
                s.wallsitSegundos ?: "", s.bulgSeries ?: "", csv(s.bulgTensionDorsal),
                s.calfSeries ?: "", s.flutterSeries ?: "", s.pushups ?: "",
                s.escaladaDuracion ?: "", csv(s.escaladaTipo), csv(s.escaladaGradoMax),
                s.escaladaDolorRodilla ?: "", s.escaladaDolorTendon ?: "",
                s.energiaGeneral ?: "", s.calidadSuenio ?: "",
                s.tensionDorsal ?: "", s.pinchazoRotula ?: "",
                bool(s.colageno), bool(s.vitaminaC), bool(s.creatina),
                csv(s.notas),
            ).joinToString(",")
        }
        return "$header\n$rows"
    }

    fun medidasACsv(medidas: List<MedidaCorporal>): String {
        val header = "fecha,peso,musloDerechoCm,musloIzquierdoCm,asimetriaCm,pantorrillaDerechaCm,pantorrillaIzquierdaCm,notas"
        val rows = medidas.sortedByDescending { it.fecha }.joinToString("\n") { m ->
            listOf(
                m.fecha,
                m.peso?.let { "%.1f".format(it) } ?: "",
                m.musloDerechoCm?.let { "%.1f".format(it) } ?: "",
                m.musloIzquierdoCm?.let { "%.1f".format(it) } ?: "",
                m.asimetriaCm?.let { "%.1f".format(it) } ?: "",
                m.pantorrillaDerechaCm?.let { "%.1f".format(it) } ?: "",
                m.pantorrillaIzquierdaCm?.let { "%.1f".format(it) } ?: "",
                csv(m.notas),
            ).joinToString(",")
        }
        return "$header\n$rows"
    }

    fun compartirCsv(context: Context, contenido: String, nombreArchivo: String) {
        val file = archivoExport(context, nombreArchivo)
        file.writeText(contenido, Charsets.UTF_8)
        compartir(context, file, "text/csv")
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    fun compartirPdf(
        context: Context,
        sesiones: List<Sesion>,
        medidas: List<MedidaCorporal>,
        nombrePaciente: String,
        fechaInicio: String,
    ) {
        val nombre = "workoutmae_reporte_${LocalDate.now()}.pdf"
        val file = archivoExport(context, nombre)
        generarPdf(sesiones, medidas, nombrePaciente, fechaInicio, file)
        compartir(context, file, "application/pdf")
    }

    private fun generarPdf(
        sesiones: List<Sesion>,
        medidas: List<MedidaCorporal>,
        nombrePaciente: String,
        fechaInicio: String,
        file: File,
    ) {
        val pageW = 595
        val pageH = 842
        val mg = 40f

        val doc = PdfDocument()
        var pageNum = 1

        fun newPage(): Pair<PdfDocument.Page, android.graphics.Canvas> {
            val pi = PdfDocument.PageInfo.Builder(pageW, pageH, pageNum++).create()
            val p = doc.startPage(pi)
            return p to p.canvas
        }

        // Paints
        val pTitle = paint(16f, Color.parseColor("#1B5E20"), bold = true)
        val pSec   = paint(11f, Color.parseColor("#2E7D32"), bold = true)
        val pNorm  = paint(9f,  Color.BLACK)
        val pSmall = paint(8f,  Color.DKGRAY)
        val pHead  = paint(8f,  Color.DKGRAY, bold = true)
        val pLine  = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }

        val sesSorted = sesiones.sortedByDescending { it.fecha }
        val medSorted = medidas.sortedByDescending { it.fecha }

        var (page, cv) = newPage()
        var y = 0f

        fun checkPage(need: Float) {
            if (y + need > pageH - mg) {
                doc.finishPage(page)
                val next = newPage()
                page = next.first
                cv = next.second
                y = mg
            }
        }

        // ── Page header ────────────────────────────────────────────────
        y = 48f
        cv.drawText("WorkoutMAE  —  Reporte de Progreso", mg, y, pTitle)
        y += 16f
        cv.drawText(
            "Paciente: ${nombrePaciente.ifBlank { "—" }}    Inicio: ${fechaInicio.ifBlank { "—" }}    Generado: ${LocalDate.now()}",
            mg, y, pSmall,
        )
        y += 8f
        cv.drawLine(mg, y, pageW - mg, y, pLine)
        y += 18f

        // ── Resumen ────────────────────────────────────────────────────
        cv.drawText("RESUMEN", mg, y, pSec)
        y += 14f
        val mejorStep  = sesiones.mapNotNull { it.stepupReps }.maxOrNull()
        val mejorWall  = sesiones.mapNotNull { it.wallsitSegundos }.maxOrNull()
        val adherencia = if (sesiones.isEmpty()) 0 else sesiones.count { it.colageno } * 100 / sesiones.size
        val ultimaAsim = medidas.firstOrNull()?.asimetriaCm

        cv.drawText("Total sesiones: ${sesiones.size}", mg, y, pNorm)
        cv.drawText("Mejor step-up: ${mejorStep ?: "—"} reps", mg + 160f, y, pNorm)
        cv.drawText("Mejor wall-sit: ${mejorWall ?: "—"} seg", mg + 330f, y, pNorm)
        y += 13f
        cv.drawText("Adherencia colageno: $adherencia%", mg, y, pNorm)
        ultimaAsim?.let {
            cv.drawText("Ultima asimetria muslo: ${"%.1f".format(it)} cm", mg + 200f, y, pNorm)
        }
        y += 10f
        cv.drawLine(mg, y, pageW - mg, y, pLine)
        y += 18f

        // ── Sesiones ───────────────────────────────────────────────────
        cv.drawText("SESIONES (${sesSorted.size})", mg, y, pSec)
        y += 14f

        // 7 columns: fecha, tipo, dolor-dur, dolor-post, step-ups, wallsit, supl
        val colW = (pageW - mg * 2) / 7f
        fun colX(i: Int) = mg + i * colW
        val sesHdrs = listOf("Fecha", "Tipo", "Dolor dur.", "Dolor post", "Step-ups", "Wallsit(s)", "Suplementos")
        sesHdrs.forEachIndexed { i, h -> cv.drawText(h, colX(i), y, pHead) }
        y += 5f; cv.drawLine(mg, y, pageW - mg, y, pLine); y += 10f

        sesSorted.forEach { s ->
            checkPage(12f)
            cv.drawText(s.fecha, colX(0), y, pSmall)
            cv.drawText(s.tipo, colX(1), y, pSmall)
            cv.drawText(s.dolorDurante.toString(), colX(2), y, pSmall)
            cv.drawText(s.dolorPost?.toString() ?: "-", colX(3), y, pSmall)
            cv.drawText(s.stepupReps?.toString() ?: "-", colX(4), y, pSmall)
            cv.drawText(s.wallsitSegundos?.toString() ?: "-", colX(5), y, pSmall)
            val supl = buildString {
                if (s.colageno) append("Col ")
                if (s.vitaminaC) append("VitC ")
                if (s.creatina) append("Cre")
            }.ifBlank { "-" }
            cv.drawText(supl, colX(6), y, pSmall)
            y += 11f
        }

        y += 8f; cv.drawLine(mg, y, pageW - mg, y, pLine); y += 18f

        // ── Medidas ────────────────────────────────────────────────────
        checkPage(60f)
        cv.drawText("MEDIDAS CORPORALES (${medSorted.size})", mg, y, pSec)
        y += 14f

        val medHdrs = listOf("Fecha", "Peso(kg)", "Muslo D(cm)", "Muslo I(cm)", "Asim.(cm)", "Pantor D", "Pantor I")
        medHdrs.forEachIndexed { i, h -> cv.drawText(h, colX(i), y, pHead) }
        y += 5f; cv.drawLine(mg, y, pageW - mg, y, pLine); y += 10f

        medSorted.forEach { m ->
            checkPage(12f)
            cv.drawText(m.fecha, colX(0), y, pSmall)
            cv.drawText(m.peso?.let { "%.1f".format(it) } ?: "-", colX(1), y, pSmall)
            cv.drawText(m.musloDerechoCm?.let { "%.1f".format(it) } ?: "-", colX(2), y, pSmall)
            cv.drawText(m.musloIzquierdoCm?.let { "%.1f".format(it) } ?: "-", colX(3), y, pSmall)
            cv.drawText(m.asimetriaCm?.let { "%.1f".format(it) } ?: "-", colX(4), y, pSmall)
            cv.drawText(m.pantorrillaDerechaCm?.let { "%.1f".format(it) } ?: "-", colX(5), y, pSmall)
            cv.drawText(m.pantorrillaIzquierdaCm?.let { "%.1f".format(it) } ?: "-", colX(6), y, pSmall)
            y += 11f
        }

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun archivoExport(context: Context, nombre: String): File {
        val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
        return File(dir, nombre)
    }

    private fun compartir(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartir archivo").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun paint(size: Float, color: Int, bold: Boolean = false) = Paint().apply {
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
    }

    private fun csv(s: String?) = (s ?: "").replace(",", ";").replace("\n", " ")
    private fun bool(b: Boolean) = if (b) "1" else "0"
    private fun boolCsv(b: Boolean?) = if (b == null) "" else if (b) "1" else "0"
}
