package com.sidescreen.app

import android.bluetooth.BluetoothProfile

/** Decides how a newly registered HID app should recover its host channel. */
object BluetoothPenReconnectPolicy {
    enum class Action {
        WAIT_FOR_HOST,
        USE_CONNECTED_CHANNEL,
    }

    fun action(
        hasPluggedHost: Boolean,
        profileState: Int,
    ): Action {
        if (!hasPluggedHost) return Action.WAIT_FOR_HOST
        return if (profileState == BluetoothProfile.STATE_CONNECTED) {
            Action.USE_CONNECTED_CHANNEL
        } else {
            Action.WAIT_FOR_HOST
        }
    }
}
