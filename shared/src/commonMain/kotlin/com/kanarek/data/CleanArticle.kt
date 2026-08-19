package com.kanarek.data

/** Plain-text article body returned by the reader backend and safe to share across platforms. */
data class CleanArticle(
    val title: String,
    val author: String?,
    val imageUrl: String?,
    val content: String,
    val wordCount: Int,
)
