package br.com.boituva.conselhotutelar

import androidx.lifecycle.SavedStateHandle
import br.com.boituva.conselhotutelar.domain.Campaign
import br.com.boituva.conselhotutelar.domain.CatalogLoadException
import br.com.boituva.conselhotutelar.domain.CatalogRepository
import br.com.boituva.conselhotutelar.domain.ContentCatalog
import br.com.boituva.conselhotutelar.domain.References
import br.com.boituva.conselhotutelar.presentation.AppRoute
import br.com.boituva.conselhotutelar.presentation.CatalogUiState
import br.com.boituva.conselhotutelar.presentation.CouncilViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CouncilViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun reportsLoadFailureAndRetriesOnlyOnceWhileLoading() = runTest(dispatcher) {
        val catalog = ContentCatalog()
        var attempts = 0
        val model = CouncilViewModel(CatalogRepository {
            attempts++
            if (attempts == 1) throw CatalogLoadException(IOException("Fixture indisponível"))
            catalog
        }, SavedStateHandle())

        assertEquals(CatalogUiState.Loading, model.state.value.catalog)
        runCurrent()
        assertEquals(CatalogUiState.Unavailable, model.state.value.catalog)

        model.retry()
        model.retry()
        runCurrent()
        assertEquals(2, attempts)
        assertEquals(CatalogUiState.Ready(catalog, emptyList()), model.state.value.catalog)
    }

    @Test
    fun restoresNavigationAndReturnsToTheSameHelpOrigin() = runTest(dispatcher) {
        val repository = CatalogRepository { ContentCatalog() }
        val handle = SavedStateHandle()
        val article = AppRoute.Article("orientacao_teste")
        val model = CouncilViewModel(repository, handle)
        model.navigate(AppRoute.Help)
        model.navigate(article)

        val savedValues = handle.keys().associateWith { handle.get<Any?>(it) }
        val restored = CouncilViewModel(repository, SavedStateHandle(savedValues))
        assertEquals(article, restored.state.value.route)
        restored.showHelpFromArticle()
        assertEquals(AppRoute.Help, restored.state.value.route)
        restored.goBack()
        assertEquals(AppRoute.Tab.HOME, restored.state.value.route)
        assertFalse(restored.state.value.canGoBack)

        restored.navigate(AppRoute.Tab.RIGHTS)
        restored.navigate(article)
        restored.showHelpFromArticle()
        assertEquals(AppRoute.ReportHelp, restored.state.value.route)
        restored.goBack()
        assertEquals(article, restored.state.value.route)
        runCurrent()
    }

    @Test
    fun refreshesPublishedCampaignsWhenReturningHome() = runTest(dispatcher) {
        val campaign = Campaign(
            title = "Campanha fictícia",
            summary = "Descrição fictícia.",
            materialUrl = "https://example.org/material",
            startsOn = "2026-11-22",
            endsOn = "2026-11-23",
            references = References(emptyList(), "2026-11-22"),
        )
        val catalog = ContentCatalog(campaigns = listOf(campaign))
        var day = "2026-11-23"
        val model = CouncilViewModel(CatalogRepository { catalog }, SavedStateHandle(), today = { day })
        runCurrent()
        assertEquals(CatalogUiState.Ready(catalog, listOf(campaign)), model.state.value.catalog)

        model.navigate(AppRoute.Tab.NETWORK)
        day = "2026-11-24"
        model.goBack()
        assertEquals(CatalogUiState.Ready(catalog, emptyList()), model.state.value.catalog)
    }
}
