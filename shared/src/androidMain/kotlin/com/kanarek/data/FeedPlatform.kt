package com.kanarek.data

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.Locale

internal actual fun parseFeedDate(value: String): Long? =
    runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
        ?: runCatching {
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        ?: runCatching {
            OffsetDateTime.parse(value, RFC_OFFSET)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        ?: runCatching {
            ZonedDateTime.parse(value, RFC_ZONE)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        ?: runCatching {
            OffsetDateTime.parse(value, COMPACT_OFFSET)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        ?: runCatching {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()

internal actual fun platformLanguage(): String = Locale.getDefault().language

private val RFC_OFFSET =
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("EEE, ")
        .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendPattern(" MMM yyyy HH:mm:ss Z")
        .toFormatter(Locale.US)

private val RFC_ZONE =
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("EEE, ")
        .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendPattern(" MMM yyyy HH:mm:ss zzz")
        .toFormatter(Locale.US)

private val COMPACT_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
