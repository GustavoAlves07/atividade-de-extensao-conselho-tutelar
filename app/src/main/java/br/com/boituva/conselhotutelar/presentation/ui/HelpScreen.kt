package br.com.boituva.conselhotutelar.presentation.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import br.com.boituva.conselhotutelar.R
import br.com.boituva.conselhotutelar.domain.Category
import br.com.boituva.conselhotutelar.domain.Channel
import br.com.boituva.conselhotutelar.domain.References
import br.com.boituva.conselhotutelar.domain.Source
import br.com.boituva.conselhotutelar.presentation.ExternalAction
import kotlin.math.roundToInt

@Composable
internal fun HelpScreen(
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
        val (label, icon, tone) = when (category) {
            Category.POLICE -> Triple("Urgência policial", R.drawable.ic_emergency, Tone.RED)
            Category.MEDICAL -> Triple("Emergência médica", R.drawable.ic_health, Tone.BLUE)
            Category.REPORT -> Triple("Comunicar uma suspeita ou violação", R.drawable.ic_message, Tone.AMBER)
        }
        Column(anchor, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(icon, tone)
                SectionHeading(label, Modifier.weight(1f))
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
