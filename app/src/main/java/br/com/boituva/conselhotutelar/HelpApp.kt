package br.com.boituva.conselhotutelar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun HelpApp(
    channels: List<Channel>,
    openAction: (ExternalAction) -> Boolean,
    copyPhone: (String, String) -> Unit,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val homeScroll = rememberScrollState()
    val helpScroll = rememberScrollState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun launchAction(action: ExternalAction) {
        if (!openAction(action)) {
            scope.launch {
                snackbar.showSnackbar("Não foi possível abrir o canal. Use outro canal ou copie o número.")
            }
        }
    }

    BackHandler(enabled = showHelp) { showHelp = false }

    MaterialTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { insets ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets)
                    .verticalScroll(if (showHelp) helpScroll else homeScroll)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (showHelp) {
                    OutlinedButton(onClick = { showHelp = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Voltar ao início")
                    }
                    Text("Preciso de ajuda", style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() })
                    Text("Ligações dependem da rede telefônica. WhatsApp e sites dependem da internet.")
                    Category.entries.forEach { category ->
                        Text(category.label, style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() })
                        channels.filter { it.category == category }.forEach { channel ->
                            ChannelCard(
                                channel = channel,
                                openAction = ::launchAction,
                                onCopy = {
                                    copyPhone(channel.name, channel.phone)
                                    scope.launch { snackbar.showSnackbar("Número copiado.") }
                                },
                            )
                        }
                    }
                } else {
                    Text("Conselho Tutelar de Boituva", style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() })
                    Text("Projeto acadêmico de extensão da UNIASSELVI")
                    Button(onClick = { showHelp = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Preciso de ajuda")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    openAction: (ExternalAction) -> Unit,
    onCopy: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(channel.name, style = MaterialTheme.typography.titleMedium)
            Text(displayPhone(channel.phone))
            Button(
                onClick = { openAction(ExternalAction.Dial(channel.phone)) },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("Abrir discador para ${channel.name}")
            }
            OutlinedButton(onClick = onCopy, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Copiar telefone de ${channel.name}")
            }
            channel.whatsapp?.let { number ->
                OutlinedButton(
                    onClick = { openAction(ExternalAction.whatsApp(number)) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Abrir WhatsApp de ${channel.name}")
                }
            }
            channel.website?.let { url ->
                OutlinedButton(
                    onClick = { openAction(ExternalAction.OpenLink(url)) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Abrir site de ${channel.name}")
                }
            }
            OutlinedButton(
                onClick = { openAction(ExternalAction.OpenLink(channel.source)) },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("Consultar fonte de ${channel.name}")
            }
            Text("Conferido em ${channel.checkedOn.split("-").reversed().joinToString("/")}.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
