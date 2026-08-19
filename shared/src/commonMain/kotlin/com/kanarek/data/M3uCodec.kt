package com.kanarek.data

/**
 * Minimal M3U/M3U8 playlist reader/writer for IPTV channels and internet radio stations.
 * Understands common `#EXTINF` attributes plus VLC-style per-stream header lines and tolerates
 * malformed/minimal input by returning whatever entries it can parse.
 */
object M3uCodec {
    private val ATTR = Regex("""([\w-]+)\s*=\s*"([^"]*)"""")

    /** Parse M3U/M3U8 text into a station list, de-duped by stream URL (first occurrence wins). */
    fun parse(text: String): List<Station> {
        val stations = mutableListOf<Station>()
        var pendingName: String? = null
        var pendingTvgId: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null
        var pendingUserAgent: String? = null
        var pendingReferrer: String? = null
        var pendingKind: StationKind = StationKind.UNKNOWN

        text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    val body = line.substringAfter(':', missingDelimiterValue = "").trim()
                    val lastQuote = body.lastIndexOf('"')
                    val attrsPart: String
                    val title: String
                    if (lastQuote >= 0) {
                        attrsPart = body.substring(0, lastQuote + 1)
                        title = body.substring(lastQuote + 1).removePrefix(",").trim()
                    } else {
                        val comma = body.indexOf(',')
                        attrsPart = if (comma >= 0) body.substring(0, comma) else body
                        title = if (comma >= 0) body.substring(comma + 1).trim() else ""
                    }
                    val attrs =
                        ATTR
                            .findAll(attrsPart)
                            .associate { it.groupValues[1].lowercase() to it.groupValues[2].trim() }
                    pendingName = title.ifEmpty { null }
                    pendingTvgId = attrs["tvg-id"]?.ifEmpty { null }
                    pendingLogo = attrs["tvg-logo"]?.ifEmpty { null }
                    pendingGroup = attrs["group-title"]?.ifEmpty { null }
                    pendingUserAgent = attrs["user-agent"]?.ifEmpty { null }
                    pendingReferrer = attrs["referrer"]?.ifEmpty { null }
                    pendingKind = kindOf(attrs["kanarek-kind"])
                }

                line.startsWith("#EXTVLCOPT", ignoreCase = true) -> {
                    val body = line.substringAfter(':', missingDelimiterValue = "")
                    val eq = body.indexOf('=')
                    if (eq > 0) {
                        val value = body.substring(eq + 1).trim()
                        if (value.isNotEmpty()) {
                            when (body.substring(0, eq).trim().lowercase()) {
                                "http-user-agent" -> pendingUserAgent = value
                                "http-referrer", "http-referer" -> pendingReferrer = value
                            }
                        }
                    }
                }

                line.startsWith("#EXTGRP:", ignoreCase = true) ||
                    line.startsWith("#EXTALB:", ignoreCase = true) -> {
                    val value = line.substringAfter(':', missingDelimiterValue = "").trim()
                    if (pendingGroup.isNullOrBlank() && value.isNotEmpty()) pendingGroup = value
                }

                line.startsWith("#") -> Unit

                else -> {
                    val url = line
                    stations +=
                        Station(
                            id = hash(url),
                            name = pendingName?.takeIf { it.isNotBlank() } ?: labelOf(url),
                            streamUrl = url,
                            logoUrl = pendingLogo,
                            groupTitle = pendingGroup,
                            tvgId = pendingTvgId,
                            userAgent = pendingUserAgent,
                            referrer = pendingReferrer,
                            kind =
                                if (pendingKind != StationKind.UNKNOWN) {
                                    pendingKind
                                } else {
                                    inferKind(pendingName, pendingGroup, pendingTvgId, url)
                                },
                        )
                    pendingName = null
                    pendingTvgId = null
                    pendingLogo = null
                    pendingGroup = null
                    pendingUserAgent = null
                    pendingReferrer = null
                    pendingKind = StationKind.UNKNOWN
                }
            }
        }
        return stations.distinctBy { it.streamUrl }
    }

    /** The same stable id [parse] assigns to this URL. */
    fun idFor(url: String): String = hash(url.trim())

    /** Serialize a station list to an M3U8 playlist. */
    fun build(stations: List<Station>): String =
        buildString {
            append("#EXTM3U\n")
            stations.forEach { s ->
                val attrs =
                    buildString {
                        s.tvgId?.takeIf { it.isNotBlank() }?.let { append(" tvg-id=\"").append(clean(it)).append('"') }
                        s.logoUrl?.takeIf { it.isNotBlank() }?.let { append(" tvg-logo=\"").append(clean(it)).append('"') }
                        s.groupTitle?.takeIf { it.isNotBlank() }?.let { append(" group-title=\"").append(clean(it)).append('"') }
                        s.userAgent?.takeIf { it.isNotBlank() }?.let { append(" user-agent=\"").append(clean(it)).append('"') }
                        s.referrer?.takeIf { it.isNotBlank() }?.let { append(" referrer=\"").append(clean(it)).append('"') }
                        kindTag(s.kind)?.let { append(" kanarek-kind=\"").append(it).append('"') }
                    }
                append("#EXTINF:-1")
                    .append(attrs)
                    .append(',')
                    .append(clean(s.name))
                    .append('\n')
                s.userAgent?.takeIf { it.isNotBlank() }?.let { append("#EXTVLCOPT:http-user-agent=").append(clean(it)).append('\n') }
                s.referrer?.takeIf { it.isNotBlank() }?.let { append("#EXTVLCOPT:http-referrer=").append(clean(it)).append('\n') }
                append(s.streamUrl.trim()).append('\n')
            }
        }

    /** Best-effort stream kind when no explicit `kanarek-kind` attribute is present. */
    fun inferKind(
        name: String?,
        groupTitle: String?,
        tvgId: String?,
        url: String,
    ): StationKind {
        val path = url.trim().lowercase().substringBefore('?').substringBefore('#')
        val audioExt = listOf(".mp3", ".aac", ".ogg", ".oga", ".opus", ".flac", ".m4a", ".pls", ".wav")
        if (audioExt.any { path.endsWith(it) }) return StationKind.RADIO
        val text = "${name.orEmpty()} ${groupTitle.orEmpty()}".lowercase()
        if (RADIO_WORD.containsMatchIn(text)) return StationKind.RADIO
        if (!tvgId.isNullOrBlank()) return StationKind.TV
        val videoExt = listOf(".m3u8", ".mpd", ".ts", ".mp4", ".mkv")
        if (videoExt.any { path.endsWith(it) } || "/hls/" in path || "/dash/" in path) return StationKind.TV
        return StationKind.UNKNOWN
    }

    private val RADIO_WORD = Regex("""(^|[^\p{L}])(radio|radia|radiowa|fm)([^\p{L}]|$)""")

    private fun kindOf(raw: String?): StationKind =
        when (raw?.trim()?.lowercase()) {
            "tv" -> StationKind.TV
            "radio" -> StationKind.RADIO
            else -> StationKind.UNKNOWN
        }

    private fun kindTag(kind: StationKind): String? =
        when (kind) {
            StationKind.TV -> "tv"
            StationKind.RADIO -> "radio"
            StationKind.UNKNOWN -> null
        }

    private fun labelOf(url: String): String = urlHostLabel(url) ?: url

    private fun clean(s: String): String = s.replace("\"", "").replace("\n", " ").trim()

    private fun hash(s: String): String = sha1Hex(s)
}
