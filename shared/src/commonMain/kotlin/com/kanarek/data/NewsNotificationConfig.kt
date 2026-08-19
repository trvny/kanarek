package com.kanarek.data

data class NewsNotificationConfig(
    val enabled: Boolean = false,
    val selectedFeeds: List<String> = emptyList(),
    /** Feed catalogue seen when [selectedFeeds] was last reconciled. This lets a later feed edit
     * distinguish “follow every configured feed” from an intentional subset. */
    val configuredFeeds: List<String> = emptyList(),
    val quietHoursEnabled: Boolean = true,
    val quietStartMinute: Int = DEFAULT_QUIET_START_MINUTE,
    val quietEndMinute: Int = DEFAULT_QUIET_END_MINUTE,
) {
    fun normalized(): NewsNotificationConfig =
        copy(
            selectedFeeds = normalizeFeeds(selectedFeeds),
            configuredFeeds = normalizeFeeds(configuredFeeds),
            quietStartMinute = quietStartMinute.coerceIn(0, MINUTES_PER_DAY - 1),
            quietEndMinute = quietEndMinute.coerceIn(0, MINUTES_PER_DAY - 1),
        )

    /**
     * Drops deleted feed URLs and carries newly configured feeds forward only when the previous
     * selection represented “all”. An intentional subset stays a subset. If every selected feed
     * was deleted, fall back to the current catalogue instead of silently leaving an enabled
     * worker with nothing to monitor.
     */
    fun reconciledWith(feeds: List<String>): NewsNotificationConfig {
        val current = normalized()
        val available = normalizeFeeds(feeds)
        val availableSet = available.toSet()
        val surviving = current.selectedFeeds.filter { it in availableSet }
        val previouslyFollowedAll =
            current.configuredFeeds.isNotEmpty() &&
                current.configuredFeeds.all { it in current.selectedFeeds }
        val nextSelected =
            when {
                available.isEmpty() -> emptyList()
                previouslyFollowedAll -> available
                surviving.isNotEmpty() -> surviving
                else -> available
            }
        return current.copy(
            selectedFeeds = nextSelected,
            configuredFeeds = available,
        )
    }

    companion object {
        const val DEFAULT_QUIET_START_MINUTE = 22 * 60
        const val DEFAULT_QUIET_END_MINUTE = 7 * 60
        const val MINUTES_PER_DAY = 24 * 60

        private fun normalizeFeeds(feeds: List<String>): List<String> =
            feeds
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
    }
}
