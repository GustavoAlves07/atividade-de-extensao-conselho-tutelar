package br.com.boituva.conselhotutelar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val HOME = "inicio"
private const val RIGHTS = "direitos"
private const val NETWORK = "rede"
private const val HELP = "ajuda"
private const val REPORT_HELP = "ajuda/suspeita"
private const val ABOUT = "sobre"
private const val ARTICLE = "artigo"
private const val SERVICE = "servico"
private const val TOPIC = "apoio"
internal const val REPORT_ARTICLE = "comunicar_suspeita"

private data class Destination(val route: String, val label: String, val icon: Int)

private val destinations = listOf(
    Destination(HOME, "Início", R.drawable.ic_home),
    Destination(RIGHTS, "Direitos", R.drawable.ic_book),
    Destination(NETWORK, "Rede", R.drawable.ic_network),
)

@Composable
internal fun HelpApp(
    channels: List<Channel>,
    openAction: (ExternalAction) -> Boolean,
    copyPhone: (String, String) -> Unit,
    content: ContentCatalog = ContentCatalog(),
) {
    var backStack by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it },
        restore = { it },
    )) { mutableStateOf(listOf(HOME)) }
    val current = backStack.last()
    val stateHolder = rememberSaveableStateHolder()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigate(route: String) {
        val existing = backStack.indexOf(route)
        backStack = if (existing >= 0) backStack.take(existing + 1) else backStack + route
    }

    fun goBack() {
        if (backStack.size > 1) backStack = backStack.dropLast(1)
    }

    fun launchAction(action: ExternalAction) {
        if (!openAction(action)) {
            val message = when (action) {
                is ExternalAction.Dial ->
                    "Não foi possível abrir o canal. Use outro canal ou copie o número."
                is ExternalAction.OpenLink -> "Não foi possível abrir o link."
                is ExternalAction.OpenMap -> "Não foi possível abrir o mapa. Consulte o endereço na ficha."
                is ExternalAction.ShareLink -> "Não foi possível compartilhar o link."
            }
            scope.launch { snackbar.showSnackbar(message) }
        }
    }

    fun copyNumber(name: String, number: String) {
        copyPhone(name, number)
        scope.launch { snackbar.showSnackbar("Número copiado.") }
    }

    BackHandler(enabled = backStack.size > 1) { goBack() }

    val kind = current.substringBefore('/')
    val id = current.substringAfter('/')
    val article = content.articles.firstOrNull { kind == ARTICLE && it.id == id }
    val service = content.services.firstOrNull { kind == SERVICE && it.id == id }
    val topic = content.supportTopics.firstOrNull { kind == TOPIC && it.id == id }
    val openArticle: (String) -> Unit = { navigate("$ARTICLE/$it") }
    val openService: (String) -> Unit = { navigate("$SERVICE/$it") }

    val isMainScreen = destinations.any { it.route == current }
    val serviceCategory = service?.let { entry -> ServiceCategory.entries.firstOrNull { it.key == entry.category } }
    fun selectDestination(route: String) {
        backStack = if (route == HOME) listOf(HOME) else listOf(HOME, route)
    }

    CouncilTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = { if (!isMainScreen) BackBar(::goBack) },
            bottomBar = {
                if (isMainScreen) BottomNavigation(current, ::selectDestination)
            },
        ) { insets ->
            Box(
                Modifier.fillMaxSize().padding(insets),
                contentAlignment = Alignment.TopCenter,
            ) {
                stateHolder.SaveableStateProvider(current) {
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier.widthIn(max = 640.dp)
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .verticalScroll(scroll)
                            .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(17.dp),
                    ) {
                        when (kind) {
                            HOME -> HomeScreen(
                                channels, content.campaigns, ::launchAction,
                                onHelp = { navigate(HELP) },
                                onRights = { selectDestination(RIGHTS) },
                                onNetwork = { selectDestination(NETWORK) },
                                onAbout = { navigate(ABOUT) },
                            )
                            HELP -> {
                                PageIntro(
                                    "Preciso de ajuda", R.drawable.ic_phone, Tone.GREEN,
                                    "Escolha o canal oficial de acordo com a situação.",
                                )
                                HelpScreen(
                                    channels, scroll, current == REPORT_HELP, ::launchAction, ::copyNumber,
                                    onOrientation = content.articles.firstOrNull { it.id == REPORT_ARTICLE }
                                        ?.let { { openArticle(REPORT_ARTICLE) } },
                                )
                            }
                            RIGHTS -> RightsScreen(content, openArticle, ::launchAction)
                            NETWORK -> NetworkScreen(content, openService) { navigate("$TOPIC/$it") }
                            ABOUT -> {
                                PageIntro("Sobre o projeto", R.drawable.ic_info, Tone.GREEN)
                                AboutScreen(
                                    content.articles.firstOrNull { it.id == "conselho_tutelar" }?.references,
                                    content.editorialChannel,
                                    ::launchAction,
                                ) { navigate(HELP) }
                            }
                            ARTICLE -> article?.let {
                                val icon = if (it.id == REPORT_ARTICLE) R.drawable.ic_message else R.drawable.ic_book
                                PageIntro(it.title, icon, Tone.BLUE, it.summary)
                                ArticleScreen(it, content.services, openService, ::launchAction) {
                                    val previous = backStack.getOrNull(backStack.lastIndex - 1)
                                    when (previous) {
                                        HELP, REPORT_HELP -> goBack()
                                        else -> navigate(REPORT_HELP)
                                    }
                                }
                            }
                            SERVICE -> service?.let {
                                PageIntro(
                                    it.name,
                                    serviceCategory?.icon ?: R.drawable.ic_pin,
                                    serviceCategory?.tone ?: Tone.GREEN,
                                    it.summary,
                                )
                                ServiceScreen(it, channels, ::launchAction, ::copyNumber)
                            }
                            TOPIC -> topic?.let {
                                PageIntro(it.title, R.drawable.ic_people, Tone.AMBER, it.summary)
                                SupportScreen(it, content.services, openService, ::launchAction)
                            }
                        }
                        if (kind in listOf(ARTICLE, SERVICE, TOPIC) && article == null && service == null && topic == null) {
                            PageIntro(
                                "Conteúdo indisponível", R.drawable.ic_info, Tone.AMBER,
                                "Volte para consultar os conteúdos disponíveis.",
                            )
                        }
                    }
                }
            }
        }
    }
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
private fun BottomNavigation(current: String, onSelect: (String) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            NavigationBar(
                modifier = Modifier.widthIn(max = 640.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = current == destination.route,
                        onClick = { onSelect(destination.route) },
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

@Composable
private fun HelpScreen(
    channels: List<Channel>,
    scroll: ScrollState,
    focusReport: Boolean,
    openAction: (ExternalAction) -> Unit,
    copyPhone: (String, String) -> Unit,
    onOrientation: (() -> Unit)?,
) {
    var positioned by rememberSaveable { mutableStateOf(!focusReport) }
    var reportOffset by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(reportOffset) {
        if (!positioned) {
            reportOffset?.let {
                scroll.scrollTo(it)
                positioned = true
            }
        }
    }

    Category.entries.forEach { category ->
        val anchor = if (category == Category.REPORT && !positioned) {
            Modifier.onGloballyPositioned { reportOffset = it.positionInParent().y.roundToInt() }
        } else {
            Modifier
        }
        val (icon, tone) = when (category) {
            Category.POLICE -> R.drawable.ic_emergency to Tone.RED
            Category.MEDICAL -> R.drawable.ic_health to Tone.BLUE
            Category.REPORT -> R.drawable.ic_message to Tone.AMBER
        }
        Column(anchor, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(icon, tone)
                SectionHeading(category.label, Modifier.weight(1f))
            }
            channels.filter { it.category == category }.forEach { channel ->
                ChannelCard(channel, openAction, copyPhone)
            }
            if (category == Category.REPORT && onOrientation != null) {
                ContentCard("Como comunicar uma suspeita", R.drawable.ic_book, tone, onClick = onOrientation)
            }
        }
    }
    InfoNote("Ligações dependem da rede telefônica. WhatsApp e sites dependem da internet.")
}

@Composable
internal fun ChannelCard(
    channel: Channel,
    openAction: (ExternalAction) -> Unit,
    copyPhone: (String, String) -> Unit,
) {
    BodyPanel {
        SectionHeading(channel.name)
        ContactActions(channel.name, channel.phone, channel.whatsapp, channel.website, openAction, copyPhone,
            urgent = channel.category == Category.POLICE)
        ReferenceList(
            References(listOf(Source(channel.name, channel.source)), channel.checkedOn),
            openAction,
            expandable = true,
        )
    }
}

@Composable
internal fun ContactActions(
    name: String,
    phone: String?,
    whatsapp: String?,
    website: String?,
    openAction: (ExternalAction) -> Unit,
    copyPhone: (String, String) -> Unit,
    urgent: Boolean = false,
) {
    phone?.let { number ->
        val accent = if (urgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        Text(displayPhone(number), style = MaterialTheme.typography.headlineMedium, color = accent)
        Button(
            onClick = { openAction(ExternalAction.Dial(number)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            AppIcon(R.drawable.ic_phone, Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
            Text("Abrir discador para $name", Modifier.weight(1f))
        }
        ScreenAction("Copiar telefone de $name", R.drawable.ic_copy) { copyPhone(name, number) }
    }
    whatsapp?.let { number ->
        ScreenAction("Abrir WhatsApp de $name", R.drawable.ic_message) { openAction(ExternalAction.whatsApp(number)) }
    }
    website?.let { url ->
        ScreenAction("Abrir site de $name", R.drawable.ic_external) { openAction(ExternalAction.OpenLink(url)) }
    }
}
