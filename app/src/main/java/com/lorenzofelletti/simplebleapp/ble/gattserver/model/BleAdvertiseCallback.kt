package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import com.lorenzofelletti.simplebleapp.BuildConfig

class BleAdvertiseCallback(
    private val doOnStartSuccess: () -> Unit = {},
    private val doOnStartFailure: () -> Unit = {},
) : android.bluetooth.le.AdvertiseCallback() {
    override fun onStartSuccess(settingsInEffect: android.bluetooth.le.AdvertiseSettings?) {
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
        private val DEBUG = BuildConfig.DEBUG
    }
}