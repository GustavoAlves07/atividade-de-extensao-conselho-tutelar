package br.com.boituva.conselhotutelar

import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelTest {
    @Test
    fun formatsShortAndLocalPhones() {
        assertEquals("190", displayPhone("190"))
        assertEquals("(11) 2345-6789", displayPhone("1123456789"))
        assertEquals("(11) 91234-5678", displayPhone("11912345678"))
    }

    @Test
    fun buildsWhatsAppLinkFromValidatedNumber() {
        assertEquals(
            ExternalAction.OpenLink("https://wa.me/5511912345678"),
            ExternalAction.whatsApp("5511912345678"),
        )
    }
}
