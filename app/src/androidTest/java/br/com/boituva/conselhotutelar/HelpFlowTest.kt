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
class HelpFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun navigatesToHelpAndCapturesDialWithoutOpeningExternalApps() {
        val actions = mutableListOf<ExternalAction>()
        val copiedNumbers = mutableListOf<String>()
        val viewModel = compose.runOnUiThread { compose.activity.fixtureViewModel() }
        compose.setContent {
            CouncilRoute(
                viewModel = viewModel,
                openAction = {
                    actions.add(it)
                    actions.size == 1
                },
                copyPhone = { _, number -> copiedNumbers.add(number) },
            )
        }
        compose.waitUntil { viewModel.state.value.catalog is CatalogUiState.Ready }

        compose.onNodeWithText("Polícia de exemplo").performScrollTo().performClick()
        compose.onNodeWithText("Preciso de ajuda").performClick()
        listOf("Urgência policial", "Emergência médica", "Comunicar uma suspeita ou violação").forEach {
            compose.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithText("Abrir discador para Polícia de exemplo").performScrollTo().performClick()
        compose.onNodeWithText("Não foi possível abrir o canal. Use outro canal ou copie o número.")
            .assertIsDisplayed()
        compose.onNodeWithText("Copiar telefone de Polícia de exemplo").performScrollTo().performClick()

        compose.runOnIdle {
            assertEquals(listOf(ExternalAction.Dial("000"), ExternalAction.Dial("000")), actions)
            assertEquals(listOf("000"), copiedNumbers)
        }
    }

    @Test
    fun preparesDialIntentWithoutPlacingACall() {
        val intent = ExternalAction.Dial("000").toIntent()
        assertEquals(Intent.ACTION_DIAL, intent.action)
        assertEquals("tel:000", intent.dataString)
    }
}
