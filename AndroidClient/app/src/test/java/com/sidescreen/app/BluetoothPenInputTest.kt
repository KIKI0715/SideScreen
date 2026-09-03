package com.sidescreen.app

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class BluetoothPenInputTest {
    @Test
    fun contactNormalizesCoordinatesAndCalibratesPressure() {
        assertArrayEquals(
            BluetoothPenProtocol.contact(x = 0.25f, y = 0.75f, pressure = 0.5f),
            BluetoothPenInput.report(
                phase = BluetoothPenInput.Phase.CONTACT,
                x = 25f,
                y = 150f,
                width = 100,
                height = 200,
                pressure = 0.5f,
                calibration = PenPressureCalibration.validated(0f, 1f, 1f),
            ),
        )
    }

    @Test
    fun hoverKeepsPenInRangeWithoutTipPressure() {
        assertArrayEquals(
            BluetoothPenProtocol.hover(x = 0.5f, y = 0.25f),
            BluetoothPenInput.report(
                phase = BluetoothPenInput.Phase.HOVER,
                x = 50f,
                y = 25f,
                width = 100,
                height = 100,
                pressure = 0.8f,
                calibration = PenPressureCalibration.DEFAULT,
            ),
        )
    }

    @Test
    fun exitClearsTipAndInRangeFlags() {
        assertArrayEquals(
            BluetoothPenProtocol.outOfRange(x = 1f, y = 0f),
            BluetoothPenInput.report(
                phase = BluetoothPenInput.Phase.OUT_OF_RANGE,
                x = 120f,
                y = -10f,
                width = 100,
                height = 100,
                pressure = 1f,
                calibration = PenPressureCalibration.DEFAULT,
            ),
        )
    }
}
