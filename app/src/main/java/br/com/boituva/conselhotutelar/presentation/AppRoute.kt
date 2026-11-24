package br.com.boituva.conselhotutelar.presentation

internal sealed interface AppRoute {
    val key: String

    enum class Tab(override val key: String) : AppRoute {
        HOME("inicio"),
        RIGHTS("direitos"),
        NETWORK("rede"),
    }

    data object Help : AppRoute { override val key = "ajuda" }
    data object ReportHelp : AppRoute { override val key = "ajuda/suspeita" }
    data object About : AppRoute { override val key = "sobre" }
    data class Article(val id: String) : AppRoute { override val key = "artigo/$id" }
    data class Service(val id: String) : AppRoute { override val key = "servico/$id" }
    data class Support(val id: String) : AppRoute { override val key = "apoio/$id" }

    companion object {
        fun restore(key: String): AppRoute? = when (key) {
            Tab.HOME.key -> Tab.HOME
            Tab.RIGHTS.key -> Tab.RIGHTS
            Tab.NETWORK.key -> Tab.NETWORK
            Help.key -> Help
            ReportHelp.key -> ReportHelp
            About.key -> About
            else -> {
                val id = key.substringAfter('/', missingDelimiterValue = "")
                if (id.isBlank()) null else when (key.substringBefore('/')) {
                    "artigo" -> Article(id)
                    "servico" -> Service(id)
                    "apoio" -> Support(id)
                    else -> null
                }
            }
        }
    }
}
