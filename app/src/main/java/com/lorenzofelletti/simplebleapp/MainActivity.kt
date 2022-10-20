package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.util.Log
import android.widget.Toast
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities.dispatchOnRequestPermissionsResult

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartServer: Button

    private lateinit var advertisingService: PeripheralAdvertiseService
    private var bound = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PeripheralAdvertiseService.PeripheralAdvertiseBinder
            advertisingService = binder.getService()
            bound = true

            advertisingService.onServiceStopActions.add {
                bound = false
                updateBtnStartServerText()
            }

            updateBtnStartServerText()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            updateBtnStartServerText()
        }
    }

    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var gattServerManager: GattServerManager

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = getSystemService(BluetoothManager::class.java)
        btAdapter = btManager.adapter
        gattServerManager = GattServerManager(this, btManager)

        // Adding the onclick listener to the start server button
        btnStartServer = findViewById(R.id.btn_start_server)

        btnStartServer.setOnClickListener(
            startServerOnClickListenerBuilder(startServerAction = {
                Toast.makeText(this, "Starting the GATT server", Toast.LENGTH_SHORT).show()

                /* Checks if the required permissions are granted and starts the GATT server,
                 * requesting them otherwise. */
                when (PermissionsUtilities.checkPermissionsGranted(
                    this, AppRequiredPermissions.permissions
                )) {
                    true -> {
                        startGattServer()
                    }
                    false -> PermissionsUtilities.checkPermissions(
                        this, AppRequiredPermissions.permissions, BLE_SERVER_REQUEST_CODE
                    )
                }
            }, stopServerAction = {
                Toast.makeText(this, "Stopping the GATT server", Toast.LENGTH_SHORT).show()

                stopGattServer()
            })
        )
    }

    override fun onStop() {
        super.onStop()
        unbindFromAdvertiseService()
    }

    private fun bindToAdvertiseService() {
        if (DEBUG) Log.d(TAG, "Binding to the Advertise Service")

        if (!bound) {
            Intent(this, PeripheralAdvertiseService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun unbindFromAdvertiseService() {
        if (DEBUG) Log.d(TAG, "Unbinding from the Advertise Service")
        unbindService(connection)
        bound = false
    }

    /**
     * Starts the GATT server.
     *
     * It starts the advertising service and creates the GATT server.
     */
    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        //startAdvertising()
        bindToAdvertiseService()

        //setGattServer()
        gattServerManager.startGattServer()

        setBluetoothService(gattServerManager)
    }

    @SuppressLint("MissingPermission")
    private fun stopGattServer() {
        gattServerManager.stopGattServer()

        // stop advertising
        unbindFromAdvertiseService()
    }

    /**
     * Updates the text of the start server button according to the state of the advertising service.
     */
    private fun updateBtnStartServerText() {
        if (DEBUG) Log.d(TAG, "Updating the start server button text")

        btnStartServer.setText(if (bound) R.string.stop_server else R.string.start_server)
    }

    /**
     * Function that checks whether the permission was granted or not
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        dispatchOnRequestPermissionsResult(requestCode,
            permissions,
            grantResults,
            onGrantedMap = mapOf(BLE_SERVER_REQUEST_CODE to {
                startGattServer()
            }),
            onDeniedMap = mapOf(BLE_SERVER_REQUEST_CODE to {
                Toast.makeText(
                    this,
                    "Some permissions were not granted, please grant them and try again",
                    Toast.LENGTH_LONG
                ).show()
            })
        )
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val DEBUG: Boolean = BuildConfig.DEBUG

        private const val BLE_SERVER_REQUEST_CODE = 2
    }
}