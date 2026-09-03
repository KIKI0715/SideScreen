package com.sidescreen.app

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts

/** Experimental screen that advertises the tablet as a Bluetooth HID digitizer. */
class BluetoothPenSetupActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var bluetoothPenSession: BluetoothPenSession
    private val pressureCalibration by lazy {
        PreferencesManager(this).penPressureCalibration
    }

    private val sessionListener: (BluetoothPenSession.Snapshot) -> Unit = { snapshot ->
        val message = when (snapshot.state) {
            BluetoothPenSession.State.IDLE -> "Preparing Bluetooth pen…"
            BluetoothPenSession.State.REGISTERING -> "Registering SideScreen Pen…"
            BluetoothPenSession.State.READY ->
                "SideScreen Pen is discoverable.\n\n" +
                    "On the Mac, open Bluetooth settings and select this Galaxy Tab."
            BluetoothPenSession.State.CONNECTING ->
                "Connecting to ${snapshot.deviceName ?: "Mac"}…"
            BluetoothPenSession.State.CONNECTED ->
                "Connected to ${snapshot.deviceName ?: "Mac"}\n\n" +
                    "Draw here with the S Pen, or tap to open SideScreen."
            BluetoothPenSession.State.DISCONNECTED -> "Waiting for the Mac to connect…"
            BluetoothPenSession.State.FAILED -> "Bluetooth pen registration failed"
        }
        showStatus(message)
    }

    private val discoverableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            showStatus("Bluetooth discoverability is required for initial pairing")
        } else {
            openHidProfile()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean =
        if (sendPenEvent(event)) true else super.dispatchTouchEvent(event)

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean =
        if (sendPenEvent(event)) true else super.dispatchGenericMotionEvent(event)

    private fun sendPenEvent(event: MotionEvent): Boolean {
        if (!bluetoothPenSession.isConnected) return false
        if (event.pointerCount == 0) return false
        val pointerIndex = event.actionIndex.coerceIn(0, event.pointerCount - 1)
        if (event.getToolType(pointerIndex) != MotionEvent.TOOL_TYPE_STYLUS) return false

        val phase = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> BluetoothPenInput.Phase.CONTACT

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_HOVER_ENTER,
            MotionEvent.ACTION_HOVER_MOVE -> BluetoothPenInput.Phase.HOVER

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_HOVER_EXIT -> BluetoothPenInput.Phase.OUT_OF_RANGE

            else -> return false
        }

        val report = BluetoothPenInput.report(
            phase = phase,
            x = event.getX(pointerIndex),
            y = event.getY(pointerIndex),
            width = window.decorView.width,
            height = window.decorView.height,
            pressure = event.getPressure(pointerIndex),
            calibration = pressureCalibration,
        )
        if (!bluetoothPenSession.send(report)) {
            Log.w(TAG, "Bluetooth HID report was rejected")
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothPenSession = BluetoothPenSession(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        status = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 20f
            setPadding(48, 48, 48, 48)
            text = "Preparing Bluetooth pen…"
        }
        setContentView(status)
        status.setOnClickListener {
            if (bluetoothPenSession.isConnected) {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                )
            }
        }
        bluetoothPenSession.addListener(sessionListener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                ),
                REQUEST_BLUETOOTH,
            )
        } else if (bluetoothPenSession.isConnected) {
            bluetoothPenSession.start()
        } else {
            ensureDiscoverableThenOpenHidProfile()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            ensureDiscoverableThenOpenHidProfile()
        } else {
            showStatus("Nearby devices permission is required for Bluetooth pen mode")
        }
    }

    private fun ensureDiscoverableThenOpenHidProfile() {
        val adapter = getSystemService(BluetoothManager::class.java).adapter
        if (adapter == null || !adapter.isEnabled) {
            showStatus("Turn on Bluetooth, then reopen this screen")
            return
        }
        if (adapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            openHidProfile()
            return
        }
        showStatus("Allow this tablet to be discovered for initial Mac pairing")
        discoverableLauncher.launch(
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                DISCOVERABLE_SECONDS,
            )
        )
    }

    private fun openHidProfile() {
        val adapter = getSystemService(BluetoothManager::class.java).adapter
        if (adapter == null || !adapter.isEnabled) {
            showStatus("Turn on Bluetooth, then reopen this screen")
            return
        }
        showStatus("Registering SideScreen Pen…")
        bluetoothPenSession.start()
    }

    private fun showStatus(message: String) {
        Log.i(TAG, message.replace('\n', ' '))
        runOnUiThread { status.text = message }
    }

    override fun onStop() {
        bluetoothPenSession.close()
        super.onStop()
    }

    override fun onDestroy() {
        bluetoothPenSession.removeListener(sessionListener)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SideScreenBluetoothPen"
        private const val REQUEST_BLUETOOTH = 901
        private const val DISCOVERABLE_SECONDS = 300
    }
}
