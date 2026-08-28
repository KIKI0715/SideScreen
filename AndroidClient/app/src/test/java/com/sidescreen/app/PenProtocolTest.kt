package com.sidescreen.app

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PenProtocolTest {
    @Test
    fun capabilityAdvertisementIsLegacySafeSingleByte() {
        assertArrayEquals(
            byteArrayOf(PenProtocol.MESSAGE_PEN_CAPABILITY.toByte()),
            PenProtocol.capabilityAdvertisement(),
        )
    }

    @Test
    fun encodesPressureStrokePacket() {
        val packet =
            PenProtocol.encode(
                action = PenProtocol.ACTION_MOVE,
                x = 0.25f,
                y = 0.75f,
                pressure = 0.5f,
            )

        assertEquals(15, packet.size)
        assertEquals(PenProtocol.MESSAGE_PEN, packet[0].toInt() and 0xFF)
        assertEquals(PenProtocol.ACTION_MOVE, packet[1].toInt() and 0xFF)
        assertEquals(0, packet[2].toInt() and 0xFF)

        val payload = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(0.25f, payload.getFloat(3))
        assertEquals(0.75f, payload.getFloat(7))
        assertEquals(0.5f, payload.getFloat(11))
    }

    @Test
    fun clampsCoordinatesAndPressureToNormalizedRange() {
        val packet =
            PenProtocol.encode(
                action = PenProtocol.ACTION_DOWN,
                x = -0.25f,
                y = 1.25f,
                pressure = 2f,
            )

        val payload = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(0f, payload.getFloat(3))
        assertEquals(1f, payload.getFloat(7))
        assertEquals(1f, payload.getFloat(11))
    }

    @Test
    fun rejectsNonFiniteValues() {
        try {
            PenProtocol.encode(
                action = PenProtocol.ACTION_MOVE,
                x = Float.NaN,
                y = 0.5f,
                pressure = 0.5f,
            )
            error("expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected: malformed coordinates must never reach the wire.
        }
    }
}
