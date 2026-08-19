package com.kanarek.data

/**
 * Container codec for multiple named playlists. The container is plain text:
 * `#KANAREK-PLAYLIST:<name>` section markers followed by ordinary M3U text. Each section
 * round-trips through [M3uCodec]. Malformed leading content is ignored and duplicate names keep
 * the last occurrence.
 */
object Playlists {
    data class Named(
        val name: String,
        val stations: List<Station>,
    )

    private const val MARKER = "#KANAREK-PLAYLIST:"

    /** Parse a container into named playlists, in file order. */
    fun parse(text: String): List<Named> {
        val sections = LinkedHashMap<String, StringBuilder>()
        var current: StringBuilder? = null
        text.lineSequence().forEach { line ->
            if (line.startsWith(MARKER)) {
                val name = line.removePrefix(MARKER).trim()
                if (name.isNotEmpty()) {
                    current = StringBuilder().also { sections[name] = it }
                } else {
                    current = null
                }
            } else {
                current?.append(line)?.append('\n')
            }
        }
        return sections.map { (name, body) -> Named(name, M3uCodec.parse(body.toString())) }
    }

    /** Serialize named playlists to the container format. */
    fun build(playlists: List<Named>): String =
        buildString {
            playlists.forEach { p ->
                append(MARKER).append(p.name.trim().replace("\n", " ")).append('\n')
                append(M3uCodec.build(p.stations))
            }
        }
}
