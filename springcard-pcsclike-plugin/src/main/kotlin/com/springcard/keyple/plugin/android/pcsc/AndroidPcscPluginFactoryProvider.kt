/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc

import android.content.Context

/**
 * Provider of factory of Android Pcsc plugin.
 *
 * @since 1.0.0
 */
object AndroidPcscPluginFactoryProvider {
  fun getFactory(
      deviceType: AndroidPcscPluginFactory.DeviceType,
      context: Context
  ): AndroidPcscPluginFactory {
    return AndroidPcscPluginFactoryAdapter(deviceType, context)
  }
}
