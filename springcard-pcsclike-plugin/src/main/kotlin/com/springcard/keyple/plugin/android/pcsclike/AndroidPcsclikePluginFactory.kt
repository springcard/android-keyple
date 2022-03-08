/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike

import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory

/**
 * Factory of Android Pcsc plugin used to register the plugin with the Keyple SmartCard Service.
 *
 * @since 1.0.0
 */
interface AndroidPcsclikePluginFactory : KeyplePluginExtensionFactory {
  /**
   * The type of device that the plugin will have to manage
   * @since 1.0.0
   */
  enum class DeviceType {
    USB,
    BLE
  }
}
