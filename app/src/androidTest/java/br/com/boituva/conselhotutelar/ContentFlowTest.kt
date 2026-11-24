package br.com.boituva.conselhotutelar

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.boituva.conselhotutelar.platform.toIntent
import br.com.boituva.conselhotutelar.presentation.CatalogUiState
import br.com.boituva.conselhotutelar.presentation.ExternalAction
import br.com.boituva.conselhotutelar.presentation.ui.CouncilRoute
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class ContentFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val actions = mutableListOf<ExternalAction>()

    @Test
    fun readsGuideAndReturnsToExpandedQuestionOrHelp() {
        openApp()
        click("Conheça seus direitos")
        compose.onNodeWithText("Artigo em revisão").assertDoesNotExist()
        click("Pergunta de exemplo?")
        compose.onNodeWithText("Resposta de exemplo.").performScrollTo().assertIsDisplayed()
        click("Ler Como comunicar uma suspeita")
        compose.onNodeWithText("Como acolher").performScrollTo().assertIsDisplayed()
        click("Ver canais de ajuda")
        compose.onNodeWithText("Comunicar uma suspeita ou violação").assertIsDisplayed()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Como comunicar uma suspeita").assertIsDisplayed()
        compose.onNodeWithText("Voltar").performClick()
        compose.onNodeWithText("Resposta de exemplo.").performScrollTo().assertIsDisplayed()

        compose.onNodeWithText("Início").performClick()
        click("Preciso de ajuda")
        click("Como comunicar uma suspeita")
        click("Ver canais de ajuda")
        compose.onNodeWithText("Como comunicar uma suspeita").assertIsDisplayed()
    }

    @Test
    fun usesConfirmedAddressAndHidesMissingFields() {
        openApp()
        compose.onNodeWithText("Rede").performClick()
        compose.onNodeWithText("Serviço em revisão").assertDoesNotExist()
        click("Apoio de exemplo")
        click("Serviço de exemplo")
        val address = "Rua de Exemplo, 123, Cidade de Exemplo – SP"
        compose.onNodeWithText(address).performScrollTo().assertIsDisplayed()
        click("Abrir no mapa")
        val action = ExternalAction.OpenMap(address)
        val intent = action.toIntent()
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("geo", intent.data?.scheme)
        assertEquals("0,0?q=$address", intent.data?.schemeSpecificPart)
        compose.runOnIdle { assertEquals(listOf(action), actions) }
        compose.onNodeWithText("Voltar").performClick()
        compose.onNodeWithText("Voltar").performClick()
        click("Serviço sem endereço")
        compose.onNodeWithText("Abrir no mapa").assertDoesNotExist()
        compose.onNodeWithText("Horário de atendimento").assertDoesNotExist()
        compose.onNodeWithText("Área atendida").assertDoesNotExist()
    }

    @Test
    fun showsOnlyApprovedActiveCampaignsAndRoutesCorrectionsExternally() {
        openApp()
        compose.onNodeWithText("Campanha de exemplo").performScrollTo().assertIsDisplayed()
        listOf("Campanha encerrada", "Campanha em revisão", "Campanha sem aprovação").forEach {
            compose.onNodeWithText(it).assertDoesNotExist()
        }
        click("Ver material")
        click("Compartilhar link")
        click("Sobre o projeto")
        click("Sugerir uma correção")

        val url = "https://example.org/material"
        val share = ExternalAction.ShareLink(url)
        val chooser = share.toIntent()
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertEquals(Intent.ACTION_SEND, send?.action)
        assertEquals("text/plain", send?.type)
        assertEquals(url, send?.getStringExtra(Intent.EXTRA_TEXT))
        compose.runOnIdle {
            assertEquals(
                listOf(
                    ExternalAction.OpenLink(url),
                    share,
                    ExternalAction.OpenLink("https://example.org/correcoes"),
                ),
                actions,
            )
        }
    }

    private fun openApp() {
        val viewModel = compose.runOnUiThread { compose.activity.fixtureViewModel() }
        compose.setContent {
            CouncilRoute(
                viewModel = viewModel,
                openAction = { actions.add(it) },
                copyPhone = { _, _ -> },
            )
        }
        compose.waitUntil { viewModel.state.value.catalog is CatalogUiState.Ready }
    }

    private fun click(text: String) {
        compose.onNodeWithText(text).performScrollTo().performClick()
    }
}
