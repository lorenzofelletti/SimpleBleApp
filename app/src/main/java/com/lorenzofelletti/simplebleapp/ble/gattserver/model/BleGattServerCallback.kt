package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.BuildConfig

class BleGattServerCallback : BluetoothGattServerCallback() {
    private var bluetoothGattServer: BluetoothGattServer? = null

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)

        if (DEBUG) Log.i(
            TAG, "${::onConnectionStateChange.name} - BluetoothDevice CONNECTED: $device"
        )
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        super.onServiceAdded(status, service)

        if (DEBUG) Log.i(
            TAG, "${::onServiceAdded.name} - BluetoothGattService ADDED: $service"
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        if (DEBUG) Log.i(
            TAG,
            "${::onCharacteristicReadRequest.name} - Read request for characteristic: $characteristic"
        )

        bluetoothGattServer?.sendResponse(
            device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic?.value
        )
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        if (DEBUG) Log.i(
            TAG,
            "${::onCharacteristicWriteRequest.name} - Write request for characteristic: $characteristic"
        )

        var success = false
        if (value != null && characteristic != null) {
            characteristic.value = value
            success = true
        }

        if (responseNeeded) {
            bluetoothGattServer?.sendResponse(
                device,
                requestId,
                if (success) BluetoothGatt.GATT_SUCCESS else BluetoothGatt.GATT_FAILURE,
                0,
                value
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        if (DEBUG) Log.i(
            TAG, "${::onDescriptorWriteRequest.name} - Write request for descriptor: $descriptor"
        )
    }

    companion object {
        private val TAG = BleGattServerCallback::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}