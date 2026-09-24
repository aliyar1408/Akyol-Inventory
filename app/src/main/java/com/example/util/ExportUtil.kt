package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.Asset

object ExportUtil {

    fun generateAssetCsv(assets: List<Asset>): String {
        val sb = StringBuilder()
        // Turkish UTF-8 CSV headers
        sb.append("Demirbaş Kodu;Demirbaş Adı;Kategori;Marka;Model;Seri Numarası;Barkod;Departman;Zimmetli Personel;Zimmet Tarihi;Durum;Kondisyon;Bina;Kat;Oda;Satın Alma Tarihi;Satın Alma Bedeli (TL);Tedarikçi;Fatura No;Garanti Bitiş;Son Bakım Tarihi\n")

        for (a in assets) {
            val line = listOf(
                a.assetCode,
                escapeCsv(a.assetName),
                escapeCsv(a.category),
                escapeCsv(a.brand),
                escapeCsv(a.model),
                escapeCsv(a.serialNumber),
                escapeCsv(a.barcode),
                escapeCsv(a.department),
                escapeCsv(a.assignedUserName.ifBlank { "Boşta" }),
                a.assignmentDate,
                a.getStatusEnum().labelTr,
                a.getConditionEnum().labelTr,
                escapeCsv(a.building),
                escapeCsv(a.floor),
                escapeCsv(a.room),
                a.purchaseDate,
                String.format(java.util.Locale.US, "%.2f", a.purchasePrice),
                escapeCsv(a.supplier),
                escapeCsv(a.invoiceNumber),
                a.warrantyEndDate,
                a.lastMaintenanceDate
            ).joinToString(";")
            sb.append(line).append("\n")
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
