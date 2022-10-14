package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import com.lorenzofelletti.simplebleapp.BuildConfig
import kotlin.reflect.KFunction4

class BleGattServerCallback(
    private val onResponseToClient: KFunction4<ByteArray, BluetoothDevice, Int, BluetoothGattCharacteristic, Unit>,
    private val notifyData: (BluetoothDevice, ByteArray, Boolean) -> Unit
) : BluetoothGattServerCallback() {
    private var bluetoothGattServer: BluetoothGattServer? = null
    private lateinit var thread: Thread
    private var runThread = false
    private var i = 0

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)

        if (DEBUG) Log.i(
            TAG,
            "${::onConnectionStateChange.name} - BluetoothDevice CONNECTED: $device"
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
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

        if (DEBUG) Log.i(
            TAG,
            "${::onCharacteristicReadRequest.name} - Read request for characteristic: $characteristic"
        )
        bluetoothGattServer?.sendResponse(
            device,
            requestId,
            BluetoothGatt.GATT_SUCCESS,
            offset,
            characteristic?.value
        )
    }

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

        if (DEBUG) Log.i(
            TAG,
            "${::onCharacteristicWriteRequest.name} - Write request for characteristic: $characteristic"
        )

        if (value != null && device != null && characteristic != null) {
            onResponseToClient(value, device, requestId, characteristic)
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
        super.onDescriptorWriteRequest(
            device,
            requestId,
            descriptor,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )
        if (DEBUG) Log.i(
            TAG,
            "${::onDescriptorWriteRequest.name} - Write request for descriptor: $descriptor"
        )

        //TODO: consider moving this in the if below
        if (responseNeeded) {
            bluetoothGattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                value
            )
        }

        val v = value?.get(0)?.toInt()
        if (v != null && v == 1) {
            runThread = true
            thread = object : Thread() {
                override fun run() {
                    while (runThread) {
                        try {
                            sleep(10)
                            if (device != null) {
                                notifyData(device, byteArrayOf(i.toByte()), false)
                            }
                            i++
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            thread.start()
        } else {
            runThread = false
            thread.interrupt()
        }
    }

    companion object {
        private val TAG = BleGattServerCallback::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}