package com.lorenzofelletti.simplebleapp.ble.gattserver.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simplebleapp.BuildConfig
import com.lorenzofelletti.simplebleapp.R

class ConnectedDeviceAdapter(
    private val devices: MutableMap<BluetoothDevice, Boolean>,
    private val activity: Activity
    ) : RecyclerView.Adapter<ConnectedDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.device_address)
        val deviceNameTextView: TextView = itemView.findViewById(R.id.device_name)
        val notificationStatusTextView: TextView = itemView.findViewById(R.id.notification_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.device_row_layout, parent, false)
        return ViewHolder(deviceView)
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices.keys.elementAt(position)

        val addressTextView = holder.deviceAddressTextView
        addressTextView.text = device.address

        val nameTextView = holder.deviceNameTextView
        nameTextView.text = device.name

        val notificationStatusTextView = holder.notificationStatusTextView
        notificationStatusTextView.background = if (devices[device]!!) {
            AppCompatResources.getDrawable(holder.itemView.context, R.drawable.green_dot)
        } else {
            AppCompatResources.getDrawable(holder.itemView.context, R.drawable.red_dot)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun clearDevices() {
        if (DEBUG) Log.d(TAG, "${::clearDevices.name}()")

        val size = devices.size
        devices.clear()
        activity.runOnUiThread { notifyItemRangeRemoved(0, size) }
    }

    fun addDevice(device: BluetoothDevice) {
        if (DEBUG)
            Log.d(TAG, "${::addDevice.name} - Adding device: ${device.address}")

        devices[device] = false
        activity.runOnUiThread { notifyItemInserted(devices.size - 1) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeDevice(device: BluetoothDevice) {
        if (DEBUG)
            Log.d(TAG, "${::removeDevice.name} - Removing device: ${device.address}")

        devices.remove(device)
        activity.runOnUiThread { notifyDataSetChanged() }
    }

    fun toggleDeviceNotification(device: BluetoothDevice) {
        val index = devices.keys.indexOf(device)
        if (index == -1) {
            if (DEBUG) Log.e(TAG, "${::toggleDeviceNotification.name} - Device not found")
            return
        }

        if (DEBUG) Log.d(
            TAG,
            "${::toggleDeviceNotification.name} - Toggling device notification for device: ${device.address}"
        )

        devices[device] = !devices[device]!!
        activity.runOnUiThread { notifyItemChanged(index) }
    }

    companion object {
        private val TAG = ConnectedDeviceAdapter::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}