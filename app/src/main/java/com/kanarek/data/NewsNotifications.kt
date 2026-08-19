package com.kanarek.data

import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest

internal data class NewsNotificationDecision(
    val knownIds: Set<String>,
    val newItems: List<NewsItem>,
    val shouldNotify: Boolean,
)

internal data class NewsNotificationSnapshot(
    val currentItems: List<NewsItem>,
    val knownIds: Set<String>,
    val initialized: Boolean,
    val minuteOfDay: Int,
    val config: NewsNotificationConfig,
)

internal object NewsNotificationPolling {
    fun feedBatches(
        feeds: List<String>,
        maxFeedsPerBatch: Int,
    ): List<List<String>> =
        if (maxFeedsPerBatch > 0) {
            feeds.chunked(maxFeedsPerBatch)
        } else {
            emptyList()
        }

    fun combine(
        results: List<NewsFetchResult>,
        limit: Int,
    ): NewsFetchResult {
        val items =
            results
                .asSequence()
                .flatMap { it.items.asSequence() }
                .filter { NewsNotifications.stableId(it).isNotBlank() }
                .distinctBy(NewsNotifications::stableId)
                .sortedByDescending { it.publishedAtMillis ?: 0L }
                .take(limit.coerceAtLeast(0))
                .toList()
        return NewsFetchResult(
            items = items,
            successfulSources = results.sumOf { it.successfulSources },
        )
    }

    fun shouldRecord(result: NewsFetchResult): Boolean = result.successfulSources > 0
}

internal object NewsNotifications {
    fun evaluate(
        snapshot: NewsNotificationSnapshot,
        maxKnownIds: Int = MAX_KNOWN_IDS,
    ): NewsNotificationDecision {
        val current =
            snapshot.currentItems
                .filter { stableId(it).isNotBlank() }
                .distinctBy(::stableId)
        val newItems =
            if (snapshot.initialized) {
                current.filterNot { stableId(it) in snapshot.knownIds }
            } else {
                emptyList()
            }
        val nextKnown = linkedSetOf<String>()
        val limit = maxKnownIds.coerceAtLeast(0)
        sequenceOf(
            current.asSequence().map(::stableId),
            snapshot.knownIds.asSequence().map(String::trim).filter(String::isNotEmpty),
        ).flatten()
            .forEach { id ->
                if (nextKnown.size < limit) nextKnown += id
            }
        val quiet =
            snapshot.config.quietHoursEnabled &&
                NewsNotificationSchedule.isQuietTime(
                    minuteOfDay = snapshot.minuteOfDay,
                    startMinute = snapshot.config.quietStartMinute,
                    endMinute = snapshot.config.quietEndMinute,
                )
        return NewsNotificationDecision(
            knownIds = nextKnown,
            newItems = newItems,
            shouldNotify = newItems.isNotEmpty() && !quiet,
        )
    }

    internal fun stableId(item: NewsItem): String {
        val link = ArticleStates.id(item)
        if (link.isBlank()) return ""
        val digest = MessageDigest.getInstance("SHA-256").digest(link.toByteArray(UTF_8))
        val alphabet = "0123456789abcdef"
        return buildString(digest.size * 2) {
            digest.forEach { byte ->
                val value = byte.toInt() and 0xff
                append(alphabet[value ushr 4])
                append(alphabet[value and 0x0f])
            }
        }
    }

    private const val MAX_KNOWN_IDS = 500
}
