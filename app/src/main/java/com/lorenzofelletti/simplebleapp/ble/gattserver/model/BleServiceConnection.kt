package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService

/**
 * Service connection for the BLE advertising.
 * To create an instance of this class, use the [BleServiceConnection.Builder] class.
 */
class BleServiceConnection private constructor(builder: Builder) : ServiceConnection {
    /**
     * True if the service is bound, false otherwise.
     */
    var bound = false
        private set
    private lateinit var advertisingService: PeripheralAdvertiseService
    private val onServiceConnectedActions = mutableListOf({ bound = true })
    private val onServiceDisconnectedActions = mutableListOf({ bound = false })

    init {
        onServiceConnectedActions.addAll(builder.onServiceConnectedActions)
        onServiceDisconnectedActions.addAll(builder.onServiceDisconnectedActions)
    }

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

    /**
     * Builder class for the [BleServiceConnection] class.
     */
    class Builder {
        val onServiceConnectedActions = mutableListOf<() -> Unit>()
        val onServiceDisconnectedActions = mutableListOf<() -> Unit>()

        /**
         * Adds an action to be executed both when the service is connected and when it is disconnected.
         */
        fun addCommonActions(vararg actions: () -> Unit) = apply {
            onServiceConnectedActions.addAll(actions.toList())
            onServiceDisconnectedActions.addAll(actions.toList())
        }

        fun addOnServiceConnectedActions(vararg actions: () -> Unit) = apply {
            onServiceConnectedActions.addAll(actions.toList())
        }

        fun addOnServiceDisconnectedActions(vararg actions: () -> Unit) = apply {
            onServiceDisconnectedActions.addAll(actions.toList())
        }

        /**
         * Builds the [BleServiceConnection] instance.
         */
        fun build() = BleServiceConnection(this)
    }

    companion object {
        private val TAG = BleServiceConnection::class.java.simpleName
    }
}