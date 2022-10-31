package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.bluetooth.BluetoothDevice

interface HasConnectedDevicesMap {
    var bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean>
}