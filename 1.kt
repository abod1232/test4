package com.yourname.cloudstreamextractor

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.parse.*
import okhttp3.Request

class TukTukCimaProvider : MainAPI() {
    override val name = "TukTukCima"
    override val mainUrl = "https://tuktukcima.com"
    override val hasQuickSearch = true

    override suspend fun search(query: String): List<SearchResponse> {
        val results = mutableListOf<SearchResponse>()
        val url = "$mainUrl/search?q=${query.replace(" ", "+")}"
        val document = app.get(url).document

        // Example, adjust selectors to actual site
        document.select("div.result-item").forEach { element ->
            val title = element.select("h3.title").text()
            val urlPath = element.select("a").attr("href")
            val poster = element.select("img").attr("src")
            results.add(
                newMovieSearchResponse(
                    title = title,
                    url = urlPath,
                    posterUrl = poster
                )
            )
        }
        return results
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.select("h1.movie-title").text()
        val poster = doc.select("div.poster img").attr("src")
        val plot = doc.select("div.description").text()

        val episodes = mutableListOf<Episode>()
        // Example: If series
        doc.select("ul.episodes li a").forEach { ep ->
            val epTitle = ep.text()
            val epUrl = ep.attr("href")
            episodes.add(Episode(epTitle, epUrl))
        }

        val info = MovieLoadResponse()
        info.title = title
        info.posterUrl = poster
        info.plot = plot
        if (episodes.isNotEmpty()) info.episodes = episodes

        return info
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit
    ): List<ExtractorLink> {
        val links = mutableListOf<ExtractorLink>()
        val doc = app.get(data).document

        // Example selector for video iframe
        val iframeUrl = doc.select("iframe.player").attr("src")

        // Add single link
        links.add(ExtractorLink("Default", iframeUrl, iframeUrl))

        return links
    }
}
