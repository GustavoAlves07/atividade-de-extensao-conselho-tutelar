package br.com.boituva.conselhotutelar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun HomeScreen(
    channels: List<Channel>,
    campaigns: List<Campaign>,
    openAction: (ExternalAction) -> Unit,
    onHelp: () -> Unit,
    onRights: () -> Unit,
    onNetwork: () -> Unit,
    onAbout: () -> Unit,
) {
    BrandHero(onHelp)
    channels.firstOrNull { it.id == "policia" }?.let { police ->
        EmergencyCard(police) { openAction(ExternalAction.Dial(police.phone)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeading("Orientação e serviços", Modifier.padding(bottom = 2.dp))
        ContentCard(
            title = "Conheça seus direitos",
            icon = R.drawable.ic_book,
            tone = Tone.BLUE,
            summary = "Guia do ECA e dúvidas frequentes",
            onClick = onRights,
        )
        ContentCard(
            title = "Rede de proteção",
            icon = R.drawable.ic_network,
            summary = "Encontre atendimento e serviços em Boituva",
            onClick = onNetwork,
        )
        ContentCard("Sobre o projeto", R.drawable.ic_info, onClick = onAbout)
    }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
    }.format(Date())
    val activeCampaigns = campaigns.filter { it.isActiveOn(today) }
    if (activeCampaigns.isNotEmpty()) {
        SectionHeading("Campanhas e materiais educativos")
        activeCampaigns.forEach { CampaignCard(it, openAction) }
    }
}

@Composable
private fun EmergencyCard(channel: Channel, onDial: () -> Unit) {
    Surface(
        onClick = onDial,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        shape = RoundedCornerShape(18.dp),
        color = Tone.RED.background,
        contentColor = Tone.RED.ink,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(R.drawable.ic_emergency, Tone.RED)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(channel.name, style = MaterialTheme.typography.titleMedium)
                Text("Urgência policial · Abrir discador", style = MaterialTheme.typography.bodyMedium)
            }
            Text(displayPhone(channel.phone), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
internal fun RightsScreen(
    content: ContentCatalog,
    openArticle: (String) -> Unit,
    openAction: (ExternalAction) -> Unit,
) {
    PageIntro(
        "Conheça seus direitos", R.drawable.ic_book, Tone.BLUE,
        "Orientações sobre o Conselho Tutelar e o Estatuto da Criança e do Adolescente.",
    )
    content.articles.firstOrNull { it.id == "conselho_tutelar" }?.let { article ->
        ContentCard(article.title, R.drawable.ic_shield, summary = article.summary) { openArticle(article.id) }
    }
    content.articles.firstOrNull { it.id == REPORT_ARTICLE }?.let { article ->
        ContentCard(article.title, R.drawable.ic_message, Tone.AMBER, emphasized = true) { openArticle(article.id) }
    }
    val themes = content.articles.filter { it.id != "conselho_tutelar" && it.id != REPORT_ARTICLE }
    if (themes.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Temas do ECA")
            themes.forEach { article ->
                val icon = when (article.id) {
                    "vida_e_saude", "cultura_esporte_e_lazer" -> R.drawable.ic_health
                    "convivencia" -> R.drawable.ic_people
                    "dignidade_e_respeito" -> R.drawable.ic_shield
                    else -> R.drawable.ic_book
                }
                ContentCard(article.title, icon, Tone.BLUE, article.summary) { openArticle(article.id) }
            }
        }
    }
    if (content.questions.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Dúvidas frequentes")
            val articles = content.articles.associateBy { it.id }
            content.questions.forEach { question ->
                key(question.id) {
                    ExpandableSection(question.question, R.drawable.ic_help) {
                        Text(question.answer, style = MaterialTheme.typography.bodyLarge)
                        articles[question.articleId]?.let { article ->
                            ScreenAction("Ler ${article.title}", R.drawable.ic_book) { openArticle(article.id) }
                        }
                        ReferenceList(question.references, openAction, expandable = true)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArticleScreen(
    article: GuideArticle,
    services: List<NetworkService>,
    openService: (String) -> Unit,
    openAction: (ExternalAction) -> Unit,
    onHelp: () -> Unit,
) {
    BodyPanel {
        article.sections.forEachIndexed { index, section ->
            if (index > 0) {
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
            SectionHeading(section.heading)
            Text(section.body, style = MaterialTheme.typography.bodyLarge)
        }
    }
    if (article.channelIds.isNotEmpty()) {
        ContentCard("Ver canais de ajuda", R.drawable.ic_phone, emphasized = true, onClick = onHelp)
    }
    ServiceLinks(article.serviceIds, services, openService)
    ReferenceList(article.references, openAction)
}

@Composable
internal fun AboutScreen(
    projectReferences: References?,
    editorial: EditorialChannel?,
    openAction: (ExternalAction) -> Unit,
    onHelp: () -> Unit,
) {
    BodyPanel {
        SectionHeading("Projeto de extensão de ADS da UNIASSELVI")
        Text(
            "O aplicativo orienta a comunidade de Boituva sobre direitos de crianças e adolescentes e encaminha aos canais oficiais de proteção. O Conselho Tutelar e os demais serviços atendem pelos próprios canais.",
            style = MaterialTheme.typography.bodyLarge,
        )
        ScreenAction("Ver canais oficiais de ajuda", R.drawable.ic_phone, onHelp)
    }
    projectReferences?.let { ReferenceList(it, openAction) }
    editorial?.let {
        BodyPanel {
            SectionHeading("Correções de conteúdo")
            Text(
                "Use o canal editorial para sugerir ajustes nas informações do app. Para comunicar uma suspeita ou violação, procure um canal oficial de ajuda.",
                style = MaterialTheme.typography.bodyLarge,
            )
            ScreenAction("Sugerir uma correção", R.drawable.ic_message) { openAction(ExternalAction.OpenLink(it.url)) }
            ReferenceList(it.references, openAction, expandable = true)
        }
    }
}

@Composable
private fun CampaignCard(campaign: Campaign, openAction: (ExternalAction) -> Unit) {
    BodyPanel {
        IconTile(R.drawable.ic_book, Tone.AMBER)
        SectionHeading(campaign.title)
        Text(campaign.summary, style = MaterialTheme.typography.bodyLarge)
        campaign.startsOn?.let { Text("Início em ${displayDate(it)}") }
        campaign.endsOn?.let { Text("Válida até ${displayDate(it)}") }
        ScreenAction("Ver material", R.drawable.ic_external) { openAction(ExternalAction.OpenLink(campaign.materialUrl)) }
        ScreenAction("Compartilhar link", R.drawable.ic_share) { openAction(ExternalAction.ShareLink(campaign.materialUrl)) }
        ReferenceList(campaign.references, openAction, expandable = true)
    }
}

@Composable
internal fun ReferenceList(
    references: References,
    openAction: (ExternalAction) -> Unit,
    expandable: Boolean = false,
) {
    ExpandableSection(if (expandable) "Fonte e conferência" else "Fontes", R.drawable.ic_file) {
        Text(
            "Conferido em ${displayDate(references.checkedOn)}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        references.sources.forEach { source ->
            ScreenAction("Consultar fonte: ${source.name}", R.drawable.ic_external) {
                openAction(ExternalAction.OpenLink(source.url))
            }
        }
    }
}

@Composable
private fun ExpandableSection(label: String, @DrawableRes icon: Int, content: @Composable () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Surface(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics {
                    stateDescription = if (expanded) "Expandido" else "Recolhido"
                },
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(icon, Modifier.size(19.dp))
                    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    AppIcon(R.drawable.ic_chevron_down, Modifier.size(18.dp).rotate(if (expanded) 180f else 0f))
                }
            }
            if (expanded) {
                Column(
                    Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}

private fun displayDate(date: String): String = date.split("-").reversed().joinToString("/")
