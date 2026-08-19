package com.kanarek.data

/** Stable SHA-1 used for persisted station ids. Not used for security. */
internal fun sha1Hex(value: String): String {
    val source = value.encodeToByteArray()
    val bitLength = source.size.toLong() * 8L
    val paddedSize = ((source.size + 9 + 63) / 64) * 64
    val data = ByteArray(paddedSize)
    source.copyInto(data)
    data[source.size] = 0x80.toByte()
    for (i in 0 until 8) {
        data[paddedSize - 1 - i] = (bitLength ushr (i * 8)).toByte()
    }

    var h0 = 0x67452301
    var h1 = 0xEFCDAB89.toInt()
    var h2 = 0x98BADCFE.toInt()
    var h3 = 0x10325476
    var h4 = 0xC3D2E1F0.toInt()
    val words = IntArray(80)

    for (chunk in data.indices step 64) {
        for (i in 0 until 16) {
            val offset = chunk + i * 4
            words[i] =
                ((data[offset].toInt() and 0xFF) shl 24) or
                    ((data[offset + 1].toInt() and 0xFF) shl 16) or
                    ((data[offset + 2].toInt() and 0xFF) shl 8) or
                    (data[offset + 3].toInt() and 0xFF)
        }
        for (i in 16 until 80) {
            words[i] = rotateLeft(words[i - 3] xor words[i - 8] xor words[i - 14] xor words[i - 16], 1)
        }

        var a = h0
        var b = h1
        var c = h2
        var d = h3
        var e = h4

        for (i in 0 until 80) {
            val f: Int
            val k: Int
            when (i) {
                in 0..19 -> {
                    f = (b and c) or (b.inv() and d)
                    k = 0x5A827999
                }
                in 20..39 -> {
                    f = b xor c xor d
                    k = 0x6ED9EBA1
                }
                in 40..59 -> {
                    f = (b and c) or (b and d) or (c and d)
                    k = 0x8F1BBCDC.toInt()
                }
                else -> {
                    f = b xor c xor d
                    k = 0xCA62C1D6.toInt()
                }
            }
            val temp = rotateLeft(a, 5) + f + e + k + words[i]
            e = d
            d = c
            c = rotateLeft(b, 30)
            b = a
            a = temp
        }

        h0 += a
        h1 += b
        h2 += c
        h3 += d
        h4 += e
    }

    return buildString(40) {
        appendHex(h0)
        appendHex(h1)
        appendHex(h2)
        appendHex(h3)
        appendHex(h4)
    }
}

private fun rotateLeft(value: Int, bits: Int): Int =
    (value shl bits) or (value ushr (32 - bits))

private fun StringBuilder.appendHex(value: Int) {
    val digits = "0123456789abcdef"
    for (shift in 28 downTo 0 step 4) {
        append(digits[(value ushr shift) and 0xF])
    }
}
