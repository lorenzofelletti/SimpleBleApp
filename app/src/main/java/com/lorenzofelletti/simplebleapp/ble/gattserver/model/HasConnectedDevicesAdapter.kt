package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import com.lorenzofelletti.simplebleapp.ble.gattserver.adapter.ConnectedDeviceAdapterInterface

interface HasConnectedDevicesAdapter {
    var adapter: ConnectedDeviceAdapterInterface?
}