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
import com.lorenzofelletti.simplebleapp.ble.gattserver.CharacteristicUtilities
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.BleGattServerCallback
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.Constants.UUID_MY_CHARACTERISTIC
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.Constants.UUID_MY_DESCRIPTOR
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.Constants.UUID_MY_SERVICE
import com.lorenzofelletti.simplebleapp.permissions.AppRequiredPermissions
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities
import com.lorenzofelletti.simplebleapp.permissions.PermissionsUtilities.dispatchOnRequestPermissionsResult

class MainActivity : AppCompatActivity() {
    private lateinit var advertisingService: PeripheralAdvertiseService
    private var bound = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PeripheralAdvertiseService.PeripheralAdvertiseBinder
            advertisingService = binder.getService()
            bound = true
            updateBtnStartServerText()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            updateBtnStartServerText()
        }
    }

    private lateinit var btnStartServer: Button

    private lateinit var btManager: BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var gattServerManager: GattServerManager

    private var bluetoothGattServer: BluetoothGattServer? = null
    private var myService: BluetoothGattService? = null
    private var myCharacteristic: BluetoothGattCharacteristic? = null

    private var bluetoothConnectedDevices: MutableSet<BluetoothDevice> = mutableSetOf()

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

    /*override fun onStart() {
        super.onStart()
        // bindToAdvertiseService()
    }*/

    override fun onStop() {
        super.onStop()
        unbindFromAdvertiseService()
    }

    private fun bindToAdvertiseService() {
        if (DEBUG) Log.d(::bindToAdvertiseService.name, "Binding to the Advertise Service")

        if (!bound) {
            Intent(this, PeripheralAdvertiseService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
            advertisingService.onServiceStopActions.add {
                bound = false
                updateBtnStartServerText()
            }
        }
    }

    private fun unbindFromAdvertiseService() {
        if (DEBUG) Log.d(::unbindFromAdvertiseService.name, "Unbinding from the Advertise Service")
        unbindService(connection)
        advertisingService.onServiceStopActions.clear()
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

        setBluetoothService()
    }

    @SuppressLint("MissingPermission")
    private fun stopGattServer() {
        // close the gatt server
        bluetoothGattServer?.close()
        bluetoothGattServer = null

        // clear the service, characteristic, and connected devices
        bluetoothConnectedDevices.clear()

        myService = null
        myCharacteristic = null

        // stop advertising
        unbindFromAdvertiseService()
    }

    /**
     * Updates the text of the start server button according to the state of the advertising service.
     */
    private fun updateBtnStartServerText() {
        btnStartServer.setText(if (bound) R.string.stop_server else R.string.start_server)
    }

    /*private fun startAdvertising() {
        if (DEBUG) Log.i(TAG, "${::startAdvertising.name} - Starting Advertising Service")

        bindToAdvertiseService()

        updateBtnStartServerText(true)

        //startService(getServiceIntent(this))
    }*/

    /*private fun stopAdvertising() {
        if (DEBUG) Log.i(TAG, "${::stopAdvertising.name} - Stopping Advertising Service")

        updateBtnStartServerText(false)

        //stopService(getServiceIntent(this))
    }*/

    /**
     * Returns the intent to start the [PeripheralAdvertiseService].
     *
     * @param context The context to use to create the intent.
     * @return The intent to start the [PeripheralAdvertiseService].
     */
    /*private fun getServiceIntent(context: Context): Intent {
        return Intent(context, PeripheralAdvertiseService::class.java)
    }*/

    /**
     * Creates the service and the characteristic to be added to the GATT server and
     * adds them to the GATT server.
     */
    @SuppressLint("MissingPermission")
    private fun setBluetoothService() {
        val myService = BluetoothGattService(
            UUID_MY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val myCharacteristic = BluetoothGattCharacteristic(
            UUID_MY_CHARACTERISTIC,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val myDescriptor = BluetoothGattDescriptor(
            UUID_MY_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_READ
        )
        myCharacteristic?.descriptors?.add(myDescriptor)

        //setCharacteristic(myCharacteristic!!, "Hello World!")

        myService?.addCharacteristic(myCharacteristic)

        //bluetoothGattServer?.addService(myService)
        gattServerManager.addService(myService)
        gattServerManager.addCharacteristicToService(myService.uuid, myCharacteristic)
        gattServerManager.setCharacteristic(myCharacteristic.uuid, "Hello World!")
    }

    /*@SuppressLint("MissingPermission")
    private fun setGattServer() {
        val callback = BleGattServerCallback(bluetoothConnectedDevices)

        bluetoothGattServer = btManager.openGattServer(this, callback)

        callback.bluetoothGattServer = bluetoothGattServer
    }*/

    /**
     * Sets the value of the characteristic.
     */
    @SuppressLint("MissingPermission")
    private fun <T> setCharacteristic(characteristic: BluetoothGattCharacteristic, value: T) {
        characteristic.value = CharacteristicUtilities.getValueAsByteArray(value)

        notifyCharacteristicChanged(characteristic)
    }

    /**
     * Notifies the connected devices that the characteristic has changed.
     */
    @SuppressLint("MissingPermission")
    private fun notifyCharacteristicChanged(
        characteristic: BluetoothGattCharacteristic, confirm: Boolean = false
    ) {
        bluetoothConnectedDevices.forEach {
            bluetoothGattServer?.notifyCharacteristicChanged(it, characteristic, confirm)
        }
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