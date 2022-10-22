package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.lorenzofelletti.simplebleapp.BuildConfig
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService

class BleServiceConnection : ServiceConnection {
    var bound = false
        private set
    private lateinit var advertisingService: PeripheralAdvertiseService
    private val onServiceConnectedActions = mutableListOf({bound = true})
    private val onServiceDisconnectedActions = mutableListOf({bound = false})

    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        if (DEBUG) Log.d(TAG, "${::onServiceConnected.name} - Service connected")

        val binder = service as PeripheralAdvertiseService.PeripheralAdvertiseBinder
        advertisingService = binder.getService()
        advertisingService.onServiceStopActions.add { executeOnServiceDisconnectedActions() }

        executeOnServiceConnectedActions()
    }

    override fun onServiceDisconnected(className: ComponentName?) {
        if (DEBUG) Log.d(TAG, "${::onServiceDisconnected.name} - Service disconnected")
        executeOnServiceConnectedActions()
    }

    private fun executeOnServiceConnectedActions() {
        onServiceConnectedActions.forEach { it() }
    }

    private fun executeOnServiceDisconnectedActions() {
        onServiceDisconnectedActions.forEach { it() }
    }

    companion object {
        private val TAG = BleServiceConnection::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG

        fun create(
            onServiceConnectedActions: List<() -> Unit>,
            onServiceDisconnectedActions: List<() -> Unit>,
        ): BleServiceConnection {
            val connection = BleServiceConnection()
            connection.onServiceConnectedActions.addAll(onServiceConnectedActions)
            connection.onServiceDisconnectedActions.addAll(onServiceDisconnectedActions)
            return connection
        }
    }
}