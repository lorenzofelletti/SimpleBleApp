package com.lorenzofelletti.simplebleapp.blescriptrunner.model

import android.bluetooth.*
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.ConnectedDeviceAdapterInterface
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.AbstractBleGattServerCallback

class BleGattServerCallback(
    override var adapter: ConnectedDeviceAdapterInterface?,
    bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean>
) : AbstractBleGattServerCallback(bluetoothConnectedDevices) {
    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)

        if (device == null) {
            if (DEBUG) Log.e(TAG, "${::onConnectionStateChange.name} - device is null")
            return
        }

        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    if (DEBUG) Log.i(
                        TAG,
                        "${::onConnectionStateChange.name} - BluetoothDevice CONNECTED: $device"
                    )
                    adapter?.addDevice(device)
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    if (DEBUG) Log.i(
                        TAG,
                        "${::onConnectionStateChange.name} - BluetoothDevice DISCONNECTED: $device"
                    )
                    adapter?.removeDevice(device)
                }
            }
        } else {
            if (DEBUG) Log.w(TAG, "${::onConnectionStateChange.name} - Error: $status")

            adapter?.removeDevice(device)
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    override fun onDescriptorReadRequest(
        device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?
    ) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor)

        val returnValue = getDeviceNotificationState(device)
        bluetoothGattServer?.sendResponse(
            device, requestId, BluetoothGatt.GATT_SUCCESS, 0, returnValue
        )
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onDescriptorWriteRequest(
            device, requestId, descriptor, preparedWrite, responseNeeded, offset, value
        )

        /*
        This is called when a client subscribes or unsubscribes to notifications/indications.
        A first call to this method is made when the client subscribes, and a second call is made
        when the client unsubscribes.
        */
        adapter?.toggleDeviceNotification(device!!)

        if (responseNeeded) {
            bluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value
            )
        }
    }

    private fun getDeviceNotificationState(device: BluetoothDevice?): ByteArray? {
        return if (bluetoothConnectedDevices[device] == true) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
    }

    companion object {
        private val TAG = BleGattServerCallback::class.java.simpleName
    }
}