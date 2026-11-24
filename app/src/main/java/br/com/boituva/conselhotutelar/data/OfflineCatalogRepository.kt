package br.com.boituva.conselhotutelar.data

import android.content.res.AssetManager
import br.com.boituva.conselhotutelar.domain.CatalogLoadException
import br.com.boituva.conselhotutelar.domain.CatalogRepository
import br.com.boituva.conselhotutelar.domain.ContentCatalog
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException

internal class OfflineCatalogRepository(
    private val assets: AssetManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CatalogRepository {
    override suspend fun load(): ContentCatalog = withContext(ioDispatcher) {
        try {
            readContent(assets, readChannels(assets))
        } catch (error: IOException) {
            throw CatalogLoadException(error)
        } catch (error: JSONException) {
            throw CatalogLoadException(error)
        } catch (error: IllegalArgumentException) {
            throw CatalogLoadException(error)
        }
    }
}
