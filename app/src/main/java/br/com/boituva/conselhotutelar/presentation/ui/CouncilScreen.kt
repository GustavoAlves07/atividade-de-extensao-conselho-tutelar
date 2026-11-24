package br.com.boituva.conselhotutelar.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.boituva.conselhotutelar.R
import br.com.boituva.conselhotutelar.domain.COUNCIL_ARTICLE_ID
import br.com.boituva.conselhotutelar.domain.REPORT_ARTICLE_ID
import br.com.boituva.conselhotutelar.presentation.AppRoute
import br.com.boituva.conselhotutelar.presentation.CatalogUiState
import br.com.boituva.conselhotutelar.presentation.CouncilUiState
import br.com.boituva.conselhotutelar.presentation.CouncilViewModel
import br.com.boituva.conselhotutelar.presentation.ExternalAction

private data class Destination(val tab: AppRoute.Tab, val label: String, val icon: Int)

private val tabs = listOf(
    Destination(AppRoute.Tab.HOME, "Início", R.drawable.ic_home),
    Destination(AppRoute.Tab.RIGHTS, "Direitos", R.drawable.ic_book),
    Destination(AppRoute.Tab.NETWORK, "Rede", R.drawable.ic_network),
)

@Composable
internal fun CouncilRoute(
    viewModel: CouncilViewModel,
    openAction: (ExternalAction) -> Boolean,
    copyPhone: (String, String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, snackbar) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    CouncilScreen(
        state = state,
        snackbar = snackbar,
        onNavigate = viewModel::navigate,
        onBack = viewModel::goBack,
        onHelpFromArticle = viewModel::showHelpFromArticle,
        onRetry = viewModel::retry,
        openAction = { viewModel.actionFinished(it, openAction(it)) },
        copyPhone = { name, number ->
            copyPhone(name, number)
            viewModel.numberCopied()
        },
    )
}

@Composable
private fun CouncilScreen(
    state: CouncilUiState,
    snackbar: SnackbarHostState,
    onNavigate: (AppRoute) -> Unit,
    onBack: () -> Unit,
    onHelpFromArticle: () -> Unit,
    onRetry: () -> Unit,
    openAction: (ExternalAction) -> Unit,
    copyPhone: (String, String) -> Unit,
) {
    val current = state.route
    val stateHolder = rememberSaveableStateHolder()
    BackHandler(enabled = state.canGoBack, onBack = onBack)

    CouncilTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = { if (current !is AppRoute.Tab) BackBar(onBack) },
            bottomBar = { if (current is AppRoute.Tab) BottomNavigation(current, onNavigate) },
        ) { insets ->
            Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
                stateHolder.SaveableStateProvider(current.key) {
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier.widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(scroll)
                            .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(17.dp),
                    ) {
                        when (val content = state.catalog) {
                            CatalogUiState.Loading -> InfoNote("Carregando conteúdo…")
                            CatalogUiState.Unavailable -> {
                                PageIntro(
                                    "Conteúdo indisponível", R.drawable.ic_info, Tone.AMBER,
                                    "O app não conseguiu ler o catálogo offline.",
                                )
                                ScreenAction("Tentar novamente", R.drawable.ic_arrow_right, onRetry)
                            }
                            is CatalogUiState.Ready -> CatalogScreen(
                                current, content, scroll, onNavigate, onHelpFromArticle, openAction, copyPhone,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogScreen(
    route: AppRoute,
    content: CatalogUiState.Ready,
    scroll: ScrollState,
    navigate: (AppRoute) -> Unit,
    onHelpFromArticle: () -> Unit,
    openAction: (ExternalAction) -> Unit,
    copyPhone: (String, String) -> Unit,
) {
    val catalog = content.catalog
    val openArticle: (String) -> Unit = { navigate(AppRoute.Article(it)) }
    val openService: (String) -> Unit = { navigate(AppRoute.Service(it)) }
    when (route) {
        AppRoute.Tab.HOME -> HomeScreen(
            catalog.channels, content.activeCampaigns, openAction,
            onHelp = { navigate(AppRoute.Help) },
            onRights = { navigate(AppRoute.Tab.RIGHTS) },
            onNetwork = { navigate(AppRoute.Tab.NETWORK) },
            onAbout = { navigate(AppRoute.About) },
        )
        AppRoute.Help, AppRoute.ReportHelp -> {
            PageIntro(
                "Preciso de ajuda", R.drawable.ic_phone, Tone.GREEN,
                "Escolha o canal oficial de acordo com a situação.",
            )
            HelpScreen(
                catalog.channels, scroll, route == AppRoute.ReportHelp, openAction, copyPhone,
                onOrientation = catalog.article(REPORT_ARTICLE_ID)?.let { { openArticle(it.id) } },
            )
        }
        AppRoute.Tab.RIGHTS -> RightsScreen(catalog, openArticle, openAction)
        AppRoute.Tab.NETWORK -> NetworkScreen(catalog, openService) { navigate(AppRoute.Support(it)) }
        AppRoute.About -> {
            PageIntro("Sobre o projeto", R.drawable.ic_info, Tone.GREEN)
            AboutScreen(catalog.article(COUNCIL_ARTICLE_ID)?.references, catalog.editorialChannel, openAction) {
                navigate(AppRoute.Help)
            }
        }
        is AppRoute.Article -> {
            val article = catalog.article(route.id)
            if (article == null) MissingDetail() else {
                val icon = if (article.id == REPORT_ARTICLE_ID) R.drawable.ic_message else R.drawable.ic_book
                PageIntro(article.title, icon, Tone.BLUE, article.summary)
                ArticleScreen(article, catalog.services, openService, openAction, onHelpFromArticle)
            }
        }
        is AppRoute.Service -> {
            val service = catalog.services.firstOrNull { it.id == route.id }
            if (service == null) MissingDetail() else {
                val category = ServiceCategory.entries.firstOrNull { it.key == service.category }
                PageIntro(service.name, category?.icon ?: R.drawable.ic_pin, category?.tone ?: Tone.GREEN, service.summary)
                ServiceScreen(service, catalog.channels, openAction, copyPhone)
            }
        }
        is AppRoute.Support -> {
            val topic = catalog.supportTopics.firstOrNull { it.id == route.id }
            if (topic == null) MissingDetail() else {
                PageIntro(topic.title, R.drawable.ic_people, Tone.AMBER, topic.summary)
                SupportScreen(topic, catalog.services, openService, openAction)
            }
        }
    }
}

@Composable
private fun MissingDetail() {
    PageIntro(
        "Conteúdo indisponível", R.drawable.ic_info, Tone.AMBER,
        "Volte para consultar os conteúdos disponíveis.",
    )
}

@Composable
private fun BackBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = 8.dp)) {
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                    AppIcon(R.drawable.ic_arrow_left, Modifier.size(20.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Voltar")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun BottomNavigation(current: AppRoute.Tab, onSelect: (AppRoute.Tab) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            NavigationBar(
                modifier = Modifier.widthIn(max = 640.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEach { destination ->
                    NavigationBarItem(
                        selected = current == destination.tab,
                        onClick = { onSelect(destination.tab) },
                        icon = { AppIcon(destination.icon, Modifier.size(23.dp)) },
                        label = { Text(destination.label, textAlign = TextAlign.Center) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}
