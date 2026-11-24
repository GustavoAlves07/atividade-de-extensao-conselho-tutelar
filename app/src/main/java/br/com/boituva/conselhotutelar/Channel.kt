package br.com.boituva.conselhotutelar

internal enum class Category(val key: String, val label: String) {
    POLICE("policial", "Urgência policial"),
    MEDICAL("medica", "Emergência médica"),
    REPORT("suspeita", "Comunicar uma suspeita ou violação"),
}

internal data class Channel(
    val name: String,
    val category: Category,
    val phone: String,
    val whatsapp: String?,
    val website: String?,
    val source: String,
    val checkedOn: String,
)

internal sealed interface ExternalAction {
    data class Dial(val phone: String) : ExternalAction
    data class OpenLink(val url: String) : ExternalAction

    companion object {
        fun whatsApp(number: String) = OpenLink("https://wa.me/$number")
    }
}

internal fun displayPhone(number: String): String = when (number.length) {
    10, 11 -> "(${number.take(2)}) ${number.drop(2).dropLast(4)}-${number.takeLast(4)}"
    else -> number
}
