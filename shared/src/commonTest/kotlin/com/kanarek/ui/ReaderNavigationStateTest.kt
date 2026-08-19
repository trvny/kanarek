package com.kanarek.ui

import com.kanarek.data.NewsItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReaderNavigationStateTest {
    @Test
    fun articleBackReturnsToReaderAndClearsSelection() {
        val article = item("https://example.com/article")

        val opened = ReaderNavigationState().openArticle(article)
        val returned = opened.back()

        assertEquals(ReaderRoute.ARTICLE, opened.route)
        assertEquals(article, opened.selectedArticle)
        assertEquals(ReaderRoute.READER, returned.route)
        assertNull(returned.selectedArticle)
    }

    @Test
    fun nestedSettingsPagesBackToSettingsBeforeReader() {
        val storage = ReaderNavigationState().open(ReaderRoute.STORAGE)
        val notifications = ReaderNavigationState().open(ReaderRoute.NOTIFICATIONS)

        assertEquals(ReaderRoute.SETTINGS, storage.back().route)
        assertEquals(ReaderRoute.SETTINGS, notifications.back().route)
        assertEquals(ReaderRoute.READER, storage.back().back().route)
    }

    private fun item(link: String): NewsItem =
        NewsItem(
            title = link.substringAfterLast('/'),
            link = link,
            summary = "",
            imageUrl = null,
            source = "Example",
            publishedAtMillis = null,
        )
}
