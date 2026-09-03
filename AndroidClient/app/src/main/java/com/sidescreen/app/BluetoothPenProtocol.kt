package com.sidescreen.app

import kotlin.math.roundToInt

/** HID report descriptor and reports used when the tablet acts as a Bluetooth pen. */
object BluetoothPenProtocol {
    const val REPORT_ID = 1

    val reportDescriptor: ByteArray = intArrayOf(
        0x05, 0x0D,       // Usage Page (Digitizers)
        0x09, 0x02,       // Usage (Pen: display-integrated digitizer)
        0xA1, 0x01,       // Collection (Application)
        0x85, REPORT_ID,  // Report ID
        0x09, 0x20,       // Usage (Stylus)
        0xA1, 0x00,       // Collection (Physical)
        0x15, 0x00,       // Logical Minimum (0)
        0x25, 0x01,       // Logical Maximum (1)
        0x75, 0x01,       // Report Size (1)
        0x09, 0x42,       // Usage (Tip Switch)
        0x09, 0x32,       // Usage (In Range)
        0x95, 0x02,       // Report Count (2)
        0x81, 0x02,       // Input (Data, Variable, Absolute)
        0x75, 0x06,       // Report Size (6)
        0x95, 0x01,       // Report Count (1)
        0x81, 0x03,       // Input (Constant)
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x30,       // Usage (X)
        0x09, 0x31,       // Usage (Y)
        0x15, 0x00,       // Logical Minimum (0)
        0x27, 0xFF, 0xFF, 0x00, 0x00, // Logical Maximum (65535, unsigned)
        0x75, 0x10,       // Report Size (16)
        0x95, 0x02,       // Report Count (2)
        0x81, 0x02,       // Input (Data, Variable, Absolute)
        0x05, 0x0D,       // Usage Page (Digitizers)
        0x09, 0x30,       // Usage (Tip Pressure)
        0x15, 0x00,       // Logical Minimum (0)
        0x26, 0xFF, 0x03, // Logical Maximum (1023)
        0x75, 0x10,       // Report Size (16)
        0x95, 0x01,       // Report Count (1)
        0x81, 0x02,       // Input (Data, Variable, Absolute)
        0xC0,             // End Collection
        0xC0,             // End Collection
    ).map(Int::toByte).toByteArray()

    fun contact(x: Float, y: Float, pressure: Float): ByteArray =
        encode(x, y, pressure, flags = 0b0000_0011)

    fun hover(x: Float, y: Float): ByteArray =
        encode(x, y, pressure = 0f, flags = 0b0000_0010)

    fun outOfRange(x: Float, y: Float): ByteArray =
        encode(x, y, pressure = 0f, flags = 0)

    private fun encode(x: Float, y: Float, pressure: Float, flags: Int): ByteArray {
        val encodedX = scale(x, 0xFFFF)
        val encodedY = scale(y, 0xFFFF)
        val encodedPressure = scale(pressure, 0x03FF)
        return byteArrayOf(
            flags.toByte(),
            encodedX.toByte(), (encodedX ushr 8).toByte(),
            encodedY.toByte(), (encodedY ushr 8).toByte(),
            encodedPressure.toByte(), (encodedPressure ushr 8).toByte(),
        )
    }

    private fun scale(value: Float, maximum: Int): Int =
        (value.coerceIn(0f, 1f) * maximum).roundToInt()
}
