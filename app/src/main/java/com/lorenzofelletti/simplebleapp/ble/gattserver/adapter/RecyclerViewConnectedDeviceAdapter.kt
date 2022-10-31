package com.lorenzofelletti.simplebleapp.ble.gattserver.adapter

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.model.HasConnectedDevicesMap
import com.lorenzofelletti.simplebleapp.blescriptrunner.adapter.ConnectedDeviceAdapter

abstract class RecyclerViewConnectedDeviceAdapter<T : RecyclerView.ViewHolder?> :
    RecyclerView.Adapter<T>(), ConnectedDeviceAdapterInterface,
    HasConnectedDevicesMap {

    override fun getItemCount(): Int {
        return bluetoothConnectedDevices.size
    }

    override fun addDevice(device: BluetoothDevice) {
        if (DEBUG)
            Log.d(TAG, "${::addDevice.name} - Adding device: ${device.address}")
    }

    override fun removeDevice(device: BluetoothDevice) {
        if (DEBUG)
            Log.d(TAG, "${::removeDevice.name} - Removing device: ${device.address}")
    }

    override fun clearDevices() {
        if (DEBUG) Log.d(TAG, "${::clearDevices.name}()")
    }

    override fun toggleDeviceNotification(device: BluetoothDevice) {
        if (DEBUG)
            Log.d(TAG, "${::toggleDeviceNotification.name} - Toggling device: ${device.address}")
    }

    companion object {
        private val TAG = ConnectedDeviceAdapter::class.java.simpleName
    }
}