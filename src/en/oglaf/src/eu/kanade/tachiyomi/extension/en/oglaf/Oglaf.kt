package eu.kanade.tachiyomi.extension.en.smbc

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale

class SMBC : ParsedHttpSource() {

    override val name = "SMBC Comics"

    override val baseUrl = "https://www.smbc-comics.com"

    override val lang = "en"

    override val supportsLatest = false

    override fun fetchPopularManga(page: Int): Observable<MangasPage> {
        val manga = SManga.create().apply {
            title = "SMBC Comics"
            artist = "Zach Weinersmith"
            author = "Zach Weinersmith"
            status = SManga.ONGOING
            url = "/archive"
            description = "Saturday Morning Breakfast Cereal (SMBC) is a daily comic strip about science, philosophy, relationships, and other weighty matters."
            thumbnail_url = "https://www.smbc-comics.com/comics/20080101.gif" // Using a default comic as thumbnail
        }

        return Observable.just(MangasPage(arrayListOf(manga), false))
    }

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> = Observable.just(MangasPage(emptyList(), false))

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> = Observable.just(manga)

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()
        val elements = document.select(chapterListSelector())
        val chapters = mutableListOf<SChapter>()

        for (element in elements) {
            val chapter = chapterFromElement(element)
            chapters.add(chapter)
        }

        // Reverse the list so newest chapters come first, and set chapter numbers
        return chapters.asReversed().mapIndexed { i, chapter ->
            chapter.apply { chapter_number = chapters.size.toFloat() - i }
        }
    }

    override fun chapterListSelector() = "div#archives a"

    override fun chapterFromElement(element: Element): SChapter {
        val urlText = element.attr("href")
        val dateText = element.text().trim()

        return SChapter.create().apply {
            url = urlText
            name = "Comic for $dateText"

            // Try to parse the date if possible
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                date_upload = dateFormat.parse(dateText)?.time ?: 0L
            } catch (e: Exception) {
                // If date parsing fails, just use 0
                date_upload = 0L
            }
        }
    }

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()

        // Main comic image
        val mainImage = document.select("img#cc-comic").first()
        if (mainImage != null) {
            pages.add(Page(0, "", mainImage.attr("abs:src")))
        }

        // After-comic/bonus panel if exists
        val bonusPanel = document.select("div#aftercomic img").first()
        if (bonusPanel != null) {
            pages.add(Page(1, "", bonusPanel.attr("abs:src")))
        }

        return pages
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException()

    override fun popularMangaSelector(): String = throw UnsupportedOperationException()

    override fun searchMangaFromElement(element: Element): SManga = throw UnsupportedOperationException()

    override fun searchMangaNextPageSelector(): String? = throw UnsupportedOperationException()

    override fun searchMangaSelector(): String = throw UnsupportedOperationException()

    override fun popularMangaRequest(page: Int): Request = throw UnsupportedOperationException()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = throw UnsupportedOperationException()

    override fun popularMangaNextPageSelector(): String? = throw UnsupportedOperationException()

    override fun popularMangaFromElement(element: Element): SManga = throw UnsupportedOperationException()

    override fun mangaDetailsParse(document: Document): SManga = throw UnsupportedOperationException()

    override fun latestUpdatesNextPageSelector(): String? = throw UnsupportedOperationException()

    override fun latestUpdatesFromElement(element: Element): SManga = throw UnsupportedOperationException()

    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException()

    override fun latestUpdatesSelector(): String = throw UnsupportedOperationException()
}
