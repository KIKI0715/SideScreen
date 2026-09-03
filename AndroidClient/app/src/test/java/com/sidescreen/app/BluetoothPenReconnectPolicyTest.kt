package com.sidescreen.app

import android.bluetooth.BluetoothProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class BluetoothPenReconnectPolicyTest {
    @Test
    fun `registered app uses an existing host connected channel`() {
        assertEquals(
            BluetoothPenReconnectPolicy.Action.USE_CONNECTED_CHANNEL,
            BluetoothPenReconnectPolicy.action(
                hasPluggedHost = true,
                profileState = BluetoothProfile.STATE_CONNECTED,
            ),
        )
    }

    @Test
    fun `registered app waits for the host when its channel is disconnected`() {
        assertEquals(
            BluetoothPenReconnectPolicy.Action.WAIT_FOR_HOST,
            BluetoothPenReconnectPolicy.action(
                hasPluggedHost = true,
                profileState = BluetoothProfile.STATE_DISCONNECTED,
            ),
        )
    }

    @Test
    fun `registered app without a plugged host waits for pairing`() {
        assertEquals(
            BluetoothPenReconnectPolicy.Action.WAIT_FOR_HOST,
            BluetoothPenReconnectPolicy.action(
                hasPluggedHost = false,
                profileState = BluetoothProfile.STATE_DISCONNECTED,
            ),
        )
    }
}
