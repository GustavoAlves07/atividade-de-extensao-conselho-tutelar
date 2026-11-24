package br.com.boituva.conselhotutelar.domain

internal enum class Category { POLICE, MEDICAL, REPORT }

internal data class Channel(
    val id: String,
    val name: String,
    val category: Category,
    val phone: String,
    val whatsapp: String?,
    val website: String?,
    val source: String,
    val checkedOn: String,
)
