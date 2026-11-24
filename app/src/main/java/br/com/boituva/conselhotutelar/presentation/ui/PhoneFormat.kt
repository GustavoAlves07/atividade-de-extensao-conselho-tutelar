package br.com.boituva.conselhotutelar.presentation.ui

internal fun displayPhone(number: String): String = when (number.length) {
    10, 11 -> "(${number.take(2)}) ${number.drop(2).dropLast(4)}-${number.takeLast(4)}"
    else -> number
}
