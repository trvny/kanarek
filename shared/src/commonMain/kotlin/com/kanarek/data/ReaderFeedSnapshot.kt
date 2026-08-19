package com.kanarek.data

data class ReaderFeedSnapshot(
    val itemsByFeed: Map<String, List<NewsItem>>,
    val lastUpdatedMillis: Long,
    val updatedAtByFeed: Map<String, Long> =
        itemsByFeed.keys.associateWith { lastUpdatedMillis },
)

data class ReaderFeedResult(
    val feed: String,
    val items: List<NewsItem>,
    val successful: Boolean,
)

data class ReaderFeedMergeOutcome(
    val snapshot: ReaderFeedSnapshot?,
    val successfulFeeds: Set<String>,
    val failedFeeds: Set<String>,
) {
    val shouldRetry: Boolean
        get() = successfulFeeds.isEmpty() && failedFeeds.isNotEmpty()
}

fun mergeReaderFeedSnapshot(
    previous: ReaderFeedSnapshot?,
    activeFeeds: List<String>,
    results: List<ReaderFeedResult>,
    nowMillis: Long,
): ReaderFeedMergeOutcome {
    val feeds = activeFeeds.normalizeFeedUrls()
    if (feeds.isEmpty()) return ReaderFeedMergeOutcome(null, emptySet(), emptySet())

    val active = feeds.toSet()
    val byFeed = results.filter { it.feed in active }.associateBy(ReaderFeedResult::feed)
    val successful =
        byFeed.values
            .filter(ReaderFeedResult::successful)
            .mapTo(linkedSetOf(), ReaderFeedResult::feed)
    val failed =
        byFeed.values
            .filterNot(ReaderFeedResult::successful)
            .mapTo(linkedSetOf(), ReaderFeedResult::feed)
    val mergedItems = linkedMapOf<String, List<NewsItem>>()
    val mergedUpdatedAt = linkedMapOf<String, Long>()

    feeds.forEach { feed ->
        val result = byFeed[feed]
        when {
            result?.successful == true -> {
                mergedItems[feed] =
                    result.items
                        .takeIf { it.isNotEmpty() }
                        ?.distinctBy { it.link.trim() }
                        ?: previous?.itemsByFeed?.get(feed).orEmpty()
                mergedUpdatedAt[feed] = nowMillis
            }

            previous?.itemsByFeed?.containsKey(feed) == true -> {
                mergedItems[feed] = previous.itemsByFeed.getValue(feed)
                mergedUpdatedAt[feed] =
                    previous.updatedAtByFeed[feed] ?: previous.lastUpdatedMillis
            }
        }
    }
    val updated = mergedUpdatedAt.values.maxOrNull() ?: previous?.lastUpdatedMillis
    return ReaderFeedMergeOutcome(
        snapshot =
            updated?.let {
                ReaderFeedSnapshot(
                    itemsByFeed = mergedItems,
                    lastUpdatedMillis = it,
                    updatedAtByFeed = mergedUpdatedAt,
                )
            },
        successfulFeeds = successful,
        failedFeeds = failed,
    )
}

fun freshReaderFeeds(
    snapshot: ReaderFeedSnapshot?,
    feeds: List<String>,
    nowMillis: Long,
    maxAgeMillis: Long,
): Set<String> {
    if (snapshot == null || maxAgeMillis <= 0L) return emptySet()
    return feeds
        .normalizeFeedUrls()
        .filterTo(linkedSetOf()) { feed ->
            if (!snapshot.itemsByFeed.containsKey(feed)) return@filterTo false
            val updated = snapshot.updatedAtByFeed[feed] ?: snapshot.lastUpdatedMillis
            (nowMillis - updated).coerceAtLeast(0L) < maxAgeMillis
        }
}

fun readerItems(
    snapshot: ReaderFeedSnapshot?,
    feeds: List<String>,
    perSourceCap: Int,
    limit: Int,
): List<NewsItem> =
    NewsMerge
        .capPerSource(
            feeds
                .normalizeFeedUrls()
                .flatMap { snapshot?.itemsByFeed?.get(it).orEmpty() }
                .distinctBy { it.link.trim() },
            perSourceCap,
        ).take(limit)

fun List<String>.normalizeFeedUrls(): List<String> =
    asSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .toList()
