package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager
import com.lorenzofelletti.simplebleapp.blescriptrunner.viewmodel.GattServerManagerViewModel
import com.lorenzofelletti.simplebleapp.blescriptrunner.Constants
import com.lorenzofelletti.simplebleapp.blescriptrunner.model.BleGattServerCallback
import com.lorenzofelletti.simplebleapp.fragments.ConnectedDevices
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities.dispatchOnRequestPermissionsResult

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartServer: Button
    private lateinit var btnSendNotification: Button

    private lateinit var gattServerManager: GattServerManager
    private val gattServerManagerViewModel: GattServerManagerViewModel by viewModels()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionsUtilities.buildRequestResultsDispatcher {
            onGranted(BLE_SERVER_REQUEST_CODE) {
                startGattServer()
            }
            onDenied(BLE_SERVER_REQUEST_CODE) {
                Toast.makeText(
                    this@MainActivity,
                    "Some permissions were not granted, please grant them and try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnStartServer = findViewById(R.id.btn_start_server)
        btnSendNotification = findViewById(R.id.btn_send_notification)
        val etScriptName: EditText = findViewById(R.id.et_script_name)

        GattServerManager(this, gattServerCallbackClass = BleGattServerCallback::class).also {
            gattServerManager = it
            gattServerManagerViewModel.gattServerManager = it
        }

        // Adding the onclick listener to the start server button
        btnStartServer.setOnClickListener {
            if (!gattServerManager.isRunning) {
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
            } else {
                stopGattServer()
            }
        }

        btnSendNotification.setOnClickListener {
            if (DEBUG) Log.i(TAG, "Sending notification")

            Toast.makeText(this, "Sending notification", Toast.LENGTH_SHORT).show()

            val scriptName = etScriptName.text.toString()
            gattServerManager.setCharacteristic(
                Constants.UUID_SCRIPT_RUNNER_CHARACTERISTIC, scriptName
            )
        }

        // setting up the connected devices fragment
        val connectedDevicesFragment = ConnectedDevices.newInstance()
        supportFragmentManager.beginTransaction().add(
            R.id.frameLayout, connectedDevicesFragment, ConnectedDevices::class.java.simpleName
        ).commit()
    }

    override fun onDestroy() {
        stopGattServer()

        super.onDestroy()
    }

    private fun updateGuiOnServerStateChange() {
        updateBtnStartServerText()
        changeNotificationBtnEnableState()
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        gattServerManager.startGattServer()
        setBluetoothService(gattServerManager)
        updateGuiOnServerStateChange()
    }

    @SuppressLint("MissingPermission")
    private fun stopGattServer() {
        gattServerManager.stopGattServer()
        updateGuiOnServerStateChange()
    }

    /**
     * Updates the text of the start server button according to the state of the advertising service.
     */
    private fun updateBtnStartServerText() {
        if (DEBUG) Log.d(TAG, "Updating the start server button text")

        btnStartServer.setText(if (gattServerManager.isRunning) R.string.stop_server else R.string.start_server)
    }

    private fun changeNotificationBtnEnableState() {
        btnSendNotification.isEnabled = gattServerManager.isRunning
    }

    /**
     * Function that checks whether the permission was granted or not.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        dispatchOnRequestPermissionsResult(
            requestCode,
            grantResults,
        )
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val BLE_SERVER_REQUEST_CODE = 2
    }
}