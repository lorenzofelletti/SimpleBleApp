package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.lorenzofelletti.simplebleapp.BuildConfig

class BleGattServerCallback(private val bleGattServer: BluetoothGattServer) : BluetoothGattServerCallback() {
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            if (DEBUG) Log.i(
                TAG, "${::onConnectionStateChange.name} - BluetoothDevice CONNECTED: $device"
            )
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            if (DEBUG) Log.i(
                TAG, "${::onConnectionStateChange.name} - BluetoothDevice DISCONNECTED: $device"
            )
            //TODO: Remove device from any active subscriptions
        }
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (DEBUG) Log.i(
            TAG, "${::onCharacteristicReadRequest.name} - Read request for characteristic: $characteristic"
        )
        //TODO: Implement this
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor
    ) {
        if (DEBUG) Log.i(
            TAG, "${::onDescriptorReadRequest.name} - Read request for descriptor: $descriptor"
        )
        //TODO: Implement this
    }

    companion object {
        private val DEBUG = BuildConfig.DEBUG
        private val TAG = BleGattServerCallback::class.java.simpleName
    }
}