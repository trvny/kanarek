package com.kanarek.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerFailureMachineTest {
    @Test
    fun transientNetworkErrorsUseBoundedExponentialBackoff() {
        val machine =
            PlayerFailureMachine(
                maxAutomaticRetries = 2,
                baseRetryDelayMs = 100L,
                maxRetryDelayMs = 1_000L,
            )

        val first = machine.onError(PlayerFailureKind.NETWORK)
        val second = machine.onError(PlayerFailureKind.NETWORK)
        val exhausted = machine.onError(PlayerFailureKind.NETWORK)

        assertEquals(100L, first.retryDelayMs)
        assertEquals(1, first.failure.automaticRetryAttempt)
        assertTrue(first.failure.retryPending)
        assertEquals(200L, second.retryDelayMs)
        assertEquals(2, second.failure.automaticRetryAttempt)
        assertTrue(second.failure.retryPending)
        assertNull(exhausted.retryDelayMs)
        assertEquals(2, exhausted.failure.automaticRetryAttempt)
        assertFalse(exhausted.failure.retryPending)
    }

    @Test
    fun permanentHttpAndDecoderErrorsWaitForManualRetry() {
        val machine = PlayerFailureMachine()

        val notFound = machine.onError(PlayerFailureKind.HTTP, httpStatus = 404)
        val decoder = machine.onError(PlayerFailureKind.DECODER)

        assertNull(notFound.retryDelayMs)
        assertNull(decoder.retryDelayMs)
        assertFalse(notFound.failure.retryPending)
        assertFalse(decoder.failure.retryPending)
    }

    @Test
    fun serverErrorsRetryButSuccessResetStartsFreshBudget() {
        val machine =
            PlayerFailureMachine(
                maxAutomaticRetries = 2,
                baseRetryDelayMs = 100L,
                maxRetryDelayMs = 1_000L,
            )

        val beforeReset = machine.onError(PlayerFailureKind.HTTP, httpStatus = 503)
        machine.reset()
        val afterReset = machine.onError(PlayerFailureKind.NETWORK)

        assertEquals(100L, beforeReset.retryDelayMs)
        assertEquals(1, beforeReset.failure.automaticRetryAttempt)
        assertEquals(100L, afterReset.retryDelayMs)
        assertEquals(1, afterReset.failure.automaticRetryAttempt)
    }

    @Test
    fun retryPolicyCoversTransientHttpStatuses() {
        listOf(408, 425, 429, 500, 503, 599).forEach { status ->
            assertTrue(PlayerFailurePolicy.isAutomaticallyRetryable(PlayerFailureKind.HTTP, status))
        }
        listOf(400, 404, 600).forEach { status ->
            assertFalse(PlayerFailurePolicy.isAutomaticallyRetryable(PlayerFailureKind.HTTP, status))
        }
    }
}
