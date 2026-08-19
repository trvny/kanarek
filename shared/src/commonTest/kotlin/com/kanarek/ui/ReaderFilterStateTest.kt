package com.kanarek.ui

import com.kanarek.data.ArticleListFilter
import com.kanarek.data.NewsItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReaderFilterStateTest {
    @Test
    fun sourceToggleIsCaseInsensitiveAndPreservesCanonicalValue() {
        val selected = ReaderFilterState().toggleSource(" Example ")
        val removed = selected.toggleSource("example")

        assertEquals(setOf("Example"), selected.sources)
        assertTrue(selected.hasSearchFilters)
        assertTrue(removed.sources.isEmpty())
        assertFalse(removed.hasSearchFilters)
    }

    @Test
    fun queryAndArticleFilterRemainIndependent() {
        val state = ReaderFilterState(filter = ArticleListFilter.SAVED, query = "  space  ")

        assertEquals(ArticleListFilter.SAVED, state.filter)
        assertTrue(state.hasSearchFilters)
        assertEquals("  space  ", state.query)
    }

    @Test
    fun sourceOptionsMergeFeedSavedAndSelectedWithoutCaseDuplicates() {
        val options =
            readerSourceOptions(
                feedItems = listOf(item("https://example.com/1", source = "PAP")),
                savedArticles = listOf(item("https://example.com/2", source = "pap")),
                selectedSources = setOf("Reuters"),
            )

        assertEquals(listOf("PAP", "Reuters"), options)
    }

    private fun item(
        link: String,
        source: String = "Example",
    ): NewsItem =
        NewsItem(
            title = link.substringAfterLast('/'),
            link = link,
            summary = "",
            imageUrl = null,
            source = source,
            publishedAtMillis = null,
        )
}
