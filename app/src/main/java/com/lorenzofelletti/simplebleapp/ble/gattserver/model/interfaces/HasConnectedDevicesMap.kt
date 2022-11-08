package com.lorenzofelletti.simplebleapp.ble.gattserver.model.interfaces

import android.bluetooth.BluetoothDevice

/**
 * Interface that defines that a class has a map of connected devices.
 */
interface HasConnectedDevicesMap {
    /**
     * Map of connected devices.
     * The key is the [BluetoothDevice], the value is a [Boolean] that indicates if the device is subscribed to
     * notifications.
     */
    var bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean>
}