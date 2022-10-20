package com.lorenzofelletti.simplebleapp.ble.gattserver

fun <T> getValueAsByteArray(value: T): ByteArray {
    return value.toString().toByteArray()
}