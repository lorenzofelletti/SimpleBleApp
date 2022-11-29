# SimpleBleApp
A simple app that implements a Bluetooth LE peripheral exposing a service with UUID `0000ffe0-0000-1000-8000-00805f9b34fb` and two characteristics.

The first characteristic has permissions for notifications and read, the second one for read and write.
These characteristic are used by an ad-hoc central to enable the execution of scripts on the central commanded by the peripheral.
The user of the app can command the execution of scripts in broadcast to all connected centrals.
