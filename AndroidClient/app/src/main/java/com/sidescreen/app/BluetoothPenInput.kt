package com.sidescreen.app

/** Converts tablet-space pen samples into Bluetooth HID reports. */
object BluetoothPenInput {
    enum class Phase {
        CONTACT,
        HOVER,
        OUT_OF_RANGE,
    }

    fun report(
        phase: Phase,
        x: Float,
        y: Float,
        width: Int,
        height: Int,
        pressure: Float,
        calibration: PenPressureCalibration,
    ): ByteArray {
        val normalizedX = if (width > 0) x / width else 0f
        val normalizedY = if (height > 0) y / height else 0f
        return when (phase) {
            Phase.CONTACT -> BluetoothPenProtocol.contact(
                x = normalizedX,
                y = normalizedY,
                pressure = calibration.map(pressure),
            )
            Phase.HOVER -> BluetoothPenProtocol.hover(
                x = normalizedX,
                y = normalizedY,
            )
            Phase.OUT_OF_RANGE -> BluetoothPenProtocol.outOfRange(
                x = normalizedX,
                y = normalizedY,
            )
        }
    }
}
