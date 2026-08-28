package com.sidescreen.app

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Wire codec for pressure-aware stylus strokes. */
object PenProtocol {
    const val MESSAGE_PEN_CAPABILITY = 12
    const val MESSAGE_PEN = 15

    const val ACTION_DOWN = 0
    const val ACTION_MOVE = 1
    const val ACTION_UP = 2

    private const val PACKET_SIZE = 15

    fun capabilityAdvertisement(): ByteArray = byteArrayOf(MESSAGE_PEN_CAPABILITY.toByte())

    /**
     * Encode `[type][action][reserved flags][x f32 LE][y f32 LE][pressure f32 LE]`.
     */
    fun encode(
        action: Int,
        x: Float,
        y: Float,
        pressure: Float,
    ): ByteArray {
        require(action in ACTION_DOWN..ACTION_UP) { "Invalid pen action: $action" }
        require(x.isFinite() && y.isFinite() && pressure.isFinite()) {
            "Pen coordinates and pressure must be finite"
        }

        return ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(MESSAGE_PEN.toByte())
            put(action.toByte())
            put(0) // Reserved for future pen flags.
            putFloat(x.coerceIn(0f, 1f))
            putFloat(y.coerceIn(0f, 1f))
            putFloat(pressure.coerceIn(0f, 1f))
        }.array()
    }
}
