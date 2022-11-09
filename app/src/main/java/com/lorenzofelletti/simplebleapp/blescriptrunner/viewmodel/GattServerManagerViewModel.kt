package com.lorenzofelletti.simplebleapp.blescriptrunner.viewmodel

import androidx.lifecycle.ViewModel
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager

/**
 * ViewModel for sharing the GattServerManager between fragments and activities
 */
class GattServerManagerViewModel : ViewModel() {
    /**
     * The GattServerManager instance, or null if it hasn't been initialized yet
     */
    var gattServerManager: GattServerManager? = null
}