package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.widget.Toast
import com.lorenzofelletti.simplebleapp.ble.gattserver.CharacteristicUtilities
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.BleGattServerCallback
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.Constants.UUID_MY_CHARACTERISTIC
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.Constants.UUID_MY_SERVICE
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartServer: Button

    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter

    private var bluetoothGattServer: BluetoothGattServer? = null
    private var myService: BluetoothGattService? = null
    private var myCharacteristic: BluetoothGattCharacteristic? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = getSystemService(BluetoothManager::class.java)
        btAdapter = btManager.adapter

        // Adding the onclick listener to the start server button
        btnStartServer = findViewById(R.id.btn_start_server)

        btnStartServer.setOnClickListener(
            startServerOnClickListenerBuilder(
                startServer = {
                    Toast.makeText(this, "Starting the GATT server", Toast.LENGTH_SHORT).show()

                    /* Checks if the required permissions are granted and starts the GATT server,
                     * requesting them otherwise. */
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
                },
                stopServer = {
                    Toast.makeText(this, "Stopping the GATT server", Toast.LENGTH_SHORT).show()

                    stopAdvertising()
                })
        )
    }

    /**
     * Starts the GATT server.
     *
     * It starts the advertising service and creates the GATT server.
     */
    private fun startGattServer() {
        startAdvertising()
        setGattServer()
        setBluetoothService()
    }

    private fun updateBtnStartServerText(isAdvertisingOn: Boolean) {
        btnStartServer.setText(if (isAdvertisingOn) R.string.stop_server else R.string.start_server)
    }

    private fun startAdvertising() {
        if (DEBUG) Log.i(TAG, "${::startAdvertising.name} - Starting Advertising Service")

        updateBtnStartServerText(true)

        startService(getServiceIntent(this))
    }

    private fun stopAdvertising() {
        if (DEBUG) Log.i(TAG, "${::stopAdvertising.name} - Stopping Advertising Service")

        updateBtnStartServerText(false)

        stopService(getServiceIntent(this))
    }

    /**
     * Returns the intent to start the [PeripheralAdvertiseService].
     *
     * @param context The context to use to create the intent.
     * @return The intent to start the [PeripheralAdvertiseService].
     */
    private fun getServiceIntent(context: Context): Intent {
        return Intent(context, PeripheralAdvertiseService::class.java)
    }

    /**
     * Creates the service and the characteristic to be added to the GATT server and
     * adds them to the GATT server.
     */
    @SuppressLint("MissingPermission")
    private fun setBluetoothService() {
        myService = BluetoothGattService(
            UUID_MY_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        myCharacteristic = BluetoothGattCharacteristic(
            UUID_MY_CHARACTERISTIC,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        setCharacteristic("Hello World!")

        myService?.addCharacteristic(myCharacteristic)

        bluetoothGattServer?.addService(myService)
    }

    private fun <T> setCharacteristic(value: T) {
        myCharacteristic?.value = CharacteristicUtilities.getValueAsByteArray(value)
    }


    @SuppressLint("MissingPermission")
    private fun setGattServer() {
        val callback = BleGattServerCallback()

        bluetoothGattServer = btManager.openGattServer(this, callback)

        callback.bluetoothGattServer = bluetoothGattServer
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

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG

        private const val BLE_SERVER_REQUEST_CODE = 2
    }
}