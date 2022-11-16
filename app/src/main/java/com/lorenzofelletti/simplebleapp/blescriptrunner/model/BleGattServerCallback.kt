package com.lorenzofelletti.simplebleapp.blescriptrunner.model

import android.bluetooth.*
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.interfaces.ConnectedDeviceAdapterInterface
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.AbstractBleGattServerCallback
import com.lorenzofelletti.simplebleapp.blescriptrunner.Constants
import com.lorenzofelletti.simplebleapp.blescriptrunner.Constants.UUID_SCRIPT_RESULTS_CHARACTERISTIC
import com.lorenzofelletti.simplebleapp.blescriptrunner.ExecutionStatus

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

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

        if (device != null && characteristic?.uuid == UUID_SCRIPT_RESULTS_CHARACTERISTIC) {
            if (DEBUG) Log.i(TAG, "${::onCharacteristicReadRequest.name} - calling blinkDevice")
            adapter?.blinkDevice(device, ExecutionStatus.RUNNING.value, -1)
        }
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
        super.onCharacteristicWriteRequest(
            device,
            requestId,
            characteristic,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )

        if (device != null && characteristic?.uuid == UUID_SCRIPT_RESULTS_CHARACTERISTIC) {
            val executionResult = String(value ?: byteArrayOf(Constants.EXIT_CODE_FAILURE.toByte()))
            val blinkStatus =
                if (executionResult == Constants.EXIT_CODE_SUCCESS) ExecutionStatus.FINISHED_SUCCESS.value else ExecutionStatus.FINISHED_ERROR.value
            adapter?.blinkDevice(device, blinkStatus, Constants.BLINKING_DURATION)
        }
    }

    private fun getDeviceNotificationState(device: BluetoothDevice?): ByteArray? {
        return if (bluetoothConnectedDevices[device] == true) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
    }

    companion object {
        private val TAG = BleGattServerCallback::class.java.simpleName
    }
}