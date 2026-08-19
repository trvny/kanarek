package com.kanarek.data

/** Validation shared by reader and widget paths for links supplied by untrusted feeds. */
object WebLinks {
    fun isHttpOrHttps(raw: String): Boolean = httpUrlHost(raw) != null
}
