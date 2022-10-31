package com.lorenzofelletti.simplebleapp.ble.gattserver.viewmodel

import androidx.lifecycle.ViewModel
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager

/**
 * ViewModel for sharing the GattServerManager between fragments and activities
 */
class GattServerManagerViewModel : ViewModel() {
    var gattServerManager: GattServerManager? = null
}