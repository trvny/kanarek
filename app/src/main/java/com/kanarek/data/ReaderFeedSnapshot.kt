package com.kanarek.data

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

internal object ReaderFeedSnapshotCodec {
    private const val VERSION = "2"
    private const val LEGACY_VERSION = "1"
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(snapshot: ReaderFeedSnapshot): String =
        buildList {
            add("$VERSION|${snapshot.lastUpdatedMillis}")
            snapshot.itemsByFeed.forEach { (feed, items) ->
                val encodedItems = encodeItems(items)
                val updatedAt = snapshot.updatedAtByFeed[feed] ?: snapshot.lastUpdatedMillis
                add("${encodeText(feed)}|$updatedAt|${encodeText(encodedItems)}")
            }
        }.joinToString("\n")

    fun decode(raw: String?): ReaderFeedSnapshot? =
        runCatching {
            val lines = raw.orEmpty().lineSequence().filter(String::isNotBlank).toList()
            val header = lines.firstOrNull()?.split('|', limit = 2) ?: return null
            if (header.size != 2 || header[0] !in setOf(VERSION, LEGACY_VERSION)) return null
            val updated = header[1].toLongOrNull()?.takeIf { it > 0L } ?: return null
            val itemsByFeed = linkedMapOf<String, List<NewsItem>>()
            val updatedAtByFeed = linkedMapOf<String, Long>()

            lines.drop(1).forEach { line ->
                val fields =
                    line.split(
                        '|',
                        limit = if (header[0] == VERSION) 3 else 2,
                    )
                val expectedSize = if (header[0] == VERSION) 3 else 2
                if (fields.size != expectedSize) return@forEach
                val feed = decodeText(fields[0]).trim()
                if (feed.isEmpty()) return@forEach
                val feedUpdatedAt =
                    if (header[0] == VERSION) {
                        fields[1].toLongOrNull()?.takeIf { it > 0L } ?: updated
                    } else {
                        updated
                    }
                val encodedItems = fields.last()
                itemsByFeed[feed] = decodeItems(decodeText(encodedItems))
                updatedAtByFeed[feed] = feedUpdatedAt
            }
            ReaderFeedSnapshot(
                itemsByFeed = itemsByFeed,
                lastUpdatedMillis = updated,
                updatedAtByFeed = updatedAtByFeed,
            )
        }.getOrNull()

    private fun encodeItems(items: List<NewsItem>): String =
        items.take(MAX_ITEMS_PER_FEED).joinToString(ITEM_SEPARATOR) { item ->
            listOf(
                encodeText(item.title.take(MAX_TITLE_CHARS)),
                encodeText(item.link.take(MAX_URL_CHARS)),
                encodeText(item.summary.take(MAX_SUMMARY_CHARS)),
                encodeText(item.imageUrl.orEmpty().take(MAX_URL_CHARS)),
                encodeText(item.source.take(MAX_SOURCE_CHARS)),
                item.publishedAtMillis?.toString().orEmpty(),
            ).joinToString(FIELD_SEPARATOR)
        }

    private fun decodeItems(raw: String): List<NewsItem> =
        raw
            .split(ITEM_SEPARATOR)
            .filter(String::isNotBlank)
            .mapNotNull(::decodeItem)
            .distinctBy { it.link.trim() }

    private fun decodeItem(raw: String): NewsItem? {
        val fields = raw.split(FIELD_SEPARATOR, limit = 6)
        if (fields.size != 6) return null
        return NewsItem(
            title = decodeText(fields[0]),
            link = decodeText(fields[1]),
            summary = decodeText(fields[2]),
            imageUrl = decodeText(fields[3]).takeIf(String::isNotBlank),
            source = decodeText(fields[4]),
            publishedAtMillis = fields[5].toLongOrNull(),
        ).takeIf { it.link.isNotBlank() }
    }

    private fun encodeText(value: String): String = encoder.encodeToString(value.toByteArray(UTF_8))

    private fun decodeText(value: String): String = String(decoder.decode(value), UTF_8)

    private const val ITEM_SEPARATOR = "~"
    private const val FIELD_SEPARATOR = ":"
    private const val MAX_ITEMS_PER_FEED = 20
    private const val MAX_TITLE_CHARS = 500
    private const val MAX_URL_CHARS = 2_048
    private const val MAX_SUMMARY_CHARS = 2_000
    private const val MAX_SOURCE_CHARS = 300
}
