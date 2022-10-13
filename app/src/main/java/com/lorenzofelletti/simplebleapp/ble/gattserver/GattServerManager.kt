package com.lorenzofelletti.simplebleapp.ble.gattserver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.util.Log
import com.lorenzofelletti.simplebleapp.BuildConfig
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.BleAdvertiseCallback
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.BleGattServerCallback

class GattServerManager(
    private val bluetoothManager: BluetoothManager,
    private val context: Context,
) {
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

    //@SuppressLint("MissingPermission")
    private var bluetoothGattServer: BluetoothGattServer? = null

    private lateinit var bleGattServerCallback: BluetoothGattServerCallback

    /**
     * Starts advertising the given service.
     *
     * @param service The service to be advertised
     * @param advertiseSettings The settings to be used for advertising
     * @param advertiseData The data to be advertised
     * @param advertiseCallback The callback to be used for advertising
     */
    @SuppressLint("MissingPermission")
    fun addServiceAndStartAdvertising(
        service: BluetoothGattService,
        advertiseSettings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        advertiseCallback: AdvertiseCallback
    ) {
        setupGattServer()
        addService(service)
        startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun setupGattServer() {
        if (DEBUG) Log.d(TAG, "${::setupGattServer.name} - Setting up Gatt Server")
        if (bluetoothGattServer != null) {
            if (DEBUG) Log.d(TAG, "${::setupGattServer.name} - Gatt Server already initialized")
            return
        }
        bluetoothGattServer = bluetoothManager.openGattServer(context, bleGattServerCallback)
        bleGattServerCallback = BleGattServerCallback(bluetoothGattServer!!)
    }

    /**
     * Adds a service to the GATT server
     */
    @SuppressLint("MissingPermission")
    fun addService(service: BluetoothGattService) {
        if (DEBUG) Log.d(TAG, "${::addService.name} - Adding service: ${service.uuid}")
        bluetoothGattServer?.addService(service)
    }

    /**
     * Close the GATT server and stop advertising.
     */
    @SuppressLint("MissingPermission")
    fun closeGattServer() {
        if (DEBUG) Log.d(TAG, "${::closeGattServer.name} - Closing GattServer")
        stopAdvertising()
        bluetoothGattServer?.close()
        bluetoothGattServer = null
    }

    /**
     * Clears the services from the GATT server.
     */
    @SuppressLint("MissingPermission")
    fun clearServices() {
        bluetoothGattServer?.clearServices()
    }

    /**
     * Start advertising the service
     *
     * @param advertiseSettings the settings for the advertising
     * @param advertiseData the data to advertise
     * @param advertiseCallback the callback to be notified when the advertising starts
     */
    @SuppressLint("MissingPermission")
    fun startAdvertising(
        advertiseSettings: AdvertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED).setConnectable(true)
            .setTimeout(0).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW).build(),
        advertiseData: AdvertiseData = AdvertiseData.Builder().setIncludeDeviceName(true).build(),
        advertiseCallback: AdvertiseCallback = BleAdvertiseCallback()
    ) {
        if (bluetoothAdapter.isMultipleAdvertisementSupported) {
            bleAdvertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        } else {
            throw UnsupportedOperationException("Multiple advertisement not supported")
        }
    }

    /**
     * Stops advertising.
     */
    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        if (DEBUG) Log.d(TAG, "${::stopAdvertising.name} - Stopping advertising")
        bleAdvertiser?.stopAdvertising(BleAdvertiseCallback())
    }

    companion object {
        private val TAG = GattServerManager::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}