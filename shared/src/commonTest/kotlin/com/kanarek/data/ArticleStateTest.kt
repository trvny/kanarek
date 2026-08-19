package com.kanarek.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArticleStateTest {
    @Test
    fun filtersHideHiddenItemsAndKeepSavedSnapshots() {
        val unread = item(link = "https://example.com/unread", publishedAtMillis = 20L)
        val read = item(link = "https://example.com/read", publishedAtMillis = 10L)
        val savedOld = item(link = "https://example.com/saved", publishedAtMillis = 5L)
        val hidden = item(link = "https://example.com/hidden", publishedAtMillis = 30L)
        val state =
            ArticleState(
                readIds = setOf(ArticleStates.id(read)),
                savedArticles = listOf(savedOld, read),
                hiddenIds = setOf(ArticleStates.id(hidden)),
            )
        val feed = listOf(hidden, unread, read, unread)

        assertEquals(listOf(unread, read), ArticleStates.visible(feed, state, ArticleListFilter.ALL))
        assertEquals(listOf(unread), ArticleStates.visible(feed, state, ArticleListFilter.UNREAD))
        assertEquals(listOf(read, savedOld), ArticleStates.visible(feed, state, ArticleListFilter.SAVED))
        assertTrue(state.isRead(read))
        assertFalse(state.isRead(unread))
        assertTrue(state.isSaved(savedOld))
    }

    @Test
    fun searchMatchesTitleSourceAndSummary() {
        val titleMatch = item(link = "https://example.com/title", title = "Mars mission")
        val sourceMatch = item(link = "https://example.com/source", source = "Space Daily")
        val summaryMatch = item(link = "https://example.com/summary", summary = "A new telescope launched")
        val other = item(link = "https://example.com/other")
        val feed = listOf(titleMatch, sourceMatch, summaryMatch, other)

        assertEquals(listOf(titleMatch), ArticleStates.visible(feed, ArticleState(), ArticleListFilter.ALL, query = "MARS"))
        assertEquals(listOf(sourceMatch), ArticleStates.visible(feed, ArticleState(), ArticleListFilter.ALL, query = "space daily"))
        assertEquals(listOf(summaryMatch), ArticleStates.visible(feed, ArticleState(), ArticleListFilter.ALL, query = "TELESCOPE"))
    }

    @Test
    fun sourceAndTextFiltersAlsoApplyToSavedSnapshots() {
        val savedMatch =
            item(
                link = "https://example.com/saved-match",
                title = "Local derby",
                source = "Sport News",
                publishedAtMillis = 20L,
            )
        val savedWrongSource =
            item(
                link = "https://example.com/saved-other",
                title = "Local derby",
                source = "City News",
                publishedAtMillis = 10L,
            )
        val liveOnly =
            item(
                link = "https://example.com/live",
                title = "Local derby",
                source = "Sport News",
            )
        val state = ArticleState(savedArticles = listOf(savedWrongSource, savedMatch))

        assertEquals(
            listOf(savedMatch),
            ArticleStates.visible(
                feedItems = listOf(liveOnly),
                state = state,
                filter = ArticleListFilter.SAVED,
                query = "derby",
                sources = setOf(" sport NEWS "),
            ),
        )
    }

    @Test
    fun articleIdTrimsFeedWhitespace() {
        val article = item(link = "  https://example.com/story  ")
        assertEquals("https://example.com/story", ArticleStates.id(article))
    }

    @Test
    fun offlineStorageSizeMatchesPersistedBase64Shape() {
        val offline =
            OfflineArticleContent(
                title = "A",
                author = null,
                imageUrl = null,
                content = "abc",
                wordCount = 1,
                storedAtMillis = 20L,
            )
        val state = ArticleState(offlineArticles = mapOf("x" to offline))

        assertEquals(9L, offlineArticleStorageBytes(offline))
        assertEquals(9L, state.offlineArticleBytes)
    }

    private fun item(
        link: String,
        publishedAtMillis: Long? = null,
        title: String = "Title",
        summary: String = "Summary",
        source: String = "Source",
    ): NewsItem =
        NewsItem(
            title = title,
            link = link,
            summary = summary,
            imageUrl = null,
            source = source,
            publishedAtMillis = publishedAtMillis,
        )
}
