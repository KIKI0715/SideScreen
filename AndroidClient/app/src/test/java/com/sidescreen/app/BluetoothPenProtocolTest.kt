package com.sidescreen.app

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothPenProtocolTest {
    @Test
    fun descriptorDeclaresDisplayIntegratedPenApplication() {
        assertArrayEquals(
            byteArrayOf(
                0x05, 0x0D, // Usage Page (Digitizers)
                0x09, 0x02, // Usage (Pen)
                0xA1.toByte(), 0x01, // Collection (Application)
            ),
            BluetoothPenProtocol.reportDescriptor.copyOfRange(0, 6),
        )
    }

    @Test
    fun descriptorDeclaresUnsignedSixteenBitCoordinateMaximum() {
        val descriptor = BluetoothPenProtocol.reportDescriptor.map(Byte::toInt)

        assertTrue(
            descriptor.windowed(size = 5).any {
                it == listOf(0x27, -1, -1, 0x00, 0x00)
            },
        )
    }

    @Test
    fun contactReportEncodesTipRangeCoordinatesAndPressure() {
        assertArrayEquals(
            byteArrayOf(
                0b0000_0011,
                0x00, 0x40,
                0xFF.toByte(), 0xBF.toByte(),
                0x00, 0x02,
            ),
            BluetoothPenProtocol.contact(
                x = 0.25f,
                y = 0.75f,
                pressure = 0.5f,
            ),
        )
    }
}
