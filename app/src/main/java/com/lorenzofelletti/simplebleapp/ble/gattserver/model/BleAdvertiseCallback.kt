package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import com.lorenzofelletti.simplebleapp.BuildConfig.DEBUG

/**
 * Callback for the BLE advertising.
 *
 * @param doOnStartSuccess Callback to be called when the advertising starts successfully.
 * @param doOnStartFailure Callback to be called when the advertising fails to start.
 *
 * @see [AdvertiseCallback](https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback)
 */
class BleAdvertiseCallback(
    private val doOnStartSuccess: () -> Unit = {},
    private val doOnStartFailure: () -> Unit = {},
) : AdvertiseCallback() {
    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        super.onStartSuccess(settingsInEffect)

        if (DEBUG) android.util.Log.d(TAG, "${::onStartSuccess.name} - LE Advertise Started")

        doOnStartSuccess()
    }

    override fun onStartFailure(errorCode: Int) {
        super.onStartFailure(errorCode)

        if (DEBUG) android.util.Log.d(TAG, "${::onStartFailure.name} - LE Advertise Failed: $errorCode")

        doOnStartFailure()
    }

    companion object {
        private val TAG = BleAdvertiseCallback::class.java.simpleName
    }
}