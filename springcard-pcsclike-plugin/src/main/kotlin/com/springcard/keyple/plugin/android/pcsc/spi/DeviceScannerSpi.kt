/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.spi

import com.springcard.keyple.plugin.android.pcsc.DeviceInfo

/**
 * Device observer recipient of the device discovery.
 *
 * @since 1.0.0
 */
interface DeviceScannerSpi {
  fun onDeviceDiscovered(deviceInfoList: MutableCollection<DeviceInfo>)
}
