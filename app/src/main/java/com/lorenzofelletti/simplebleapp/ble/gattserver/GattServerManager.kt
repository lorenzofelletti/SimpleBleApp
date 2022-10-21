package com.lorenzofelletti.simplebleapp.ble.gattserver

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.BuildConfig
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.BleGattServerCallback
import java.util.UUID

class GattServerManager(
    private val activity: Activity,
    private val bluetoothManager: BluetoothManager
) {
    private val bluetoothConnectedDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    private val gattServerCallback = BleGattServerCallback(bluetoothConnectedDevices)
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val servicesMap: MutableMap<UUID, BluetoothGattService> = mutableMapOf()
    private val characteristicsMap: MutableMap<UUID, BluetoothGattCharacteristic> = mutableMapOf()

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun startGattServer() {
        if (DEBUG) Log.i(TAG, "${::startGattServer.name} - Starting GattServer...")

        bluetoothGattServer = bluetoothManager.openGattServer(activity, gattServerCallback)
        gattServerCallback.bluetoothGattServer = bluetoothGattServer
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun addService(service: BluetoothGattService) {
        if (DEBUG) Log.i(TAG, "${::addService.name} - BluetoothGattService ADDED: $service")

        servicesMap[service.uuid] = service
        bluetoothGattServer?.addService(service)
    }

    fun addCharacteristicToService(serviceUUID: UUID, characteristic: BluetoothGattCharacteristic) {
        if (DEBUG) Log.i(
            TAG,
            "${::addCharacteristicToService.name} - Adding characteristic to service: $serviceUUID"
        )

        servicesMap[serviceUUID]?.addCharacteristic(characteristic)
        characteristicsMap[characteristic.uuid] = characteristic
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun stopGattServer() {
        if (DEBUG) Log.i(TAG, "Stopping GattServer")

        bluetoothGattServer?.close()
    }

    /**
     * Sets the value of the characteristic.
     */
    @SuppressLint("MissingPermission")
    fun <T> setCharacteristic(characteristicUUID: UUID, value: T) {
        if (DEBUG) Log.i(
            TAG,
            "setCharacteristic - characteristicUUID: $characteristicUUID, value: $value"
        )

        val characteristic = characteristicsMap[characteristicUUID]
        if (characteristic != null) {
            characteristic.value = getValueAsByteArray(value)
            notifyCharacteristicChanged(characteristic)
        }
    }

    /**
     * Notifies the connected devices that the characteristic has changed.
     */
    @SuppressLint("MissingPermission")
    private fun notifyCharacteristicChanged(
        characteristic: BluetoothGattCharacteristic, confirm: Boolean = false
    ) {
        if (DEBUG) Log.i(
            TAG,
            "notifyCharacteristicChanged - characteristic: $characteristic, confirm: $confirm"
        )

        bluetoothConnectedDevices.forEach {
            bluetoothGattServer?.notifyCharacteristicChanged(it, characteristic, confirm)
        }
    }

    companion object {
        private val TAG = GattServerManager::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}