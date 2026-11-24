package br.com.boituva.conselhotutelar

import android.content.res.AssetManager
import org.json.JSONObject

internal data class Source(val name: String, val url: String)

internal data class References(val sources: List<Source>, val checkedOn: String)

internal data class GuideArticle(
    val id: String,
    val title: String,
    val summary: String,
    val sections: List<ArticleSection>,
    val channelIds: List<String>,
    val serviceIds: List<String>,
    val references: References,
)

internal data class ArticleSection(val heading: String, val body: String)

internal data class GuideQuestion(
    val id: String,
    val question: String,
    val answer: String,
    val articleId: String?,
    val references: References,
)

internal data class NetworkService(
    val id: String,
    val name: String,
    val category: String,
    val summary: String,
    val channelIds: List<String>,
    val phone: String?,
    val whatsapp: String?,
    val website: String?,
    val address: String?,
    val openingHours: String?,
    val coverage: String?,
    val accessConditions: String?,
    val accessibility: String?,
    val references: References,
)

internal data class SupportTopic(
    val id: String,
    val title: String,
    val summary: String,
    val serviceIds: List<String>,
    val references: References,
)

internal data class Campaign(
    val title: String,
    val summary: String,
    val materialUrl: String,
    val startsOn: String?,
    val endsOn: String?,
    val references: References,
) {
    fun isActiveOn(day: String): Boolean =
        (startsOn == null || startsOn <= day) && (endsOn == null || day <= endsOn)
}

internal data class EditorialChannel(val url: String, val references: References)

internal data class ContentCatalog(
    val articles: List<GuideArticle> = emptyList(),
    val questions: List<GuideQuestion> = emptyList(),
    val services: List<NetworkService> = emptyList(),
    val supportTopics: List<SupportTopic> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
    val editorialChannel: EditorialChannel? = null,
)

internal fun loadContent(assets: AssetManager): ContentCatalog {
    val guide = assets.readContent("guia.json")
    val network = assets.readContent("rede_protecao.json")
    val editorial = assets.readContent("editorial.json")

    return ContentCatalog(
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
