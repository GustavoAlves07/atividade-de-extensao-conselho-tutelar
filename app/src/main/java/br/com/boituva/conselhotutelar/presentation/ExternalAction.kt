package br.com.boituva.conselhotutelar.presentation

internal sealed interface ExternalAction {
    data class Dial(val phone: String) : ExternalAction
    data class OpenLink(val url: String) : ExternalAction
    data class OpenMap(val address: String) : ExternalAction
    data class ShareLink(val url: String) : ExternalAction

    companion object {
        fun whatsApp(number: String) = OpenLink("https://wa.me/$number")
    }
}
