package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.view.View
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager

private val DEBUG = BuildConfig.DEBUG

/**
 * A function that builds an [android.view.View.OnClickListener] for starting/stopping the BLE server having
 * two different behaviors depending on whether a server is already running or not.
 *
 * @param initialServerState The initial state of the server.
 * @param startServerAction A lambda that starts the server.
 * @param stopServerAction A lambda that stops the server.
 * @return An [android.view.View.OnClickListener] that starts the server if it is not already
 */
fun startServerOnClickListenerBuilder(
    initialServerState: Boolean = false,
    startServerAction: () -> Unit,
    stopServerAction: () -> Unit
): (View) -> Unit {
    var isServerStarted = initialServerState
    return fun(v: View) {
        if (DEBUG) android.util.Log.d(
            ::startServerOnClickListenerBuilder.name,
            "${v.javaClass.simpleName}:${v.id} - ${if (isServerStarted) "Stop" else "Start"} Server Button Clicked"
        )

        isServerStarted = if (isServerStarted) {
            stopServerAction()
            false
        } else {
            startServerAction()
            true
        }
    }
}

/**
 * Creates the service and the characteristic to be added to the GATT server and
 * adds them to the GATT server.
 */
@SuppressLint("MissingPermission")
fun setBluetoothService(gattServerManager: GattServerManager) {
    val myService = BluetoothGattService(
        Constants.UUID_MY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY
    )
    val myCharacteristic = BluetoothGattCharacteristic(
        Constants.UUID_MY_CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ
    )

    val myDescriptor = BluetoothGattDescriptor(
        Constants.UUID_MY_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_READ
    )
    myCharacteristic.descriptors?.add(myDescriptor)


    myService.addCharacteristic(myCharacteristic)

    gattServerManager.addService(myService)
    gattServerManager.addCharacteristicToService(myService.uuid, myCharacteristic)
    gattServerManager.setCharacteristic(myCharacteristic.uuid, "Hello World!")
}