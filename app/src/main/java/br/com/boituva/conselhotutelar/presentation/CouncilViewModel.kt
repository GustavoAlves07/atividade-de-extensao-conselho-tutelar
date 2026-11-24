package br.com.boituva.conselhotutelar.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.boituva.conselhotutelar.domain.Campaign
import br.com.boituva.conselhotutelar.domain.CatalogLoadException
import br.com.boituva.conselhotutelar.domain.CatalogRepository
import br.com.boituva.conselhotutelar.domain.ContentCatalog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data object Unavailable : CatalogUiState
    data class Ready(val catalog: ContentCatalog, val activeCampaigns: List<Campaign>) : CatalogUiState
}

internal data class CouncilUiState(
    val route: AppRoute,
    val canGoBack: Boolean,
    val catalog: CatalogUiState = CatalogUiState.Loading,
)

private const val BACK_STACK_KEY = "council.backStack"

internal class CouncilViewModel(
    private val repository: CatalogRepository,
    private val savedState: SavedStateHandle,
    private val today: () -> String = ::todayInBoituva,
) : ViewModel() {
    private var backStack = savedState.get<ArrayList<String>>(BACK_STACK_KEY)
        ?.mapNotNull(AppRoute::restore)
        ?.takeIf { it.firstOrNull() == AppRoute.Tab.HOME }
        ?: listOf(AppRoute.Tab.HOME)

    private val mutableState = MutableStateFlow(CouncilUiState(backStack.last(), backStack.size > 1))
    val state = mutableState.asStateFlow()

    private val feedback = Channel<String>(Channel.BUFFERED)
    val messages = feedback.receiveAsFlow()

    init {
        loadCatalog()
    }

    fun retry() {
        if (state.value.catalog == CatalogUiState.Unavailable) loadCatalog()
    }

    fun navigate(route: AppRoute) {
        val next = when (route) {
            AppRoute.Tab.HOME -> listOf(route)
            is AppRoute.Tab -> listOf(AppRoute.Tab.HOME, route)
            else -> {
                val existing = backStack.indexOf(route)
                if (existing < 0) backStack + route else backStack.take(existing + 1)
            }
        }
        updateNavigation(next)
    }

    fun goBack() {
        if (backStack.size > 1) updateNavigation(backStack.dropLast(1))
    }

    fun showHelpFromArticle() {
        when (backStack.getOrNull(backStack.lastIndex - 1)) {
            AppRoute.Help, AppRoute.ReportHelp -> goBack()
            else -> navigate(AppRoute.ReportHelp)
        }
    }

    fun actionFinished(action: ExternalAction, succeeded: Boolean) {
        if (succeeded) return
        val message = when (action) {
            is ExternalAction.Dial -> "Não foi possível abrir o canal. Use outro canal ou copie o número."
            is ExternalAction.OpenLink -> "Não foi possível abrir o link."
            is ExternalAction.OpenMap -> "Não foi possível abrir o mapa. Consulte o endereço na ficha."
            is ExternalAction.ShareLink -> "Não foi possível compartilhar o link."
        }
        feedback.trySend(message)
    }

    fun numberCopied() {
        feedback.trySend("Número copiado.")
    }

    private fun loadCatalog() {
        mutableState.update { it.copy(catalog = CatalogUiState.Loading) }
        viewModelScope.launch {
            val result = try {
                val catalog = repository.load()
                CatalogUiState.Ready(catalog, activeCampaigns(catalog))
            } catch (_: CatalogLoadException) {
                CatalogUiState.Unavailable
            }
            mutableState.update { it.copy(catalog = result) }
        }
    }

    private fun updateNavigation(next: List<AppRoute>) {
        backStack = next
        savedState[BACK_STACK_KEY] = ArrayList(next.map { it.key })
        mutableState.update { previous ->
            val catalog = previous.catalog
            val route = next.last()
            previous.copy(
                route = route,
                canGoBack = next.size > 1,
                catalog = if (route == AppRoute.Tab.HOME && catalog is CatalogUiState.Ready) {
                    catalog.copy(activeCampaigns = activeCampaigns(catalog.catalog))
                } else {
                    catalog
                },
            )
        }
    }

    private fun activeCampaigns(catalog: ContentCatalog): List<Campaign> {
        val day = today()
        return catalog.campaigns.filter { it.isActiveOn(day) }
    }
}

private fun todayInBoituva(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
}.format(Date())
