package com.lorenzofelletti.simplebleapp

import android.view.View

private const val TAG = "onClickListener"
private val DEBUG = BuildConfig.DEBUG

/**
 * A function that builds an [android.view.View.OnClickListener] for starting the BLE server having
 * two different behaviors depending on whether a server is already running or not.
 *
 * @param initialServerState The initial state of the server.
 * @param startServer A lambda that starts the server.
 * @param stopServer A lambda that stops the server.
 * @return An [android.view.View.OnClickListener] that starts the server if it is not already
 */
fun startServerOnClickListenerBuilder(
    initialServerState: Boolean = false,
    startServer: () -> Unit,
    stopServer: () -> Unit
): (View) -> Unit {
    var isServerStarted = initialServerState
    return fun(v: View) {
        if (DEBUG) android.util.Log.d(
            TAG,
            "${v.javaClass.simpleName}:${v.id} - ${if (isServerStarted) "Stop" else "Start"} Server Button Clicked"
        )
        isServerStarted = if (isServerStarted) {
            stopServer()
            false
        } else {
            startServer()
            true
        }
    }
}