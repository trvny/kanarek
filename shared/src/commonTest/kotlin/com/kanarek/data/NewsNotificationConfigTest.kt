package com.kanarek.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewsNotificationConfigTest {
    @Test
    fun allSourcesSelectionTracksAddedAndRemovedConfiguredFeeds() {
        val config =
            NewsNotificationConfig(
                enabled = true,
                selectedFeeds = listOf("https://feeds.example/a", "https://feeds.example/b"),
                configuredFeeds = listOf("https://feeds.example/a", "https://feeds.example/b"),
            )

        val reconciled =
            config.reconciledWith(
                listOf("https://feeds.example/b", "https://feeds.example/c"),
            )

        assertEquals(
            listOf("https://feeds.example/b", "https://feeds.example/c"),
            reconciled.selectedFeeds,
        )
        assertEquals(reconciled.selectedFeeds, reconciled.configuredFeeds)
    }

    @Test
    fun intentionalSourceSubsetDoesNotSelectNewFeeds() {
        val config =
            NewsNotificationConfig(
                enabled = true,
                selectedFeeds = listOf("https://feeds.example/a"),
                configuredFeeds = listOf("https://feeds.example/a", "https://feeds.example/b"),
            )

        val reconciled =
            config.reconciledWith(
                listOf(
                    "https://feeds.example/a",
                    "https://feeds.example/b",
                    "https://feeds.example/c",
                ),
            )

        assertEquals(listOf("https://feeds.example/a"), reconciled.selectedFeeds)
    }

    @Test
    fun deletedOnlySelectionFallsBackToConfiguredFeeds() {
        val config =
            NewsNotificationConfig(
                enabled = true,
                selectedFeeds = listOf("https://feeds.example/deleted"),
                configuredFeeds =
                    listOf(
                        "https://feeds.example/deleted",
                        "https://feeds.example/also-deleted",
                    ),
            )

        val reconciled =
            config.reconciledWith(listOf("https://feeds.example/current"))

        assertEquals(listOf("https://feeds.example/current"), reconciled.selectedFeeds)
    }

    @Test
    fun normalizationTrimsFeedsAndClampsQuietMinutes() {
        val normalized =
            NewsNotificationConfig(
                selectedFeeds = listOf(" https://feeds.example/a ", "", "https://feeds.example/a"),
                configuredFeeds = listOf(" https://feeds.example/b "),
                quietStartMinute = -1,
                quietEndMinute = NewsNotificationConfig.MINUTES_PER_DAY + 5,
            ).normalized()

        assertEquals(listOf("https://feeds.example/a"), normalized.selectedFeeds)
        assertEquals(listOf("https://feeds.example/b"), normalized.configuredFeeds)
        assertEquals(0, normalized.quietStartMinute)
        assertEquals(NewsNotificationConfig.MINUTES_PER_DAY - 1, normalized.quietEndMinute)
    }

    @Test
    fun quietHoursWrapAcrossMidnight() {
        assertTrue(NewsNotificationSchedule.isQuietTime(23 * 60, 22 * 60, 7 * 60))
        assertTrue(NewsNotificationSchedule.isQuietTime(6 * 60 + 59, 22 * 60, 7 * 60))
        assertFalse(NewsNotificationSchedule.isQuietTime(7 * 60, 22 * 60, 7 * 60))
        assertFalse(NewsNotificationSchedule.isQuietTime(12 * 60, 22 * 60, 7 * 60))
        assertFalse(NewsNotificationSchedule.isQuietTime(12 * 60, 12 * 60, 12 * 60))
    }
}
