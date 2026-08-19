package com.kanarek.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReaderFeedSnapshotTest {
    @Test
    fun partialFailureUpdatesOneFeedAndKeepsOtherFeedsFreshness() {
        val previous =
            ReaderFeedSnapshot(
                itemsByFeed =
                    mapOf(
                        FEED_A to listOf(item("old-a", 1L)),
                        FEED_B to listOf(item("old-b", 2L)),
                    ),
                lastUpdatedMillis = 5L,
                updatedAtByFeed = mapOf(FEED_A to 5L, FEED_B to 4L),
            )

        val outcome =
            mergeReaderFeedSnapshot(
                previous = previous,
                activeFeeds = listOf(FEED_A, FEED_B),
                results =
                    listOf(
                        ReaderFeedResult(FEED_A, listOf(item("new-a", 3L)), successful = true),
                        ReaderFeedResult(FEED_B, emptyList(), successful = false),
                    ),
                nowMillis = 10L,
            )

        assertEquals(listOf("new-a"), outcome.snapshot?.itemsByFeed?.get(FEED_A)?.map(NewsItem::title))
        assertEquals(listOf("old-b"), outcome.snapshot?.itemsByFeed?.get(FEED_B)?.map(NewsItem::title))
        assertEquals(mapOf(FEED_A to 10L, FEED_B to 4L), outcome.snapshot?.updatedAtByFeed)
        assertFalse(outcome.shouldRetry)
    }

    @Test
    fun unrequestedRetainedFeedSurvivesAnotherFeedsRefresh() {
        val previous =
            ReaderFeedSnapshot(
                itemsByFeed =
                    mapOf(
                        FEED_A to listOf(item("old-a", 1L)),
                        FEED_B to listOf(item("old-b", 1L)),
                    ),
                lastUpdatedMillis = 5L,
                updatedAtByFeed = mapOf(FEED_A to 5L, FEED_B to 4L),
            )

        val outcome =
            mergeReaderFeedSnapshot(
                previous = previous,
                activeFeeds = listOf(FEED_A, FEED_B),
                results = listOf(ReaderFeedResult(FEED_A, listOf(item("new-a", 2L)), true)),
                nowMillis = 10L,
            )

        assertEquals(listOf("new-a"), outcome.snapshot?.itemsByFeed?.get(FEED_A)?.map(NewsItem::title))
        assertEquals(listOf("old-b"), outcome.snapshot?.itemsByFeed?.get(FEED_B)?.map(NewsItem::title))
        assertEquals(4L, outcome.snapshot?.updatedAtByFeed?.get(FEED_B))
    }

    @Test
    fun totalFailureKeepsSnapshotAndRequestsRetry() {
        val previous = ReaderFeedSnapshot(mapOf(FEED_A to listOf(item("old", 1L))), 5L)

        val outcome =
            mergeReaderFeedSnapshot(
                previous = previous,
                activeFeeds = listOf(FEED_A),
                results = listOf(ReaderFeedResult(FEED_A, emptyList(), successful = false)),
                nowMillis = 10L,
            )

        assertEquals(previous, outcome.snapshot)
        assertTrue(outcome.shouldRetry)
    }

    @Test
    fun emptySuccessKeepsStoriesButRefreshesFeedTimestamp() {
        val previous = ReaderFeedSnapshot(mapOf(FEED_A to listOf(item("old", 1L))), 5L)

        val outcome =
            mergeReaderFeedSnapshot(
                previous = previous,
                activeFeeds = listOf(FEED_A),
                results = listOf(ReaderFeedResult(FEED_A, emptyList(), successful = true)),
                nowMillis = 10L,
            )

        assertEquals(previous.itemsByFeed, outcome.snapshot?.itemsByFeed)
        assertEquals(10L, outcome.snapshot?.updatedAtByFeed?.get(FEED_A))
        assertFalse(outcome.shouldRetry)
    }

    @Test
    fun freshnessIsCalculatedPerFeed() {
        val snapshot =
            ReaderFeedSnapshot(
                itemsByFeed =
                    mapOf(
                        FEED_A to listOf(item("a", 1L)),
                        FEED_B to listOf(item("b", 1L)),
                    ),
                lastUpdatedMillis = 100L,
                updatedAtByFeed = mapOf(FEED_A to 100L, FEED_B to 40L),
            )

        assertEquals(
            setOf(FEED_A),
            freshReaderFeeds(
                snapshot = snapshot,
                feeds = listOf(FEED_A, FEED_B),
                nowMillis = 120L,
                maxAgeMillis = 50L,
            ),
        )
        assertTrue(
            freshReaderFeeds(
                snapshot = snapshot,
                feeds = listOf(FEED_A),
                nowMillis = 120L,
                maxAgeMillis = 0L,
            ).isEmpty(),
        )
    }

    @Test
    fun readerUsesOnlyConfiguredFeedsAndAppliesSourceCap() {
        val snapshot =
            ReaderFeedSnapshot(
                itemsByFeed =
                    mapOf(
                        FEED_A to listOf(item("a1", 3L), item("a2", 2L)),
                        FEED_B to listOf(item("b", 1L)),
                    ),
                lastUpdatedMillis = 10L,
            )

        val items = readerItems(snapshot, listOf(FEED_A), perSourceCap = 1, limit = 15)

        assertEquals(listOf("a1"), items.map(NewsItem::title))
    }

    @Test
    fun removedFeedsDisappearFromNewSnapshot() {
        val previous =
            ReaderFeedSnapshot(
                mapOf(
                    FEED_A to listOf(item("a", 1L)),
                    FEED_B to listOf(item("b", 1L)),
                ),
                5L,
            )

        val outcome =
            mergeReaderFeedSnapshot(
                previous = previous,
                activeFeeds = listOf(FEED_A),
                results = emptyList(),
                nowMillis = 10L,
            )

        assertEquals(setOf(FEED_A), outcome.snapshot?.itemsByFeed?.keys)
        assertEquals(setOf(FEED_A), outcome.snapshot?.updatedAtByFeed?.keys)
    }

    @Test
    fun feedUrlsNormalizeWhitespaceAndDuplicates() {
        assertEquals(
            listOf(FEED_A, FEED_B),
            listOf(" $FEED_A ", "", FEED_B, FEED_A).normalizeFeedUrls(),
        )
    }

    private fun item(
        title: String,
        publishedAtMillis: Long,
    ): NewsItem =
        NewsItem(
            title = title,
            link = "https://example.com/$title",
            summary = "Summary: ~ | unicode żółć",
            imageUrl = null,
            source = "Example",
            publishedAtMillis = publishedAtMillis,
        )

    companion object {
        private const val FEED_A = "https://example.com/a.xml"
        private const val FEED_B = "https://example.com/b.xml"
    }
}
