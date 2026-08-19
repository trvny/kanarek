package com.kanarek.ui

import com.kanarek.data.ArticleListFilter
import com.kanarek.data.NewsItem

data class ReaderFilterState(
    val filter: ArticleListFilter = ArticleListFilter.ALL,
    val query: String = "",
    val sources: Set<String> = emptySet(),
) {
    val hasSearchFilters: Boolean
        get() = query.isNotBlank() || sources.isNotEmpty()

    fun toggleSource(source: String): ReaderFilterState {
        val normalized = source.trim()
        if (normalized.isEmpty()) return this
        val selected = sources.any { it.equals(normalized, ignoreCase = true) }
        val next =
            if (selected) {
                sources.filterNot { it.equals(normalized, ignoreCase = true) }.toSet()
            } else {
                sources + normalized
            }
        return copy(sources = next)
    }
}

fun readerSourceOptions(
    feedItems: List<NewsItem>,
    savedArticles: List<NewsItem>,
    selectedSources: Set<String>,
): List<String> =
    (feedItems.map(NewsItem::source) + savedArticles.map(NewsItem::source) + selectedSources)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }
