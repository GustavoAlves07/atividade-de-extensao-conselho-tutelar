package br.com.boituva.conselhotutelar.data

import android.content.res.AssetManager
import br.com.boituva.conselhotutelar.domain.Category
import br.com.boituva.conselhotutelar.domain.Channel
import org.json.JSONArray

internal fun readChannels(assets: AssetManager): List<Channel> {
    val json = assets.open("canais.json").bufferedReader().use { it.readText() }
    val entries = JSONArray(json)
    return buildList {
        for (index in 0 until entries.length()) {
            val entry = entries.getJSONObject(index)
            if (entry.getString("status") == "pendente") continue

            val category = when (val key = entry.getString("category")) {
                "policial" -> Category.POLICE
                "medica" -> Category.MEDICAL
                "suspeita" -> Category.REPORT
                else -> throw IllegalArgumentException("Categoria de canal inválida: $key")
            }
            add(
                Channel(
                    id = entry.getString("id"),
                    name = entry.getString("name"),
                    category = category,
                    phone = entry.getString("phone"),
                    whatsapp = entry.optString("whatsapp").ifBlank { null },
                    website = entry.optString("website").ifBlank { null },
                    source = entry.getString("source"),
                    checkedOn = entry.getString("checkedOn"),
                ),
            )
        }
    }
}
