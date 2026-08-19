package com.kanarek.player

enum class PlayerFailureKind {
    NETWORK,
    HTTP,
    DECODER,
    UNAVAILABLE,
}

data class PlayerFailure(
    val kind: PlayerFailureKind,
    val httpStatus: Int? = null,
    val automaticRetryAttempt: Int = 0,
    val maxAutomaticRetries: Int = 0,
    val retryPending: Boolean = false,
)

data class PlayerFailureTransition(
    val failure: PlayerFailure,
    val retryDelayMs: Long?,
)

class PlayerFailureMachine(
    private val maxAutomaticRetries: Int = DEFAULT_MAX_AUTOMATIC_RETRIES,
    private val baseRetryDelayMs: Long = DEFAULT_BASE_RETRY_DELAY_MS,
    private val maxRetryDelayMs: Long = DEFAULT_MAX_RETRY_DELAY_MS,
) {
    private var automaticRetryAttempts = 0

    fun onError(
        kind: PlayerFailureKind,
        httpStatus: Int? = null,
    ): PlayerFailureTransition {
        val retryable = PlayerFailurePolicy.isAutomaticallyRetryable(kind, httpStatus)
        val canRetry = retryable && automaticRetryAttempts < maxAutomaticRetries
        val retryDelay =
            if (canRetry) {
                val multiplier = 1L shl automaticRetryAttempts
                (baseRetryDelayMs * multiplier).coerceAtMost(maxRetryDelayMs)
            } else {
                null
            }
        if (retryDelay != null) automaticRetryAttempts++
        return PlayerFailureTransition(
            failure =
                PlayerFailure(
                    kind = kind,
                    httpStatus = httpStatus,
                    automaticRetryAttempt = automaticRetryAttempts,
                    maxAutomaticRetries = maxAutomaticRetries,
                    retryPending = retryDelay != null,
                ),
            retryDelayMs = retryDelay,
        )
    }

    fun reset() {
        automaticRetryAttempts = 0
    }

    private companion object {
        const val DEFAULT_MAX_AUTOMATIC_RETRIES = 2
        const val DEFAULT_BASE_RETRY_DELAY_MS = 1_000L
        const val DEFAULT_MAX_RETRY_DELAY_MS = 8_000L
    }
}

internal object PlayerFailurePolicy {
    fun isAutomaticallyRetryable(
        kind: PlayerFailureKind,
        httpStatus: Int?,
    ): Boolean =
        when (kind) {
            PlayerFailureKind.NETWORK -> true
            PlayerFailureKind.HTTP ->
                httpStatus == null ||
                    httpStatus == HTTP_REQUEST_TIMEOUT ||
                    httpStatus == HTTP_TOO_EARLY ||
                    httpStatus == HTTP_TOO_MANY_REQUESTS ||
                    httpStatus in HTTP_SERVER_ERROR_RANGE
            PlayerFailureKind.DECODER,
            PlayerFailureKind.UNAVAILABLE -> false
        }

    private val HTTP_SERVER_ERROR_RANGE = 500..599
    private const val HTTP_REQUEST_TIMEOUT = 408
    private const val HTTP_TOO_EARLY = 425
    private const val HTTP_TOO_MANY_REQUESTS = 429
}
