package com.lorenzofelletti.simplebleapp.permissions

import android.Manifest

/**
 * An object containing the required permissions for scanning bluetooth LE devices.
 */
object AppRequiredPermissions {

    /** Array of required permissions for BLE scanning. */
    val gattServerPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH,
    )

    /** The rationale message to show when the user denies the permissions. */
    const val gattServerRationaleMessage =
        "The app needs Bluetooth and Location permissions for the GATT server to work properly."

    const val GATT_SERVER_REQUEST_CODE = 2
}