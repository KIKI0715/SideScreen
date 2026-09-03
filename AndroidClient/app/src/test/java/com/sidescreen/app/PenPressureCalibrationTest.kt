package com.sidescreen.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PenPressureCalibrationTest {
    @Test
    fun validatedSettingsClampToSupportedRanges() {
        val calibration = PenPressureCalibration.validated(1f, 0f, 3f)

        assertEquals(0.2f, calibration.minimum)
        assertEquals(0.5f, calibration.maximum)
        assertEquals(2f, calibration.gamma)
    }

    @Test
    fun nonFiniteSettingsFallBackToDefaults() {
        val calibration = PenPressureCalibration.validated(Float.NaN, Float.POSITIVE_INFINITY, Float.NaN)

        assertEquals(PenPressureCalibration.DEFAULT, calibration)
    }

    @Test
    fun mappedPressureIsMonotonicAndClamped() {
        for (gamma in listOf(0.5f, 1f, 2f)) {
            val calibration = PenPressureCalibration.validated(0.02f, 0.95f, gamma)
            val outputs = (-10..110).map { calibration.map(it / 100f) }

            assertEquals(0f, outputs.first())
            assertEquals(1f, outputs.last())
            outputs.zipWithNext().forEach { (left, right) ->
                check(left <= right) { "Pressure curve must be monotonic: $left > $right" }
            }
        }
    }
}
