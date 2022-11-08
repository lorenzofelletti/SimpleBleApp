package com.lorenzofelletti.simplebleapp.blescriptrunner.adapter

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
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.R
import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.AbstractRecyclerViewConnectedDeviceAdapter

/**
 * Adapter for the connected devices list's [RecyclerView].
 *
 * @param activity The activity that contains the [RecyclerView].
 * @param bluetoothConnectedDevices The map of connected devices.
 */
class ConnectedDeviceAdapter(
    private val activity: Activity,
    override var bluetoothConnectedDevices: MutableMap<BluetoothDevice, Boolean>
) : AbstractRecyclerViewConnectedDeviceAdapter<ConnectedDeviceAdapter.ViewHolder>() {
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
        val device = bluetoothConnectedDevices.keys.elementAt(position)

        val addressTextView = holder.deviceAddressTextView
        addressTextView.text = device.address

        val nameTextView = holder.deviceNameTextView
        nameTextView.text = device.name

        val notificationStatusTextView = holder.notificationStatusTextView
        notificationStatusTextView.background = if (bluetoothConnectedDevices[device]!!) {
            AppCompatResources.getDrawable(holder.itemView.context, R.drawable.green_dot)
        } else {
            AppCompatResources.getDrawable(holder.itemView.context, R.drawable.red_dot)
        }
    }

    override fun clearDevices() {
        super.clearDevices()

        val size = bluetoothConnectedDevices.size
        bluetoothConnectedDevices.clear()
        activity.runOnUiThread { notifyItemRangeRemoved(0, size) }
    }

    override fun addDevice(device: BluetoothDevice) {
        super.addDevice(device)

        bluetoothConnectedDevices[device] = false
        activity.runOnUiThread { notifyItemInserted(bluetoothConnectedDevices.size - 1) }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun removeDevice(device: BluetoothDevice) {
        super.removeDevice(device)

        bluetoothConnectedDevices.remove(device)
        activity.runOnUiThread { notifyDataSetChanged() }
    }

    /**
     * Toggles the notification for a device, i.e. if the notification subscription is active (true) it
     * will be deactivated (false) and vice versa.
     *
     * @param device The device to toggle the notification.
     */
    override fun toggleDeviceNotification(device: BluetoothDevice) {
        super.toggleDeviceNotification(device)

        val index = bluetoothConnectedDevices.keys.indexOf(device)
        if (index == -1) {
            if (DEBUG) Log.e(TAG, "${::toggleDeviceNotification.name} - Device not found")
            return
        }

        bluetoothConnectedDevices[device]?.let { notificationState ->
            bluetoothConnectedDevices[device] = !notificationState
            activity.runOnUiThread { notifyItemChanged(index) }
        }
    }

    companion object {
        private val TAG = ConnectedDeviceAdapter::class.java.simpleName
    }
}