package com.kanarek.player

import androidx.media3.common.PlaybackException

internal object PlayerFailures {
    fun classify(errorCode: Int): PlayerFailureKind =
        when {
            errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> PlayerFailureKind.HTTP
            errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
                errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> PlayerFailureKind.NETWORK
            errorCode in DECODER_ERROR_RANGE -> PlayerFailureKind.DECODER
            else -> PlayerFailureKind.UNAVAILABLE
        }

    private val DECODER_ERROR_RANGE = 4_000..4_999
}
