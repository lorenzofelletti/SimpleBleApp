package com.lorenzofelletti.simplebleapp

import android.view.View

private val DEBUG = BuildConfig.DEBUG

/**
 * A function that builds an [android.view.View.OnClickListener] for starting/stopping the BLE server having
 * two different behaviors depending on whether a server is already running or not.
 *
 * @param initialServerState The initial state of the server.
 * @param startServerAction A lambda that starts the server.
 * @param stopServerAction A lambda that stops the server.
 * @return An [android.view.View.OnClickListener] that starts the server if it is not already
 */
fun startServerOnClickListenerBuilder(
    initialServerState: Boolean = false,
    startServerAction: () -> Unit,
    stopServerAction: () -> Unit
): (View) -> Unit {
    var isServerStarted = initialServerState
    return fun(v: View) {
        if (DEBUG) android.util.Log.d(
            ::startServerOnClickListenerBuilder.name,
            "${v.javaClass.simpleName}:${v.id} - ${if (isServerStarted) "Stop" else "Start"} Server Button Clicked"
        )

        isServerStarted = if (isServerStarted) {
            stopServerAction()
            false
        } else {
            startServerAction()
            true
        }
    }
}