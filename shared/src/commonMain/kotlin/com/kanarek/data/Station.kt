package com.kanarek.data

/** What kind of stream a [Station] is, so clients can split TV from radio and only spend a
 * video surface on streams that carry a picture. [UNKNOWN] is the honest default for a
 * hand-added station or an imported list that did not say. */
enum class StationKind { TV, RADIO, UNKNOWN }

/**
 * A single playable stream: an IPTV channel or an internet radio station. [id] is a stable
 * hash of [streamUrl] (see [M3uCodec]), so re-importing or re-parsing the same URL never mints
 * a new identity for it. [userAgent]/[referrer] are optional per-stream HTTP request headers
 * that platform playback implementations can apply when required by a source.
 */
data class Station(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    /** iptv-org channel id (M3U `tvg-id`); the join key for logo/EPG enrichment. */
    val tvgId: String? = null,
    val userAgent: String? = null,
    val referrer: String? = null,
    /** TV vs radio. Persisted as a `kanarek-kind` #EXTINF attribute by [M3uCodec]; defaults to
     * [StationKind.UNKNOWN]. */
    val kind: StationKind = StationKind.UNKNOWN,
)
