package com.lorenzofelletti.simplebleapp.ble.gattserver

object CharacteristicUtilities {
    fun <T> getValueAsByteArray(value: T): ByteArray {
        return when (value) {
            is String -> value.toByteArray()
            is Int -> value.toString().toByteArray()
            is Float -> value.toString().toByteArray()
            is Double -> value.toString().toByteArray()
            is Boolean -> value.toString().toByteArray()
            else -> throw IllegalArgumentException("Value type not supported")
        }
    }
}