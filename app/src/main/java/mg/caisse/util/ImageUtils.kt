package mg.caisse.util

import android.content.Context
import android.net.Uri
import java.io.File

/** Copie une image choisie dans le stockage interne et retourne son chemin. */
fun copyImageToInternal(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "receipts").apply { mkdirs() }
        val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
