package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager

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
        Constants.UUID_MY_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
    )
    myCharacteristic.descriptors?.add(myDescriptor)


    myService.addCharacteristic(myCharacteristic)

    gattServerManager.addService(myService)
    gattServerManager.addCharacteristicToService(myService.uuid, myCharacteristic)
}