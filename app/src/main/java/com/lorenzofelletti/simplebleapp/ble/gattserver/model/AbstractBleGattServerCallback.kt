package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.bluetooth.*
import android.util.Log
import androidx.annotation.RequiresPermission
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG


abstract class AbstractBleGattServerCallback(override var bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean>) :
    BluetoothGattServerCallback(), HasConnectedDevicesMap, HasConnectedDevicesAdapter {
    var bluetoothGattServer: BluetoothGattServer? = null

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

        val readPermitted =
            characteristic?.permissions?.and(BluetoothGattCharacteristic.PERMISSION_READ) != 0
        val gattSuccess = BluetoothGatt.GATT_SUCCESS
        val gattReadNotPermitted = BluetoothGatt.GATT_READ_NOT_PERMITTED

        bluetoothGattServer?.sendResponse(
            device,
            requestId,
            if (readPermitted) gattSuccess else gattReadNotPermitted,
            offset,
            if (readPermitted) characteristic?.value else null
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
    }

    private fun canWrite(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristic != null && characteristic.permissions and BluetoothGattCharacteristic.PERMISSION_WRITE == BluetoothGattCharacteristic.PERMISSION_WRITE
    }

    companion object {
        private val TAG = AbstractBleGattServerCallback::class.java.simpleName
    }
}