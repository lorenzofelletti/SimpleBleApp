package com.lorenzofelletti.simplebleapp.ble.gattserver

object CharacteristicUtilities {
    fun <T> getValueAsByteArray(value: T): ByteArray {
        return value.toString().toByteArray()
    }
}