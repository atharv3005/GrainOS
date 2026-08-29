package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ProcurementEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportProcurements(context: Context, procurements: List<ProcurementEntity>) {
        try {
            // Create a file in the cache directory
            val cachePath = File(context.cacheDir, "csv_exports")
            cachePath.mkdirs()
            val fileName = "procurements_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
            val file = File(cachePath, fileName)

            val writer = FileWriter(file)
            
            // Write CSV Header
            writer.append("Token No,Date,Farmer Name,Mobile,Village,Vehicle No,Crop,Gross Wt (Kg),Tare Wt (Kg),Net Wt (Kg),Rate/Qtl,Total Amount,Status,Payment Status\n")

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            
            // Write Rows
            for (p in procurements) {
                val dateStr = dateFormat.format(Date(p.createdAt))
                writer.append("${p.tokenNo},")
                writer.append("${dateStr},")
                writer.append("${escapeCsv(p.farmerName)},")
                writer.append("${escapeCsv(p.mobileNumber)},")
                writer.append("${escapeCsv(p.village)},")
                writer.append("${escapeCsv(p.vehicleNumber)},")
                writer.append("${escapeCsv(p.cropType)},")
                writer.append("${p.grossWeightKg},")
                writer.append("${p.tareWeightKg},")
                writer.append("${p.netWeightKg},")
                writer.append("${p.ratePerQuintal},")
                writer.append("${p.totalAmount},")
                writer.append("${p.status},")
                writer.append("${p.paymentStatus}\n")
            }

            writer.flush()
            writer.close()

            // Share the file via Intent
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Procurements Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Export Procurements CSV"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun escapeCsv(value: String): String {
        var escaped = value
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = escaped.replace("\"", "\"\"")
            escaped = "\"$escaped\""
        }
        return escaped
    }
}
