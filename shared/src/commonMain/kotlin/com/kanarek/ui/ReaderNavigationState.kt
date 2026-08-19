package com.kanarek.ui

import com.kanarek.data.NewsItem

enum class ReaderRoute {
    READER,
    ARTICLE,
    SETTINGS,
    STORAGE,
    NOTIFICATIONS,
}

data class ReaderNavigationState(
    val route: ReaderRoute = ReaderRoute.READER,
    val selectedArticle: NewsItem? = null,
) {
    fun openArticle(item: NewsItem): ReaderNavigationState =
        copy(route = ReaderRoute.ARTICLE, selectedArticle = item)

    fun open(route: ReaderRoute): ReaderNavigationState =
        copy(route = route, selectedArticle = null)

    fun back(): ReaderNavigationState =
        when (route) {
            ReaderRoute.STORAGE,
            ReaderRoute.NOTIFICATIONS,
            -> copy(route = ReaderRoute.SETTINGS, selectedArticle = null)

            ReaderRoute.READER -> this
            ReaderRoute.ARTICLE,
            ReaderRoute.SETTINGS,
            -> ReaderNavigationState()
        }
}
