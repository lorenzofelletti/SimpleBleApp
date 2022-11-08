package com.lorenzofelletti.simplebleapp.ble.gattserver.model.interfaces

import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.interfaces.ConnectedDeviceAdapterInterface

/**
 * Interface that defines a class that has a [ConnectedDeviceAdapterInterface] instance.
 */
interface HasConnectedDevicesAdapter {
    /**
     * The [ConnectedDeviceAdapterInterface] instance.
     */
    var adapter: ConnectedDeviceAdapterInterface?
}