/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.pcsclike

import android.content.Context
import android.hardware.usb.UsbDevice
import com.springcard.pcsclike.ccid.*
import com.springcard.pcsclike.communication.*

class SCardReaderListUsb
internal constructor(layerDevice: UsbDevice, callbacks: SCardReaderListCallback) :
    SCardReaderList(layerDevice as Any, callbacks) {

  override fun create(ctx: Context) {
    if (layerDevice is UsbDevice) {
      commLayer = UsbLayer(this, layerDevice)
      commLayer.connect(ctx)
    }
  }

  override fun create(ctx: Context, secureConnexionParameters: CcidSecureParameters) {
    throw NotImplementedError(
        "Cannot create SCardReaderListUsb with secure parameters for the moment")
  }
}
