package br.com.boituva.conselhotutelar

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelpFlowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun navigatesToHelpAndCapturesDialWithoutOpeningExternalApps() {
        val actions = mutableListOf<ExternalAction>()
        val copiedNumbers = mutableListOf<String>()
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val channels = loadChannels(assets)

        compose.setContent {
            HelpApp(
                channels = channels,
                openAction = {
                    actions.add(it)
                    actions.size == 1
                },
                copyPhone = { _, number -> copiedNumbers.add(number) },
            )
        }

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
