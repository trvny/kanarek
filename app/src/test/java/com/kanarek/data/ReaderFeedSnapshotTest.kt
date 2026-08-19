package com.kanarek.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderFeedSnapshotTest {
    @Test
    fun refreshIntervalsNormalizeAndChooseScheduleAction() {
        assertEquals(ReaderBackgroundRefresh.OFF, ReaderBackgroundRefresh.normalize(17))
        assertEquals(
            ReaderRefreshScheduleAction.CANCEL,
            ReaderBackgroundRefresh.scheduleAction(ReaderBackgroundRefresh.OFF),
        )
        ReaderBackgroundRefresh.options.drop(1).forEach { minutes ->
            assertEquals(minutes, ReaderBackgroundRefresh.normalize(minutes))
            assertEquals(
                ReaderRefreshScheduleAction.SCHEDULE,
                ReaderBackgroundRefresh.scheduleAction(minutes),
            )
        }
    }

    @Test
    fun codecPreservesFeedBucketsAndPerFeedFreshness() {
        val snapshot =
            ReaderFeedSnapshot(
                itemsByFeed =
                    linkedMapOf(
                        FEED_A to listOf(item("A", 2L)),
                        FEED_B to emptyList(),
                    ),
                lastUpdatedMillis = 20L,
                updatedAtByFeed = mapOf(FEED_A to 20L, FEED_B to 10L),
            )

        assertEquals(snapshot, ReaderFeedSnapshotCodec.decode(ReaderFeedSnapshotCodec.encode(snapshot)))
    }

    @Test
    fun legacyCodecUsesGlobalTimestampForEveryFeed() {
        val snapshot =
            ReaderFeedSnapshot(
                itemsByFeed =
                    linkedMapOf(
                        FEED_A to listOf(item("A", 2L)),
                        FEED_B to emptyList(),
                    ),
                lastUpdatedMillis = 20L,
                updatedAtByFeed = mapOf(FEED_A to 20L, FEED_B to 10L),
            )
        val legacy =
            ReaderFeedSnapshotCodec
                .encode(snapshot)
                .lineSequence()
                .mapIndexed { index, line ->
                    if (index == 0) {
                        line.replaceFirst("2|", "1|")
                    } else {
                        val fields = line.split('|', limit = 3)
                        "${fields[0]}|${fields[2]}"
                    }
                }.joinToString("\n")

        val decoded = ReaderFeedSnapshotCodec.decode(legacy)

        assertEquals(mapOf(FEED_A to 20L, FEED_B to 20L), decoded?.updatedAtByFeed)
    }

    @Test
    fun corruptSnapshotIsIgnored() {
        assertNull(ReaderFeedSnapshotCodec.decode("broken"))
    }

    @Test
    fun cachedFeedMakesNotificationResultRecordableWithoutNetworkSuccess() {
        val cached =
            ReaderFeedSyncResult(
                items = listOf(item("cached", 1L)),
                recordableItems = listOf(item("cached", 1L)),
                successfulFeeds = emptySet(),
                cachedFeeds = setOf(FEED_A),
                failedFeeds = setOf(FEED_B),
            )
        val failed =
            ReaderFeedSyncResult(
                items = listOf(item("old", 1L)),
                recordableItems = emptyList(),
                successfulFeeds = emptySet(),
                cachedFeeds = emptySet(),
                failedFeeds = setOf(FEED_A),
            )

        assertTrue(cached.canRecord)
        assertFalse(cached.shouldRetry)
        assertFalse(failed.canRecord)
        assertTrue(failed.shouldRetry)
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
