/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike

import com.springcard.keyple.plugin.android.pcsclike.spi.DeviceScannerSpi
import org.eclipse.keyple.core.common.KeyplePluginExtension
import org.eclipse.keyple.core.plugin.ReaderIOException

/**
 * The Android Pcsc plugin interface defines the methods of the Keyple Plugin Extension.
 *
 * @since 1.0.0
 */
interface AndroidPcsclikePlugin : KeyplePluginExtension {

  /**
   * Starts the device scanning. The scan stops either at the first compatible device found or after
   * the time specified by timeout. In all cases, the results are returned asynchronously to the
   * caller who must provide an object implementing [DeviceScannerSpi].
   * @param timeout The maximum scan time in milliseconds.
   * @param stopOnFirstDeviceDiscovered True to stop the scan as soon as a compatible device is
   * found.
   * @param deviceScannerSpi An object implementing the callback method.
   * @throws ReaderIOException If an error occurred during the device scanning.
   * @since 1.0.0
   */
  fun scanDevices(
      timeout: Long,
      stopOnFirstDeviceDiscovered: Boolean,
      deviceScannerSpi: DeviceScannerSpi
  )

  /**
   * Connects to the device whose identifier is provided as a parameter.
   *
   * The identifier is found in the data provided in response to [scanDevices] via the
   * [DeviceScannerSpi].
   * @param identifier The identifier of the device to connect to.
   * @throws IllegalStateException If the provided identifier does not exist.
   * @since 1.0.0
   */
  fun connectToDevice(identifier: String)

  /**
   * Transmits a control command to the device.
   *
   * This communication is not attached to a particular slot but to the whole device which can have
   * several slots.
   * @param dataIn The input data of the command.
   * @return The output data of the command.
   * @throws IllegalStateException If dataIn is null.
   * @throws ReaderIOException If no reader is available.
   * @since 1.0.0
   */
  fun transmitControl(dataIn: ByteArray?): ByteArray
}
