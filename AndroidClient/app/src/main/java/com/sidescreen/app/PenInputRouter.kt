package com.sidescreen.app

/** Chooses exactly one Mac input path for each tablet input event. */
object PenInputRouter {
    enum class Destination {
        BLUETOOTH_HID,
        NETWORK_PEN,
        NETWORK_TOUCH,
    }

    fun destinations(
        isStylus: Boolean,
        bluetoothConnected: Boolean,
        networkPenSupported: Boolean,
    ): List<Destination> =
        if (!isStylus) {
            listOf(Destination.NETWORK_TOUCH)
        } else if (networkPenSupported) {
            listOf(Destination.NETWORK_PEN)
        } else if (bluetoothConnected) {
            listOf(Destination.BLUETOOTH_HID)
        } else {
            listOf(Destination.NETWORK_TOUCH)
        }
}
