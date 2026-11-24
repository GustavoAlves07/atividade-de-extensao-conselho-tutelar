package br.com.boituva.conselhotutelar

import android.content.res.AssetManager
import org.json.JSONArray

internal fun loadChannels(assets: AssetManager): List<Channel> {
    val json = assets.open("canais.json").bufferedReader().use { it.readText() }
    val entries = JSONArray(json)
    return buildList {
        for (index in 0 until entries.length()) {
            val entry = entries.getJSONObject(index)
            if (entry.getString("status") == "pendente") continue

            val category = entry.getString("category")
            add(
                Channel(
                    id = entry.getString("id"),
                    name = entry.getString("name"),
                    category = Category.entries.first { it.key == category },
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
