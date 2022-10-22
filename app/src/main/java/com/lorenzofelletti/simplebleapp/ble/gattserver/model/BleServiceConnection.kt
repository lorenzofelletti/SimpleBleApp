package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.lorenzofelletti.simplebleapp.BuildConfig
import com.lorenzofelletti.simplebleapp.ble.gattserver.PeripheralAdvertiseService

class BleServiceConnection private constructor(builder: Builder) : ServiceConnection {
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

    class Builder {
        val onServiceConnectedActions = mutableListOf<() -> Unit>()
        val onServiceDisconnectedActions = mutableListOf<() -> Unit>()

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

        fun build() = BleServiceConnection(this)
    }

    companion object {
        private val TAG = BleServiceConnection::class.java.simpleName
        private val DEBUG = BuildConfig.DEBUG
    }
}