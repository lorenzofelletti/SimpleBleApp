package com.lorenzofelletti.simplebleapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Button
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartServer: Button

    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter

    private var bluetoothGattServer: BluetoothGattServer? = null
    private var characteristicRead: BluetoothGattCharacteristic? = null

    private lateinit var thread: Thread
    private var runThread = false

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        var i = 0
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

            bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)

            val v = value?.get(0)
            if (v as Int == 1) {
                runThread = true
                thread = object : Thread() {
                    override fun run() {
                        while (runThread) {
                            try {
                                sleep(10)
                                if (device != null) {
                                    notifyData(device, byteArrayOf(i as Byte), false)
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
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting the BluetoothManager
        btManager = getSystemService(BluetoothManager::class.java)
        btAdapter = btManager.adapter

        // Adding the onclick listener to the start server button
        btnStartServer = findViewById(R.id.btn_start_server)
        btnStartServer.setOnClickListener {
            if (DEBUG) Log.i(TAG, "${it.javaClass.simpleName}:${it.id} - onClick event")
            Toast.makeText(this, "Start GATT server", Toast.LENGTH_SHORT).show()

            // Checks if the required permissions are granted and starts the scan if so, otherwise it requests them
            when (PermissionsUtilities.checkPermissionsGranted(
                this,
                AppRequiredPermissions.permissions
            )) {
                true -> {
                    startGattServer()
                }
                false -> PermissionsUtilities.checkPermissions(
                    this, AppRequiredPermissions.permissions, BLE_SERVER_REQUEST_CODE
                )
            }
        }

    }

    override fun onPause() {
        super.onPause()
        //TODO: Stop the GATT server
        //gattServerManager.closeGattServer()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            bluetoothGattServer?.close()
        }
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (PermissionsUtilities.checkRequestedPermissionsResults(permissions, grantResults)) {
            true -> {
                if (DEBUG) {
                    Log.d(
                        TAG, "${::onRequestPermissionsResult.name} - $permissions granted!"
                    )
                }

                // Dispatch what to do after the permissions are granted
                when (requestCode) {
                    BLE_SERVER_REQUEST_CODE -> startGattServer()
                }
            }
            false -> {
                if (DEBUG) {
                    Log.d(
                        TAG,
                        "${::onRequestPermissionsResult.name} - some permissions in $permissions were not granted"
                    )
                }

                Toast.makeText(
                    this,
                    "Some permissions were not granted, please grant them and try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        // Start GATT server
        val advertiseSettings = AdvertiseSettings.Builder()
            //.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            //.setTimeout(0)
            //.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .build()
        val scanResponseData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID_SERVICE))
            .setIncludeTxPowerLevel(false)
            .build()
        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                if (DEBUG) Log.i(TAG, "LE Advertise Started.")
                initServices(applicationContext)
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                if (DEBUG) Log.e(TAG, "LE Advertise Failed: $errorCode")
            }
        }

        val bleAdvertiser = btAdapter.bluetoothLeAdvertiser
        bleAdvertiser.startAdvertising(advertiseSettings, advertiseData, scanResponseData, callback)
    }

    @SuppressLint("MissingPermission")
    fun initServices(context: Context) {
        bluetoothGattServer = btManager.openGattServer(context, gattServerCallback)
    }

    @SuppressLint("MissingPermission")
    private fun onResponseToClient(
        requestBytes: ByteArray,
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (DEBUG) Log.i(TAG, "${::onResponseToClient.name} - Sending response to client")

        val str = "Hello"
        characteristicRead?.value = str.toByteArray()
        bluetoothGattServer?.notifyCharacteristicChanged(device, characteristicRead, false)
    }

    @SuppressLint("MissingPermission")
    private fun notifyData(device: BluetoothDevice, value: ByteArray, confirm: Boolean) {
        var characteristic: BluetoothGattCharacteristic? = null
        for (service in bluetoothGattServer?.services!!) {
            for (iCharacteristic in service.characteristics) {
                if (iCharacteristic.uuid == UUID_DESCRIPTOR) {
                    characteristic = iCharacteristic
                    break
                }
            }
        }
        if (characteristic != null) {
            characteristic.value = value
            bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, confirm)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG

        private const val BLE_SERVER_REQUEST_CODE = 2
        private val UUID_SERVICE = UUID.fromString("23782c92-139c-4846-aac5-31d1b078d439")
        private val UUID_READ_CHARACTERISTIC =
            UUID.fromString("23782c92-139c-4846-aac5-31d1b078d440")
        private val UUID_DESCRIPTOR =
            UUID.fromString("23782c92-139c-4846-aac5-31d1b078d441")
    }
}