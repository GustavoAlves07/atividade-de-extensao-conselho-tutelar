package br.com.boituva.conselhotutelar.data

import android.content.res.AssetManager
import br.com.boituva.conselhotutelar.domain.ArticleSection
import br.com.boituva.conselhotutelar.domain.Campaign
import br.com.boituva.conselhotutelar.domain.Channel
import br.com.boituva.conselhotutelar.domain.ContentCatalog
import br.com.boituva.conselhotutelar.domain.EditorialChannel
import br.com.boituva.conselhotutelar.domain.GuideArticle
import br.com.boituva.conselhotutelar.domain.GuideQuestion
import br.com.boituva.conselhotutelar.domain.NetworkService
import br.com.boituva.conselhotutelar.domain.References
import br.com.boituva.conselhotutelar.domain.Source
import br.com.boituva.conselhotutelar.domain.SupportTopic
import org.json.JSONObject

internal fun readContent(assets: AssetManager, channels: List<Channel>): ContentCatalog {
    val guide = assets.readContent("guia.json")
    val network = assets.readContent("rede_protecao.json")
    val editorial = assets.readContent("editorial.json")

    return ContentCatalog(
        channels = channels,
        articles = guide.records("articles").filter { it.isPublished() }.map {
            GuideArticle(
                id = it.getString("id"),
                title = it.getString("title"),
                summary = it.getString("summary"),
                sections = it.records("sections").map { section ->
                    ArticleSection(section.getString("heading"), section.getString("body"))
                },
                channelIds = it.ids("channelIds"),
                serviceIds = it.ids("serviceIds"),
                references = it.references(),
            )
        },
        questions = guide.records("faq").filter { it.isPublished() }.map {
            GuideQuestion(
                id = it.getString("id"),
                question = it.getString("question"),
                answer = it.getString("answer"),
                articleId = it.optionalText("articleId"),
                references = it.references(),
            )
        },
        services = network.records("services").filter { it.isPublished() }.map {
            NetworkService(
                id = it.getString("id"),
                name = it.getString("name"),
                category = it.getString("category"),
                summary = it.getString("summary"),
                channelIds = it.ids("channelIds"),
                phone = it.optionalText("phone"),
                whatsapp = it.optionalText("whatsapp"),
                website = it.optionalText("website"),
                address = it.optJSONObject("address")?.formattedAddress(),
                openingHours = it.optionalText("openingHours"),
                coverage = it.optionalText("coverage"),
                accessConditions = it.optionalText("accessConditions"),
                accessibility = it.optionalText("accessibility"),
                references = it.references(),
            )
        },
        supportTopics = network.records("supportTopics").filter { it.isPublished() }.map {
            SupportTopic(
                id = it.getString("id"),
                title = it.getString("title"),
                summary = it.getString("summary"),
                serviceIds = it.ids("serviceIds"),
                references = it.references(),
            )
        },
        campaigns = editorial.records("campaigns").filter { it.isApproved() }.map {
            Campaign(
                title = it.getString("title"),
                summary = it.getString("summary"),
                materialUrl = it.getString("materialUrl"),
                startsOn = it.optionalText("startsOn"),
                endsOn = it.optionalText("endsOn"),
                references = it.references(),
            )
        },
        editorialChannel = editorial.optJSONObject("corrections")?.takeIf { it.isApproved() }?.let {
            EditorialChannel(it.getString("url"), it.references())
        },
    )
}

private fun AssetManager.readContent(filename: String): JSONObject {
    val raw = open(filename).bufferedReader().use { it.readText() }
    return JSONObject(raw).also { require(it.getInt("schemaVersion") == 1) }
}

private fun JSONObject.records(key: String): List<JSONObject> {
    val array = getJSONArray(key)
    return List(array.length()) { array.getJSONObject(it) }
}

private fun JSONObject.ids(key: String): List<String> {
    val array = optJSONArray(key) ?: return emptyList()
    return List(array.length()) { array.getString(it) }
}

private fun JSONObject.optionalText(key: String): String? =
    if (isNull(key)) null else getString(key).takeIf { it.isNotBlank() }

private fun JSONObject.isApproved(): Boolean = getString("status") == "validado_com_orgao"

private fun JSONObject.isPublished(): Boolean =
    getString("status") == "verificado_em_fonte_publica" || isApproved()

private fun JSONObject.references() = References(
    sources = records("sources").map { Source(it.getString("name"), it.getString("url")) },
    checkedOn = getString("checkedOn"),
)

private fun JSONObject.formattedAddress(): String {
    val streetLine = listOfNotNull(getString("street"), optionalText("number")).joinToString(", ")
    val cityLine = "${getString("city")} – ${getString("state")}"
    return listOfNotNull(streetLine, optionalText("district"), cityLine).joinToString(", ")
}
