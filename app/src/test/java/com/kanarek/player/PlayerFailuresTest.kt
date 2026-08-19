package com.kanarek.player

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerFailuresTest {
    @Test
    fun classifiesNetworkHttpDecoderAndUnavailableErrors() {
        assertEquals(
            PlayerFailureKind.NETWORK,
            PlayerFailures.classify(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED),
        )
        assertEquals(
            PlayerFailureKind.HTTP,
            PlayerFailures.classify(PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS),
        )
        assertEquals(
            PlayerFailureKind.DECODER,
            PlayerFailures.classify(PlaybackException.ERROR_CODE_DECODING_FAILED),
        )
        assertEquals(
            PlayerFailureKind.UNAVAILABLE,
            PlayerFailures.classify(PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND),
        )
    }
}
