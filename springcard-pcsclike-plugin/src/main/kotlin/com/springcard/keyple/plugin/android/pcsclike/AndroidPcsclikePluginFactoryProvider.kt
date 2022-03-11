/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike

import android.content.Context

/**
 * Provider of factory of Android Pcsc plugin.
 *
 * @since 1.0.0
 */
object AndroidPcsclikePluginFactoryProvider {
  /**
   * Returns the plugin factory for the provided device type and context.
   * @since 1.0.0
   */
  fun getFactory(
      deviceType: AndroidPcsclikePluginFactory.DeviceType,
      context: Context
  ): AndroidPcsclikePluginFactory {
    return AndroidPcsclikePluginFactoryAdapter(deviceType, context)
  }
}
