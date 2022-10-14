package com.lorenzofelletti.simplebleapp.ble.gattserver.model

import java.util.UUID

object Constants {
    val UUID_SERVICE: UUID = UUID.fromString("23782c92-139c-4846-aac5-31d1b078d439")

    val UUID_READ_CHARACTERISTIC: UUID =
        UUID.fromString("23782c92-139c-4846-aac5-31d1b078d440")

    val UUID_DESCRIPTOR: UUID =
        UUID.fromString("23782c92-139c-4846-aac5-31d1b078d441")
}