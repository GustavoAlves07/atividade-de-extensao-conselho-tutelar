package br.com.boituva.conselhotutelar.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.boituva.conselhotutelar.R
import br.com.boituva.conselhotutelar.domain.Channel
import br.com.boituva.conselhotutelar.domain.ContentCatalog
import br.com.boituva.conselhotutelar.domain.NetworkService
import br.com.boituva.conselhotutelar.domain.SupportTopic
import br.com.boituva.conselhotutelar.presentation.ExternalAction

internal enum class ServiceCategory(
    val key: String,
    val label: String,
    @DrawableRes val icon: Int,
    val tone: Tone,
) {
    RIGHTS("protecao_de_direitos", "Proteção de direitos", R.drawable.ic_shield, Tone.GREEN),
    SOCIAL("assistencia_social", "Assistência social", R.drawable.ic_people, Tone.AMBER),
    HEALTH("saude", "Saúde", R.drawable.ic_health, Tone.BLUE),
    SAFETY("seguranca", "Segurança", R.drawable.ic_emergency, Tone.RED),
}

@Composable
internal fun NetworkScreen(
    content: ContentCatalog,
    openService: (String) -> Unit,
    openTopic: (String) -> Unit,
) {
    PageIntro(
        "Rede de proteção", R.drawable.ic_network, Tone.GREEN,
        "Consulte os serviços e veja como buscar atendimento em Boituva.",
    )
    val groups = content.services.groupBy { it.category }
    ServiceCategory.entries.forEach { category ->
        groups[category.key]?.let { services ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeading(category.label)
                services.forEach { service ->
                    ContentCard(service.name, category.icon, category.tone, service.summary) { openService(service.id) }
                }
            }
        }
    }
    if (content.supportTopics.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Onde buscar apoio")
            content.supportTopics.forEach { topic ->
                ContentCard(topic.title, R.drawable.ic_people, Tone.AMBER, topic.summary) { openTopic(topic.id) }
            }
        }
    }
}

@Composable
internal fun ServiceScreen(
    service: NetworkService,
    channels: List<Channel>,
    openAction: (ExternalAction) -> Unit,
    copyPhone: (String, String) -> Unit,
) {
    val serviceChannels = channels.filter { it.id in service.channelIds }
    val hasDirectContact = service.phone != null || service.whatsapp != null || service.website != null
    if (serviceChannels.isNotEmpty() || hasDirectContact) {
        SectionHeading("Contato")
        serviceChannels.forEach { ChannelCard(it, openAction, copyPhone) }
        if (hasDirectContact) {
            BodyPanel {
                ContactActions(service.name, service.phone, service.whatsapp, service.website, openAction, copyPhone)
            }
        }
        InfoNote("Ligações dependem da rede telefônica. WhatsApp e sites dependem da internet.")
    }

    if (service.address != null || service.openingHours != null || service.coverage != null ||
        service.accessConditions != null || service.accessibility != null
    ) {
        BodyPanel {
            service.address?.let {
                ServiceField("Endereço", it, R.drawable.ic_pin)
                ScreenAction("Abrir no mapa", R.drawable.ic_pin) { openAction(ExternalAction.OpenMap(it)) }
            }
            ServiceField("Horário de atendimento", service.openingHours, R.drawable.ic_clock)
            ServiceField("Área atendida", service.coverage, R.drawable.ic_network)
            ServiceField("Condições de acesso", service.accessConditions, R.drawable.ic_file)
            ServiceField("Acessibilidade", service.accessibility, R.drawable.ic_info)
        }
    }
    ReferenceList(service.references, openAction)
}

@Composable
internal fun SupportScreen(
    topic: SupportTopic,
    services: List<NetworkService>,
    openService: (String) -> Unit,
    openAction: (ExternalAction) -> Unit,
) {
    ServiceLinks(topic.serviceIds, services, openService)
    ReferenceList(topic.references, openAction)
}

@Composable
internal fun ServiceLinks(ids: List<String>, services: List<NetworkService>, openService: (String) -> Unit) {
    val linkedServices = ids.mapNotNull { id -> services.firstOrNull { it.id == id } }
    if (linkedServices.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeading("Serviços relacionados")
            linkedServices.forEach { service ->
                ContentCard(service.name, R.drawable.ic_pin) { openService(service.id) }
            }
        }
    }
}

@Composable
private fun ServiceField(label: String, value: String?, @DrawableRes icon: Int) {
    value?.let {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(icon, Modifier.size(19.dp), MaterialTheme.colorScheme.primary)
                SectionHeading(label)
            }
            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
