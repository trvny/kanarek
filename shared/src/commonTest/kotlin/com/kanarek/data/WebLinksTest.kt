package com.kanarek.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebLinksTest {
    @Test
    fun acceptsHttpAndHttps() {
        assertTrue(WebLinks.isHttpOrHttps("https://example.com/article"))
        assertTrue(WebLinks.isHttpOrHttps("HTTP://example.com/article"))
        assertTrue(WebLinks.isHttpOrHttps("  https://example.com/trimmed  "))
    }

    @Test
    fun rejectsNonWebAndMalformedLinks() {
        assertFalse(WebLinks.isHttpOrHttps("intent://example/#Intent;scheme=https;end"))
        assertFalse(WebLinks.isHttpOrHttps("javascript:alert(1)"))
        assertFalse(WebLinks.isHttpOrHttps("example.com/no-scheme"))
        assertFalse(WebLinks.isHttpOrHttps(""))
    }
}
