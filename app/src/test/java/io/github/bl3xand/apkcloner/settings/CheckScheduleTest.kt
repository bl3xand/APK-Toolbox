package io.github.bl3xand.apkcloner.settings

import io.github.bl3xand.apkcloner.update.AutoUpdateWorker
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckScheduleTest {
    @Test
    fun oldSliderIndicesKeepTheirDurationsAndHourlyOptionsAreAppended() {
        assertEquals(listOf(24, 72, 168, 336, 720, 2160, 4320, 8760), (0..7).map(AppSettings::intervalHours))
        assertEquals(listOf(4, 6, 8, 12, 24), (8..12).map(AppSettings::intervalHours))
        assertEquals(12, AppSettings.intervalHours(11))
    }

    @Test
    fun anchorUsesNextLocalHourAtMidnightAndWhenAlreadyPassed() {
        val zone = ZoneId.of("Europe/Berlin")
        val before = Instant.parse("2025-01-15T00:30:00Z") // 01:30 local, anchor 01:00 has passed.
        val delay = AutoUpdateWorker.delayUntilAnchor(before, zone, 1)
        assertEquals(23 * 60 * 60 * 1000L + 30 * 60 * 1000L, delay)
        val midnight = Instant.parse("2025-01-15T00:00:00Z") // 01:00 local exactly.
        assertEquals(0L, AutoUpdateWorker.delayUntilAnchor(midnight, zone, 1))
    }

    @Test
    fun anchorRemainsPositiveAcrossDaylightSavingTransitions() {
        val zone = ZoneId.of("Europe/Berlin")
        val beforeGap = Instant.parse("2025-03-29T23:30:00Z")
        val gapDelay = AutoUpdateWorker.delayUntilAnchor(beforeGap, zone, 2)
        assertTrue(gapDelay > 0)
        val beforeOverlap = Instant.parse("2025-10-25T23:30:00Z")
        assertTrue(AutoUpdateWorker.delayUntilAnchor(beforeOverlap, zone, 2) > 0)
    }
}
