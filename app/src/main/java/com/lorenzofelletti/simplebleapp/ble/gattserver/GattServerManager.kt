package com.lorenzofelletti.simplebleapp.ble.gattserver

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.ConnectedDeviceAdapterInterface
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.AbstractBleGattServerCallback
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.HasConnectedDevicesAdapter
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.HasConnectedDevicesMap
import java.util.UUID
import kotlin.reflect.KClass

/**
 * This class is used to manage the GattServer.
 *
 * @param context The context to use.
 * @param bluetoothConnectedDevices A reference to the list of connected devices, if null the list will be created.
 * @param adapter A reference to the adapter of the connected devices.
 * @param gattServerCallbackClass The class of the callback to use.
 */
class GattServerManager(
    private val context: Context,
    override var bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean> = mutableMapOf(),
    adapter: ConnectedDeviceAdapterInterface? = null,
    private val gattServerCallbackClass: KClass<out AbstractBleGattServerCallback>,
) : HasConnectedDevicesAdapter, HasConnectedDevicesMap {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)

    private var gattServerCallback =
        gattServerCallbackClass.constructors.first().call(adapter, bluetoothConnectedDevices)

    override var adapter = adapter
        set(value) {
            field = value
            gattServerCallback.adapter = value
        }

    private var bluetoothGattServer: BluetoothGattServer? = null

    private val servicesMap: MutableMap<UUID, BluetoothGattService> = mutableMapOf()
    private val characteristicsMap: MutableMap<UUID, BluetoothGattCharacteristic> = mutableMapOf()


    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun startGattServer() {
        if (DEBUG) Log.i(TAG, "${::startGattServer.name} - Starting GattServer...")

        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        gattServerCallback.bluetoothGattServer = bluetoothGattServer
    }

    /**
     * Adds a service to the GattServer.
     */
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun addService(service: BluetoothGattService) {
        if (DEBUG) Log.i(TAG, "${::addService.name} - BluetoothGattService ADDED: $service")

        servicesMap[service.uuid] = service
        bluetoothGattServer?.addService(service)
    }

    /**
     * Adds a characteristic to a service. The service must be added before adding the characteristic,
     * otherwise the characteristic will not be added.
     */
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
        adapter?.clearDevices()
        servicesMap.clear()
        characteristicsMap.clear()
    }

    /**
     * Sets the value of the characteristic.
     */
    @SuppressLint("MissingPermission")
    fun <T> setCharacteristic(characteristicUUID: UUID, value: T) {
        if (DEBUG) Log.i(
            TAG, "setCharacteristic - characteristicUUID: $characteristicUUID, value: $value"
        )

        val characteristic = characteristicsMap[characteristicUUID]
        if (characteristic != null) {
            if (DEBUG) Log.i(
                TAG, "setCharacteristic - setting characteristic ${characteristic.uuid} value"
            )
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
            TAG, "notifyCharacteristicChanged - characteristic: $characteristic, confirm: $confirm"
        )

        bluetoothConnectedDevices.forEach {
            bluetoothGattServer?.notifyCharacteristicChanged(it.key, characteristic, confirm)
        }
    }

    companion object {
        private val TAG = GattServerManager::class.java.simpleName
    }
}