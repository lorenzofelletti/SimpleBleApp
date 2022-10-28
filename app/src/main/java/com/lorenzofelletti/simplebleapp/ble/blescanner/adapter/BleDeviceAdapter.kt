package com.lorenzofelletti.simplebleapp.ble.blescanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simplebleapp.R
import com.lorenzofelletti.simplebleapp.ble.blescanner.model.BleDevice

/**
 * Adapter for the RecyclerView that shows the found BLE devices.
 */
class BleDeviceAdapter(private val devices: List<BleDevice>) : RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById<TextView>(R.id.device_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.device_row_layout, parent, false)
        return ViewHolder(deviceView)
    }

    override fun onBindViewHolder(holder: BleDeviceAdapter.ViewHolder, position: Int) {
        val device = devices[position]
        val textView = holder.deviceNameTextView
        textView.text = device.address
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}