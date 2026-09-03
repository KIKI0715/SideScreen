package com.sidescreen.app

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.concurrent.CopyOnWriteArraySet

/** Activity-owned Bluetooth HID pen session. */
class BluetoothPenSession(private val context: Context) {
    enum class State {
        IDLE,
        REGISTERING,
        READY,
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        FAILED,
    }

    data class Snapshot(
        val state: State,
        val deviceName: String? = null,
    )

    private val listeners = CopyOnWriteArraySet<(Snapshot) -> Unit>()
    private val mainExecutor = ContextCompat.getMainExecutor(context)

    @Volatile
    private var snapshot = Snapshot(State.IDLE)

    @Volatile
    private var hidDevice: BluetoothHidDevice? = null

    @Volatile
    private var connectedDevice: BluetoothDevice? = null

    @Volatile
    private var openingProfile = false

    @Volatile
    private var appRegistered = false

    @Volatile
    private var registeringApp = false

    @Volatile
    private var active = false

    val isConnected: Boolean
        get() = snapshot.state == State.CONNECTED && connectedDevice != null

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            if (!active) {
                context.getSystemService(BluetoothManager::class.java).adapter
                    .closeProfileProxy(BluetoothProfile.HID_DEVICE, proxy)
                return
            }
            openingProfile = false
            val device = proxy as BluetoothHidDevice
            hidDevice = device
            registerApp(device)
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            openingProfile = false
            appRegistered = false
            registeringApp = false
            hidDevice = null
            connectedDevice = null
            if (active) publish(Snapshot(State.DISCONNECTED))
        }
    }

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, isRegistered: Boolean) {
            if (!active) return
            registeringApp = false
            appRegistered = isRegistered
            if (!isRegistered) {
                connectedDevice = null
                publish(Snapshot(State.FAILED))
                return
            }

            val proxy = hidDevice
            if (pluggedDevice == null || proxy == null) {
                connectedDevice = null
                publish(Snapshot(State.READY))
                return
            }

            val profileState = proxy.getConnectionState(pluggedDevice)
            when (BluetoothPenReconnectPolicy.action(true, profileState)) {
                BluetoothPenReconnectPolicy.Action.WAIT_FOR_HOST -> publish(Snapshot(State.READY))
                BluetoothPenReconnectPolicy.Action.USE_CONNECTED_CHANNEL -> {
                    connectedDevice = pluggedDevice
                    publish(Snapshot(State.CONNECTED, safeDeviceName(pluggedDevice)))
                }
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            if (!active) return
            val next = when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    Snapshot(State.CONNECTED, safeDeviceName(device))
                }
                BluetoothProfile.STATE_CONNECTING -> Snapshot(State.CONNECTING, safeDeviceName(device))
                BluetoothProfile.STATE_DISCONNECTING -> {
                    connectedDevice = null
                    Snapshot(State.DISCONNECTED, safeDeviceName(device))
                }
                else -> {
                    connectedDevice = null
                    Snapshot(State.DISCONNECTED, safeDeviceName(device))
                }
            }
            publish(next)
        }
    }

    fun addListener(listener: (Snapshot) -> Unit) {
        listeners += listener
        listener(snapshot)
    }

    fun removeListener(listener: (Snapshot) -> Unit) {
        listeners -= listener
    }

    @SuppressLint("MissingPermission")
    fun start() {
        active = true
        val currentProxy = hidDevice
        if (currentProxy != null) {
            if (!appRegistered && !registeringApp) {
                registerApp(currentProxy)
            } else {
                publish(snapshot)
            }
            return
        }
        if (openingProfile) {
            publish(snapshot)
            return
        }
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
        if (adapter == null || !adapter.isEnabled) {
            publish(Snapshot(State.FAILED))
            return
        }
        openingProfile = true
        publish(Snapshot(State.REGISTERING))
        if (!adapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)) {
            openingProfile = false
            publish(Snapshot(State.FAILED))
        }
    }

    @SuppressLint("MissingPermission")
    fun send(report: ByteArray): Boolean {
        val proxy = hidDevice ?: return false
        val device = connectedDevice ?: return false
        if (proxy.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED) return false
        return proxy.sendReport(device, BluetoothPenProtocol.REPORT_ID, report)
    }

    @SuppressLint("MissingPermission")
    fun close() {
        active = false
        openingProfile = false
        val proxy = hidDevice
        if (proxy != null) {
            if (appRegistered) proxy.unregisterApp()
            context.getSystemService(BluetoothManager::class.java).adapter
                .closeProfileProxy(BluetoothProfile.HID_DEVICE, proxy)
        }
        hidDevice = null
        connectedDevice = null
        appRegistered = false
        registeringApp = false
        publish(Snapshot(State.IDLE))
    }

    @SuppressLint("MissingPermission")
    private fun registerApp(proxy: BluetoothHidDevice) {
        if (appRegistered || registeringApp) return
        registeringApp = true
        publish(Snapshot(State.REGISTERING))
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "SideScreen Pen",
            "Pressure-sensitive stylus from SideScreen",
            "SideScreen",
            BluetoothHidDevice.SUBCLASS2_DIGITIZER_TABLET,
            BluetoothPenProtocol.reportDescriptor,
        )
        val accepted = proxy.registerApp(sdp, null, null, mainExecutor, callback)
        Log.i(TAG, "registerApp accepted=$accepted; awaiting status callback")
    }

    private fun publish(next: Snapshot) {
        snapshot = next
        listeners.forEach { it(next) }
    }

    @SuppressLint("MissingPermission")
    private fun safeDeviceName(device: BluetoothDevice?): String? =
        try {
            device?.name
        } catch (_: SecurityException) {
            null
        }

    private companion object {
        const val TAG = "SideScreenBluetoothPen"
    }
}
