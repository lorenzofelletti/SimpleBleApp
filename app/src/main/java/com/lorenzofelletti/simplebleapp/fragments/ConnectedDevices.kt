package com.lorenzofelletti.simplebleapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simplebleapp.R
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager
import com.lorenzofelletti.simplebleapp.ble.gattserver.viewmodel.GattServerManagerViewModel
import com.lorenzofelletti.simplebleapp.blescriptrunner.adapter.ConnectedDeviceAdapter

/**
 * A simple [Fragment] to manage the connected devices UI.
 *
 * Use the [ConnectedDevices.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConnectedDevices : Fragment() {
    private val gattServerManagerViewModel: GattServerManagerViewModel by activityViewModels()
    private lateinit var gattServerManager: GattServerManager
    private lateinit var rvConnectedDevices: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connected_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            gattServerManager = gattServerManagerViewModel.gattServerManager!!

            val devices = gattServerManager.bluetoothConnectedDevices
            gattServerManager.adapter = this.activity?.let { ConnectedDeviceAdapter(it, devices) }

        } catch (e: UninitializedPropertyAccessException) {
            throw UninitializedPropertyAccessException("GattServerManager not initialized")
        }

        rvConnectedDevices = view.findViewById(R.id.rv_connected_devices)
        rvConnectedDevices.adapter = gattServerManager.adapter as RecyclerView.Adapter<*>?
        rvConnectedDevices.layoutManager = LinearLayoutManager(activity)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ConnectedDevices.
         */
        @JvmStatic
        fun newInstance() = ConnectedDevices()
    }
}