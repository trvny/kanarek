package com.kanarek.data

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderBackgroundRefreshTest {
    @Test
    fun refreshIntervalsNormalizeAndChooseScheduleAction() {
        assertEquals(ReaderBackgroundRefresh.OFF, ReaderBackgroundRefresh.normalize(17))
        assertEquals(
            ReaderRefreshScheduleAction.CANCEL,
            ReaderBackgroundRefresh.scheduleAction(ReaderBackgroundRefresh.OFF),
        )
        ReaderBackgroundRefresh.options.drop(1).forEach { minutes ->
            assertEquals(minutes, ReaderBackgroundRefresh.normalize(minutes))
            assertEquals(
                ReaderRefreshScheduleAction.SCHEDULE,
                ReaderBackgroundRefresh.scheduleAction(minutes),
            )
        }
    }
}
