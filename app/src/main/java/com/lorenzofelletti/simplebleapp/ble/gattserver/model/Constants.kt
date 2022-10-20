package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import java.util.UUID

object Constants {
    val UUID_MY_SERVICE: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")

    val UUID_MY_CHARACTERISTIC: UUID =
        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    val UUID_MY_DESCRIPTOR: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}