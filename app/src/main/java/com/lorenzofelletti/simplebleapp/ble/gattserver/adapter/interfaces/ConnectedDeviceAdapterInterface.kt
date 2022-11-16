package com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.interfaces

import android.bluetooth.BluetoothDevice

/**
 * This interface is used to handle the UI updates when a device connects, disconnects or
 * subscribes/unsubscribes to a characteristic notification.
 */
interface ConnectedDeviceAdapterInterface {
    val blinkingMap: MutableMap<BluetoothDevice, Int>

    /**
     * Clears the list of connected devices and notifies the adapter.
     */
    fun clearDevices()

    /**
     * Adds a device to the list of connected devices and notifies the adapter.
     *
     * @param device The device to add.
     */
    fun addDevice(device: BluetoothDevice)

    /**
     * Removes a device from the list of connected devices and notifies the adapter.
     *
     * @param device The device to remove.
     */
    fun removeDevice(device: BluetoothDevice)

    /**
     * Toggles the notification for a device.
     *
     * @param device The device to toggle the notification.
     */
    fun toggleDeviceNotification(device: BluetoothDevice)

    /**
     * Makes the device blink in the list of connected devices for some time.
     *
     * @param device The device to blink
     * @param blinkStatus The color to use for the blink
     * @param duration The duration of the blink in milliseconds, negative to blink indefinitely
     */
    fun blinkDevice(device: BluetoothDevice, blinkStatus: Int, duration: Long = 1000) {
        // empty to avoid forcing the implementation of this method
    }
}