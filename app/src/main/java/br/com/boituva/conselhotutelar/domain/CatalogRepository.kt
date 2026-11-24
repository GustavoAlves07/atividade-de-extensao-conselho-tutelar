package br.com.boituva.conselhotutelar.domain

internal fun interface CatalogRepository {
    /** Loads the content bundled with the app, or throws [CatalogLoadException]. */
    suspend fun load(): ContentCatalog
}

internal class CatalogLoadException(cause: Throwable) : Exception("Falha ao ler o catálogo", cause)
