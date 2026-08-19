package com.kanarek.data

private val URL_HOST =
    Regex("""^[A-Za-z][A-Za-z0-9+.-]*://(?:[^@/?#]*@)?(\[[^]]+]|[^:/?#]+)""")

/** Return a compact host label for an absolute URL, or null when the input has no usable host. */
internal fun urlHostLabel(url: String): String? =
    URL_HOST
        .find(url.trim())
        ?.groupValues
        ?.getOrNull(1)
        ?.removePrefix("[")
        ?.removeSuffix("]")
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }
