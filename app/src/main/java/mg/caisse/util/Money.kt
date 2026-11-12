package mg.caisse.util

import java.text.NumberFormat
import java.util.Locale

private val MGA_LOCALE = Locale("fr", "MG")

/** Formate un montant en Ariary : "1 250 000 Ar". */
fun formatAriary(amount: Long): String =
    "${NumberFormat.getIntegerInstance(MGA_LOCALE).format(amount)} Ar"

/** Parse une saisie utilisateur en montant entier (Ariary). */
fun parseAmount(text: String): Long? {
    val cleaned = text.trim()
        .replace(" ", "")
        .replace("\u00A0", "")
        .replace("Ar", "", ignoreCase = true)
        .replace(",", ".")
    if (cleaned.isEmpty()) return null
    return cleaned.toDoubleOrNull()?.toLong()
}
