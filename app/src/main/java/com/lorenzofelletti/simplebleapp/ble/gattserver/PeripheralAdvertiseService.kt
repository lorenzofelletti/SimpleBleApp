package com.lorenzofelletti.simplebleapp.ble.gattserver

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.BleAdvertiseCallback
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.Constants
import java.util.concurrent.TimeUnit

class PeripheralAdvertiseService(
    private var advertiseCallback: AdvertiseCallback = BleAdvertiseCallback()
) :
    Service() {
    private var running = false

    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private val handler: Handler = Handler()
    private val timeoutRunnable: Runnable = Runnable { stopSelf() }

    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    override fun onCreate() {
        super.onCreate()
        running = true
        initialize()
        startAdvertising()
        setTimeout()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        running = false
        stopAdvertising()
        handler.removeCallbacks(timeoutRunnable)
        stopForeground(true)
        super.onDestroy()
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private fun stopAdvertising() {
        if (DEBUG) Log.d(TAG, "${::stopAdvertising.name} - Stopping advertising")

        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

    /**
     * Starts a delayed [Runnable] that will stop this service after [TIMEOUT] time.
     */
    private fun setTimeout() {
        handler.postDelayed(timeoutRunnable, TIMEOUT)
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    private fun startAdvertising() {
        if (DEBUG) Log.d(TAG, "${::startAdvertising.name} - Starting advertising")

        val settings: AdvertiseSettings = buildAdvertiseSettings()
        val data: AdvertiseData = buildAdvertiseData()
        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
    }

    private fun buildAdvertiseData(): AdvertiseData {
        return AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(Constants.UUID_SERVICE))
            .setIncludeDeviceName(true)
            .build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(true)
            .setTimeout(0)
            .build()
    }

    /**
     * Initializes the [BluetoothLeAdvertiser].
     */
    private fun initialize() {
        bluetoothLeAdvertiser = getSystemService(Context.BLUETOOTH_SERVICE).let {
            (it as BluetoothManager).adapter.bluetoothLeAdvertiser
        }
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    override fun onBind(intent: android.content.Intent?): android.os.IBinder? {
        return null
    }

    companion object {
        private val TAG = PeripheralAdvertiseService::class.java.simpleName
        private val DEBUG = com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG

        /**
         * Length of time to advertise for, in milliseconds.
         */
        private var TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)
    }
}