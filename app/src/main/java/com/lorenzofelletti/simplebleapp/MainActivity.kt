package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.checkPermissions
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.doOnDenied
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.doOnGranted
import com.lorenzofelletti.permissions.dispatcher.DispatcherEntry.Companion.showRationaleDialog
import com.lorenzofelletti.permissions.dispatcher.RequestResultsDispatcher.Companion.withRequestCode
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager
import com.lorenzofelletti.simplebleapp.blescriptrunner.Constants
import com.lorenzofelletti.simplebleapp.blescriptrunner.model.BleGattServerCallback
import com.lorenzofelletti.simplebleapp.blescriptrunner.viewmodel.GattServerManagerViewModel
import com.lorenzofelletti.simplebleapp.fragments.ConnectedDevices
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions.GATT_SERVER_REQUEST_CODE


class MainActivity : AppCompatActivity() {
    private lateinit var btnStartServer: Button
    private lateinit var btnSendNotification: Button

    private lateinit var gattServerManager: GattServerManager
    private val gattServerManagerViewModel: GattServerManagerViewModel by viewModels()

    private val permissionManager: PermissionManager by lazy {
        PermissionManager(this)
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val btStateChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_ON -> btnStartServer.isEnabled = true
                    BluetoothAdapter.STATE_OFF -> {
                        if (gattServerManager.isRunning) stopGattServer()
                    }
                }
            }
        }
    }

    private val enableBtIntentActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    if (DEBUG) Log.d(TAG, "Bluetooth enabled")

                    changeServerState()
                }
                RESULT_CANCELED -> {
                    if (DEBUG) Log.d(TAG, "Bluetooth not enabled")

                    userRefusedToEnableBluetooth()
                }
            }
        }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(btStateChangedReceiver, filter)

        permissionManager.buildRequestResultsDispatcher {
            withRequestCode(GATT_SERVER_REQUEST_CODE) {
                checkPermissions(AppRequiredPermissions.gattServerPermissions)
                showRationaleDialog(AppRequiredPermissions.gattServerRationaleMessage)
                doOnGranted { startGattServer() }
                doOnDenied {
                    Toast.makeText(
                        this@MainActivity,
                        "Some permissions were not granted, please grant them and try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
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
            if (!bluetoothAdapter!!.isEnabled) {
                showEnableBluetoothDialog({
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBtIntentActivityResultLauncher.launch(enableBtIntent)
                }, ::userRefusedToEnableBluetooth)
            } else {
                changeServerState()
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

    private fun changeServerState() {
        if (!gattServerManager.isRunning) {
            /* Checks if the required permissions are granted and starts the GATT server if so,
             * requesting them otherwise. */
            permissionManager.checkRequestAndDispatch(GATT_SERVER_REQUEST_CODE)
        } else {
            stopGattServer()
        }
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

        permissionManager.dispatchOnRequestPermissionsResult(
            requestCode,
            grantResults,
        )
    }

    private fun userRefusedToEnableBluetooth() {
        Toast.makeText(
            this,
            getString(R.string.user_refused_to_enable_bt_toast),
            Toast.LENGTH_LONG
        ).show()
        stopGattServer()
        btnStartServer.isEnabled = false
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}