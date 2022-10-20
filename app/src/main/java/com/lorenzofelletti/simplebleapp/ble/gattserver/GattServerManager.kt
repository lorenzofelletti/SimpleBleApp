package com.lorenzofelletti.simplebleapp.ble.gattserver

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import androidx.annotation.RequiresPermission
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
        bluetoothGattServer = bluetoothManager.openGattServer(activity, gattServerCallback)
        gattServerCallback.bluetoothGattServer = bluetoothGattServer
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun addService(service: BluetoothGattService) {
        servicesMap[service.uuid] = service
        gattServerCallback.bluetoothGattServer?.addService(service)
    }

    fun addCharacteristicToService(serviceUUID: UUID, characteristic: BluetoothGattCharacteristic) {
        servicesMap[serviceUUID]?.addCharacteristic(characteristic)
        characteristicsMap[characteristic.uuid] = characteristic
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun stopGattServer() {
        gattServerCallback.bluetoothGattServer?.close()
    }

    /**
     * Sets the value of the characteristic.
     */
    @SuppressLint("MissingPermission")
    fun <T> setCharacteristic(characteristicUUID: UUID, value: T) {
        val characteristic = characteristicsMap[characteristicUUID]
        if (characteristic != null) {
            characteristic.value = CharacteristicUtilities.getValueAsByteArray(value)
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
        bluetoothConnectedDevices.forEach {
            bluetoothGattServer?.notifyCharacteristicChanged(it, characteristic, confirm)
        }
    }
}