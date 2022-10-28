package com.lorenzofelletti.simplebleapp.ble.gattserver.model

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
import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.ConnectedDeviceAdapter

/**
 * This class is used to handle the callbacks of the GattServer.
 *
 * @param bluetoothConnectedDevices A reference to the list of connected devices.
 */
class BleGattServerCallback(
    private val bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean>,
    private val adapter: ConnectedDeviceAdapter
) : BluetoothGattServerCallback() {
    var bluetoothGattServer: BluetoothGattServer? = null

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
                    adapter.addDevice(device)
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    if (DEBUG) Log.i(
                        TAG,
                        "${::onConnectionStateChange.name} - BluetoothDevice DISCONNECTED: $device"
                    )
                    adapter.removeDevice(device)
                }
            }
        } else {
            if (DEBUG) Log.w(TAG, "${::onConnectionStateChange.name} - Error: $status")

            adapter.removeDevice(device)
        }
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        super.onNotificationSent(device, status)

        if (DEBUG) Log.i(
            TAG, "${::onNotificationSent.name} - BluetoothDevice NOTIFICATION SENT: $device}"
        )
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        super.onServiceAdded(status, service)

        if (DEBUG) Log.i(
            TAG, "${::onServiceAdded.name} - BluetoothGattService ADDED: ${service?.uuid}"
        )
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        if (DEBUG) Log.i(
            TAG,
            "${::onCharacteristicReadRequest.name} - Read request for characteristic: ${characteristic?.uuid}"
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
            "${::onCharacteristicWriteRequest.name} - Write request for characteristic: ${characteristic?.uuid}"
        )

        var success = false
        if (canWrite(characteristic)) {
            characteristic?.value = value
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

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    override fun onDescriptorReadRequest(
        device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?
    ) {
        if (DEBUG) Log.i(
            TAG,
            "${::onDescriptorReadRequest.name} - Read request for descriptor: ${descriptor?.uuid}"
        )

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
        if (DEBUG) Log.i(
            TAG,
            "${::onDescriptorWriteRequest.name} - Write request for descriptor: ${descriptor?.uuid}"
        )

        /*
        This is called when a client subscribes or unsubscribes to notifications/indications.
        A first call to this method is made when the client subscribes, and a second call is made
        when the client unsubscribes.
        */
        adapter.toggleDeviceNotification(device!!)

        if (responseNeeded) {
            bluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value
            )
        }
    }

    private fun canWrite(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristic != null && characteristic.permissions and BluetoothGattCharacteristic.PERMISSION_WRITE == BluetoothGattCharacteristic.PERMISSION_WRITE
    }

    private fun getDeviceNotificationState(device: BluetoothDevice?): ByteArray? {
        return if (bluetoothConnectedDevices[device] == true)
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else
            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
    }

    companion object {
        private val TAG = BleGattServerCallback::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}