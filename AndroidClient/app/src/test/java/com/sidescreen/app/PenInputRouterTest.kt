package com.sidescreen.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PenInputRouterTest {
    @Test
    fun pressureAwareHostWinsWithoutDuplicatingBluetoothInput() {
        assertEquals(
            listOf(PenInputRouter.Destination.NETWORK_PEN),
            PenInputRouter.destinations(
                isStylus = true,
                bluetoothConnected = true,
                networkPenSupported = true,
            ),
        )
    }

    @Test
    fun networkPenRemainsFallbackWhenBluetoothIsDisconnected() {
        assertEquals(
            listOf(PenInputRouter.Destination.NETWORK_PEN),
            PenInputRouter.destinations(
                isStylus = true,
                bluetoothConnected = false,
                networkPenSupported = true,
            ),
        )
    }

    @Test
    fun fingerInputAlwaysUsesExistingTouchPath() {
        assertEquals(
            listOf(PenInputRouter.Destination.NETWORK_TOUCH),
            PenInputRouter.destinations(
                isStylus = false,
                bluetoothConnected = true,
                networkPenSupported = true,
            ),
        )
    }
}
