package com.kanarek.data

internal actual fun parseFeedDate(value: String): Long? =
    parseIsoDateTime(value)
        ?: parseRfcDateTime(value)
        ?: parseLocalDate(value)

internal actual fun platformLanguage(): String = "en"

private val ISO_DATE_TIME =
    Regex(
        """^(\d{4})-(\d{2})-(\d{2})[Tt](\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{1,9}))?)?([Zz]|[+-]\d{2}:?\d{2})$""",
    )
private val LOCAL_DATE = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")
private val RFC_DATE_TIME =
    Regex(
        """^(?:[A-Za-z]{3},\s*)?(\d{1,2})\s+([A-Za-z]{3})\s+(\d{4})\s+(\d{2}):(\d{2}):(\d{2})\s+([+-]\d{4}|[A-Za-z]{1,5})$""",
    )

private fun parseIsoDateTime(value: String): Long? {
    val match = ISO_DATE_TIME.matchEntire(value) ?: return null
    val millis =
        match.groupValues[7]
            .take(3)
            .padEnd(3, '0')
            .ifEmpty { "0" }
            .toIntOrNull() ?: return null
    return epochMillis(
        year = match.groupValues[1].toIntOrNull() ?: return null,
        month = match.groupValues[2].toIntOrNull() ?: return null,
        day = match.groupValues[3].toIntOrNull() ?: return null,
        hour = match.groupValues[4].toIntOrNull() ?: return null,
        minute = match.groupValues[5].toIntOrNull() ?: return null,
        second = match.groupValues[6].toIntOrNull() ?: 0,
        millis = millis,
        offsetSeconds = parseOffset(match.groupValues[8]) ?: return null,
    )
}

private fun parseRfcDateTime(value: String): Long? {
    val match = RFC_DATE_TIME.matchEntire(value) ?: return null
    return epochMillis(
        year = match.groupValues[3].toIntOrNull() ?: return null,
        month = monthNumber(match.groupValues[2]) ?: return null,
        day = match.groupValues[1].toIntOrNull() ?: return null,
        hour = match.groupValues[4].toIntOrNull() ?: return null,
        minute = match.groupValues[5].toIntOrNull() ?: return null,
        second = match.groupValues[6].toIntOrNull() ?: return null,
        millis = 0,
        offsetSeconds = parseOffset(match.groupValues[7]) ?: return null,
    )
}

private fun parseLocalDate(value: String): Long? {
    val match = LOCAL_DATE.matchEntire(value) ?: return null
    return epochMillis(
        year = match.groupValues[1].toIntOrNull() ?: return null,
        month = match.groupValues[2].toIntOrNull() ?: return null,
        day = match.groupValues[3].toIntOrNull() ?: return null,
        hour = 0,
        minute = 0,
        second = 0,
        millis = 0,
        offsetSeconds = 0,
    )
}

private fun parseOffset(raw: String): Int? {
    return when (raw.uppercase()) {
        "Z", "UT", "UTC", "GMT" -> 0
        "EST" -> -5 * 3_600
        "EDT" -> -4 * 3_600
        "CST" -> -6 * 3_600
        "CDT" -> -5 * 3_600
        "MST" -> -7 * 3_600
        "MDT" -> -6 * 3_600
        "PST" -> -8 * 3_600
        "PDT" -> -7 * 3_600
        "CET" -> 1 * 3_600
        "CEST" -> 2 * 3_600
        "EET" -> 2 * 3_600
        "EEST" -> 3 * 3_600
        "BST" -> 1 * 3_600
        "JST", "KST" -> 9 * 3_600
        "AEST" -> 10 * 3_600
        "AEDT" -> 11 * 3_600
        "ACST" -> 9 * 3_600 + 30 * 60
        "ACDT" -> 10 * 3_600 + 30 * 60
        "AWST" -> 8 * 3_600
        else -> parseNumericOffset(raw)
    }
}

private fun parseNumericOffset(raw: String): Int? {
    if (raw.length !in 5..6 || raw.firstOrNull() !in setOf('+', '-')) return null
    val digits = raw.drop(1).replace(":", "")
    if (digits.length != 4 || digits.any { !it.isDigit() }) return null
    val hours = digits.take(2).toInt()
    val minutes = digits.drop(2).toInt()
    if (hours > 18 || minutes > 59 || (hours == 18 && minutes != 0)) return null
    val seconds = hours * 3_600 + minutes * 60
    return if (raw[0] == '-') -seconds else seconds
}

private fun monthNumber(raw: String): Int? =
    when (raw.lowercase()) {
        "jan" -> 1
        "feb" -> 2
        "mar" -> 3
        "apr" -> 4
        "may" -> 5
        "jun" -> 6
        "jul" -> 7
        "aug" -> 8
        "sep" -> 9
        "oct" -> 10
        "nov" -> 11
        "dec" -> 12
        else -> null
    }

private fun epochMillis(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int,
    millis: Int,
    offsetSeconds: Int,
): Long? {
    if (month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
    if (hour !in 0..23 || minute !in 0..59 || second !in 0..59 || millis !in 0..999) return null
    val seconds =
        daysFromCivil(year, month, day) * 86_400L +
            hour * 3_600L +
            minute * 60L +
            second -
            offsetSeconds
    return seconds * 1_000L + millis
}

private fun daysInMonth(
    year: Int,
    month: Int,
): Int =
    when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

private fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun daysFromCivil(
    year: Int,
    month: Int,
    day: Int,
): Long {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = if (adjustedYear >= 0) adjustedYear / 400 else (adjustedYear - 399) / 400
    val yearOfEra = adjustedYear - era * 400
    val adjustedMonth = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * adjustedMonth + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097L + dayOfEra - 719_468L
}
