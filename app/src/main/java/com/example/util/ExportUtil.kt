package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.Asset
import com.example.data.model.AssetCondition
import com.example.data.model.AssetStatus
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportUtil {

    private const val TAG = "ExportUtil"

    /**
     * Generates a genuine Microsoft Excel (.xlsx) OpenXML workbook file.
     * Includes bold header row, defined column widths, numeric cell types for amounts,
     * and full UTF-8 Turkish character preservation.
     */
    fun exportToXlsx(
        context: Context,
        assets: List<Asset>,
        filterDescription: String = "Tüm Demirbaşlar"
    ): Result<File> {
        return runCatching {
            val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
            val fileName = "Akyol_Inventory_Demirbas_Listesi_$dateStamp.xlsx"

            // Target file in cache or external documents
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outputFile = File(exportDir, fileName)

            FileOutputStream(outputFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // 1. [Content_Types].xml
                    writeZipEntry(zos, "[Content_Types].xml", getContentTypesXml())

                    // 2. _rels/.rels
                    writeZipEntry(zos, "_rels/.rels", getRootRelsXml())

                    // 3. xl/_rels/workbook.xml.rels
                    writeZipEntry(zos, "xl/_rels/workbook.xml.rels", getWorkbookRelsXml())

                    // 4. xl/workbook.xml
                    writeZipEntry(zos, "xl/workbook.xml", getWorkbookXml())

                    // 5. xl/styles.xml (Bold header style and number formatting)
                    writeZipEntry(zos, "xl/styles.xml", getStylesXml())

                    // 6. xl/worksheets/sheet1.xml (Actual data rows)
                    writeZipEntry(zos, "xl/worksheets/sheet1.xml", buildWorksheetXml(assets))
                }
            }

            Log.i(TAG, "Excel (.xlsx) generated successfully: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            outputFile
        }
    }

    /**
     * Generates a genuine native PDF report with branding, summary statistics,
     * and clean asset data table.
     */
    fun exportToPdf(
        context: Context,
        assets: List<Asset>,
        reportTitle: String = "Demirbaş Envanter Raporu",
        filterDescription: String = "Tüm Demirbaşlar"
    ): Result<File> {
        return runCatching {
            val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
            val fileName = "Akyol_Inventory_Rapor_$dateStamp.pdf"

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outputFile = File(exportDir, fileName)

            val pdfDocument = PdfDocument()

            // Standard A4 dimensions in PostScript points: 595 x 842 pt
            val pageWidth = 595
            val pageHeight = 842
            val margin = 36f

            val titlePaint = Paint().apply {
                color = Color.rgb(16, 25, 35) // NavyDark #101923
                textSize = 18f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(111, 159, 161) // AccentTeal #6F9FA1
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            val metaPaint = Paint().apply {
                color = Color.rgb(80, 95, 110)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(32, 45, 58) // Card surface #202D3A
                style = Paint.Style.FILL
            }

            val headerTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val cellTextPaint = Paint().apply {
                color = Color.rgb(24, 36, 48)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            val cellCodePaint = Paint().apply {
                color = Color.rgb(15, 118, 110)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }

            val altRowPaint = Paint().apply {
                color = Color.rgb(245, 247, 250)
                style = Paint.Style.FILL
            }

            val dividerPaint = Paint().apply {
                color = Color.rgb(220, 226, 232)
                strokeWidth = 0.5f
            }

            val rowsPerPage = 28
            val totalPages = maxOf(1, (assets.size + rowsPerPage - 1) / rowsPerPage)

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
                maximumFractionDigits = 0
            }
            val totalValue = assets.sumOf { it.purchasePrice }
            val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date())

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                var y = margin + 20f

                // Header on Page 1
                if (pageIndex == 0) {
                    canvas.drawText("AKYOL INVENTORY", margin, y, titlePaint)
                    y += 16f
                    canvas.drawText("Demirbaş Yönetim Sistemi — $reportTitle", margin, y, subtitlePaint)
                    y += 22f

                    // Metadata Box
                    val metaY = y
                    canvas.drawText("Rapor Tarihi: $formattedDate", margin, metaY, metaPaint)
                    canvas.drawText("Filtre: $filterDescription", margin + 200f, metaY, metaPaint)
                    y += 14f
                    canvas.drawText("Toplam Demirbaş: ${assets.size} Adet", margin, y, metaPaint)
                    canvas.drawText("Toplam Portföy Değeri: ${currencyFormatter.format(totalValue)}", margin + 200f, y, metaPaint)
                    y += 22f
                } else {
                    canvas.drawText("AKYOL INVENTORY — $reportTitle (Sayfa ${pageIndex + 1}/$totalPages)", margin, y, subtitlePaint)
                    y += 20f
                }

                // Table Column Layout
                val colX = floatArrayOf(
                    margin,             // Kod: 36
                    margin + 90f,       // Ad: 126
                    margin + 200f,      // Kategori: 236
                    margin + 290f,      // Marka/Model: 326
                    margin + 380f,      // Zimmetli: 416
                    margin + 465f       // Durum: 501
                )
                val tableWidth = pageWidth - (margin * 2)

                // Table Header
                canvas.drawRect(margin, y - 11f, margin + tableWidth, y + 5f, headerBgPaint)
                canvas.drawText("Demirbaş Kodu", colX[0] + 4f, y, headerTextPaint)
                canvas.drawText("Demirbaş Adı", colX[1] + 4f, y, headerTextPaint)
                canvas.drawText("Kategori", colX[2] + 4f, y, headerTextPaint)
                canvas.drawText("Marka / Model", colX[3] + 4f, y, headerTextPaint)
                canvas.drawText("Zimmetli Kişi", colX[4] + 4f, y, headerTextPaint)
                canvas.drawText("Durum", colX[5] + 4f, y, headerTextPaint)

                y += 16f

                // Table Rows
                val startIdx = pageIndex * rowsPerPage
                val endIdx = minOf(startIdx + rowsPerPage, assets.size)

                for (i in startIdx until endIdx) {
                    val asset = assets[i]
                    val isAlt = (i % 2 == 1)

                    if (isAlt) {
                        canvas.drawRect(margin, y - 10f, margin + tableWidth, y + 4f, altRowPaint)
                    }
                    canvas.drawLine(margin, y + 4f, margin + tableWidth, y + 4f, dividerPaint)

                    val code = truncateText(asset.assetCode, 16)
                    val name = truncateText(asset.assetName, 20)
                    val cat = truncateText(asset.category, 18)
                    val brandModel = truncateText("${asset.brand} ${asset.model}".trim(), 18)
                    val assigned = truncateText(asset.assignedUserName.ifBlank { "Boşta" }, 16)
                    val status = truncateText(AssetStatus.fromString(asset.status).labelTr, 10)

                    canvas.drawText(code, colX[0] + 4f, y, cellCodePaint)
                    canvas.drawText(name, colX[1] + 4f, y, cellTextPaint)
                    canvas.drawText(cat, colX[2] + 4f, y, cellTextPaint)
                    canvas.drawText(brandModel, colX[3] + 4f, y, cellTextPaint)
                    canvas.drawText(assigned, colX[4] + 4f, y, cellTextPaint)
                    canvas.drawText(status, colX[5] + 4f, y, cellTextPaint)

                    y += 15f
                }

                // Page Footer
                val footerY = pageHeight - margin + 10f
                canvas.drawLine(margin, footerY - 14f, margin + tableWidth, footerY - 14f, dividerPaint)
                canvas.drawText("AKYOL INVENTORY Enterprise Asset Management • Gizli & Kurumsal Kullanım", margin, footerY, metaPaint)
                val pageStr = "Sayfa ${pageIndex + 1} / $totalPages"
                canvas.drawText(pageStr, pageWidth - margin - 60f, footerY, metaPaint)

                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()

            Log.i(TAG, "PDF generated successfully: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            outputFile
        }
    }

    /**
     * Shares or opens the exported file using the system chooser with proper FileProvider grant permissions.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    // --- ECMA-376 OPENXML XLSX BUILDER HELPERS ---

    private fun writeZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        zos.write(bytes)
        zos.closeEntry()
    }

    private fun getContentTypesXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun getRootRelsXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun getWorkbookRelsXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun getWorkbookXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Demirbaşlar" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private fun getStylesXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1">
    <numFmt numFmtId="164" formatCode="#,##0.00\ &quot;TL&quot;"/>
  </numFmts>
  <fonts count="2">
    <font>
      <sz val="11"/>
      <color rgb="FF101923"/>
      <name val="Calibri"/>
    </font>
    <font>
      <b/>
      <sz val="11"/>
      <color rgb="FFFFFFFF"/>
      <name val="Calibri"/>
    </font>
  </fonts>
  <fills count="3">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill>
      <patternFill patternType="solid">
        <fgColor rgb="FF182430"/>
      </patternFill>
    </fill>
  </fills>
  <borders count="1">
    <border>
      <left/><right/><top/><bottom/><diagonal/>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="3">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
    <xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
  </cellXfs>
</styleSheet>"""

    private fun buildWorksheetXml(assets: List<Asset>): String {
        val headers = listOf(
            "Demirbaş Kodu",
            "Demirbaş Adı",
            "Kategori",
            "Alt Kategori",
            "Marka",
            "Model",
            "Seri No",
            "Durum",
            "Fiziksel Durum",
            "Zimmetli Kişi",
            "Departman",
            "Şube",
            "Bina",
            "Kat",
            "Oda",
            "Satın Alma Tarihi",
            "Satın Alma Bedeli",
            "Tedarikçi",
            "Fatura No",
            "Garanti Bitiş Tarihi",
            "Son Bakım Tarihi",
            "Sonraki Bakım Tarihi",
            "Açıklama"
        )

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>
    <col min="1" max="1" width="18" customWidth="1"/>
    <col min="2" max="2" width="28" customWidth="1"/>
    <col min="3" max="3" width="20" customWidth="1"/>
    <col min="4" max="4" width="16" customWidth="1"/>
    <col min="5" max="5" width="16" customWidth="1"/>
    <col min="6" max="6" width="18" customWidth="1"/>
    <col min="7" max="7" width="20" customWidth="1"/>
    <col min="8" max="8" width="14" customWidth="1"/>
    <col min="9" max="9" width="16" customWidth="1"/>
    <col min="10" max="10" width="22" customWidth="1"/>
    <col min="11" max="11" width="18" customWidth="1"/>
    <col min="12" max="12" width="16" customWidth="1"/>
    <col min="13" max="13" width="14" customWidth="1"/>
    <col min="14" max="14" width="12" customWidth="1"/>
    <col min="15" max="15" width="14" customWidth="1"/>
    <col min="16" max="16" width="18" customWidth="1"/>
    <col min="17" max="17" width="20" customWidth="1"/>
    <col min="18" max="18" width="20" customWidth="1"/>
    <col min="19" max="19" width="18" customWidth="1"/>
    <col min="20" max="20" width="18" customWidth="1"/>
    <col min="21" max="21" width="18" customWidth="1"/>
    <col min="22" max="22" width="18" customWidth="1"/>
    <col min="23" max="23" width="35" customWidth="1"/>
  </cols>
  <sheetData>
""")

        // Row 1: Header (Style 1: Bold with dark fill)
        sb.append("    <row r=\"1\">\n")
        for ((cIdx, h) in headers.withIndex()) {
            val colLetter = getColumnLetter(cIdx + 1)
            val cellRef = "$colLetter" + "1"
            sb.append("      <c r=\"$cellRef\" s=\"1\" t=\"inlineStr\"><is><t>${escapeXml(h)}</t></is></c>\n")
        }
        sb.append("    </row>\n")

        // Data Rows
        for ((rIdx, asset) in assets.withIndex()) {
            val rowNum = rIdx + 2
            sb.append("    <row r=\"$rowNum\">\n")

            val values = listOf(
                asset.assetCode,
                asset.assetName,
                asset.category,
                asset.subcategory,
                asset.brand,
                asset.model,
                asset.serialNumber,
                AssetStatus.fromString(asset.status).labelTr,
                AssetCondition.fromString(asset.condition).labelTr,
                asset.assignedUserName.ifBlank { "Boşta" },
                asset.department,
                asset.branch,
                asset.building,
                asset.floor,
                asset.room,
                asset.purchaseDate,
                asset.purchasePrice.toString(), // Column 17 is Price (numeric)
                asset.supplier,
                asset.invoiceNumber,
                asset.warrantyEndDate,
                asset.lastMaintenanceDate,
                asset.nextMaintenanceDate,
                asset.description
            )

            for ((cIdx, value) in values.withIndex()) {
                val colLetter = getColumnLetter(cIdx + 1)
                val cellRef = "$colLetter$rowNum"

                if (cIdx == 16) { // Price column (number)
                    val priceNum = asset.purchasePrice
                    sb.append("      <c r=\"$cellRef\" s=\"2\" t=\"n\"><v>$priceNum</v></c>\n")
                } else {
                    sb.append("      <c r=\"$cellRef\" t=\"inlineStr\"><is><t>${escapeXml(value)}</t></is></c>\n")
                }
            }
            sb.append("    </row>\n")
        }

        sb.append("  </sheetData>\n</worksheet>")
        return sb.toString()
    }

    private fun getColumnLetter(colIndex: Int): String {
        var num = colIndex
        var str = ""
        while (num > 0) {
            val rem = (num - 1) % 26
            str = ('A' + rem) + str
            num = (num - 1) / 26
        }
        return str
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun truncateText(text: String, maxLen: Int): String {
        return if (text.length <= maxLen) text else text.substring(0, maxLen - 1) + "…"
    }

    // CSV backward compatibility
    fun generateAssetCsv(assets: List<Asset>): String {
        val sb = StringBuilder()
        sb.append("Demirbaş Kodu;Demirbaş Adı;Kategori;Alt Kategori;Marka;Model;Seri No;Durum;Fiziksel Durum;Zimmetli Kişi;Departman;Şube;Bina;Kat;Oda;Satın Alma Tarihi;Satın Alma Bedeli (TL);Tedarikçi;Fatura No;Garanti Bitiş Tarihi;Son Bakım Tarihi;Sonraki Bakım Tarihi;Açıklama\n")
        for (a in assets) {
            val fields = listOf(
                a.assetCode,
                escapeCsv(a.assetName),
                escapeCsv(a.category),
                escapeCsv(a.subcategory),
                escapeCsv(a.brand),
                escapeCsv(a.model),
                escapeCsv(a.serialNumber),
                AssetStatus.fromString(a.status).labelTr,
                AssetCondition.fromString(a.condition).labelTr,
                escapeCsv(a.assignedUserName.ifBlank { "Boşta" }),
                escapeCsv(a.department),
                escapeCsv(a.branch),
                escapeCsv(a.building),
                escapeCsv(a.floor),
                escapeCsv(a.room),
                a.purchaseDate,
                String.format(Locale.US, "%.2f", a.purchasePrice),
                escapeCsv(a.supplier),
                escapeCsv(a.invoiceNumber),
                a.warrantyEndDate,
                a.lastMaintenanceDate,
                a.nextMaintenanceDate,
                escapeCsv(a.description)
            )
            sb.append(fields.joinToString(";")).append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(str: String): String {
        var clean = str.replace(";", ",")
        clean = clean.replace("\n", " ").replace("\r", "")
        return clean
    }

    fun shareText(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
