/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc

/**
 * Data object containing the device information resulting from a device scanning.
 *
 * [identifier] is the unique identifier of the device used for the device connection.
 *
 * [textInfo] is a text dedicated to the user interface to help in a possible selection. Device name
 * or address for a BLE device, manufacturer and device name for a USB device.
 *
 * [extraInfo] contains technical details. The RSSI (receive signal strength indicator) for a BLE
 * device or the VID/PID (Vendor ID / Product ID) for a USB device.
 *
 * @since 1.0.0
 */
data class DeviceInfo(val identifier: String, val textInfo: String, val extraInfo: String)
