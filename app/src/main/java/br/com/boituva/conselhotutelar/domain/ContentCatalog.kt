package br.com.boituva.conselhotutelar.domain

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
    val channels: List<Channel> = emptyList(),
    val articles: List<GuideArticle> = emptyList(),
    val questions: List<GuideQuestion> = emptyList(),
    val services: List<NetworkService> = emptyList(),
    val supportTopics: List<SupportTopic> = emptyList(),
    val campaigns: List<Campaign> = emptyList(),
    val editorialChannel: EditorialChannel? = null,
) {
    fun article(id: String): GuideArticle? = articles.firstOrNull { it.id == id }
}

internal const val COUNCIL_ARTICLE_ID = "conselho_tutelar"
internal const val REPORT_ARTICLE_ID = "comunicar_suspeita"
