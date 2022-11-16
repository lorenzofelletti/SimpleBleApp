package com.lorenzofelletti.simplebleapp.blescriptrunner

import java.util.UUID

object Constants {
    val UUID_SCRIPT_RUNNER_SERVICE: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")

    val UUID_SCRIPT_RUNNER_CHARACTERISTIC: UUID =
        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    val UUID_SCRIPT_RUNNER_DESCRIPTOR: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val UUID_SCRIPT_RESULTS_CHARACTERISTIC: UUID =
        UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")

    val UUID_SCRIPT_RESULTS_DESCRIPTOR: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val BLINKING_DURATION= 1500L

    const val EXIT_CODE_SUCCESS = "0"
    const val EXIT_CODE_FAILURE = "1"
}

enum class ExecutionStatus(val value: Int) {
    UNAVAILABLE(-1),
    FINISHED_SUCCESS(0),
    FINISHED_ERROR(1),
    RUNNING(2)
}