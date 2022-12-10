package com.lorenzofelletti.simplebleapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.lorenzofelletti.simplebleapp.ble.gattserver.GattServerManager
import com.lorenzofelletti.simplebleapp.blescriptrunner.Constants

/**
 * Creates the service and the characteristic to be added to the GATT server and
 * adds them to the GATT server.
 */
@SuppressLint("MissingPermission")
fun setBluetoothService(gattServerManager: GattServerManager) {
    val scriptRunnerService = BluetoothGattService(
        Constants.UUID_SCRIPT_RUNNER_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY
    )
    val scriptRunnerCharacteristic = BluetoothGattCharacteristic(
        Constants.UUID_SCRIPT_RUNNER_CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        BluetoothGattCharacteristic.PERMISSION_READ
    )

    val scriptRunnerCharDescriptor = BluetoothGattDescriptor(
        Constants.UUID_SCRIPT_RUNNER_DESCRIPTOR,
        BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
    )
    scriptRunnerCharacteristic.descriptors?.add(scriptRunnerCharDescriptor)


    scriptRunnerService.addCharacteristic(scriptRunnerCharacteristic)

    // Add script result characteristic
    val scriptResultsCharacteristic = BluetoothGattCharacteristic(
        Constants.UUID_SCRIPT_RESULTS_CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
        BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val scriptResultsCharDescriptor = BluetoothGattDescriptor(
        Constants.UUID_SCRIPT_RESULTS_DESCRIPTOR,
        BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
    )
    scriptResultsCharacteristic.descriptors?.add(scriptResultsCharDescriptor)

    scriptRunnerService.addCharacteristic(scriptResultsCharacteristic)

    gattServerManager.addService(scriptRunnerService)

    gattServerManager.addCharacteristicToService(
        scriptRunnerService.uuid,
        scriptResultsCharacteristic
    )
    gattServerManager.addCharacteristicToService(
        scriptRunnerService.uuid,
        scriptRunnerCharacteristic
    )
}

fun MainActivity.showEnableBluetoothDialog(
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: () -> Unit
) {
    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
    builder.setTitle(R.string.enable_bluetooth_dialog_title)
    builder.setMessage(R.string.enable_bluetooth_dialog_message)
    builder.setPositiveButton(R.string.enable_bluetooth_dialog_positive_button) { _, _ ->
        onPositiveButtonClicked()
    }
    builder.setNegativeButton(R.string.enable_bluetooth_dialog_negative_button) { _, _ ->
        onNegativeButtonClicked()
    }
    builder.show()
}