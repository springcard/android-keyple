/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc

import android.content.Context
import org.eclipse.keyple.core.common.CommonApiProperties
import org.eclipse.keyple.core.plugin.PluginApiProperties
import org.eclipse.keyple.core.plugin.spi.PluginFactorySpi
import org.eclipse.keyple.core.plugin.spi.PluginSpi

/**
 * Implementation of the AndroidPcscPluginFactory
 * @since 1.0.0
 */
internal class AndroidPcscPluginFactoryAdapter
internal constructor(
    private val deviceType: AndroidPcscPluginFactory.DeviceType,
    val context: Context
) : AndroidPcscPluginFactory, PluginFactorySpi {
  private val PLUGIN_NAME = "AndroidPcscPlugin"
  private val name = "${PLUGIN_NAME}_${deviceType.name}"

  override fun getPluginName(): String {
    return name
  }

  /** Instantiates the plugin according to the specified link. */
  override fun getPlugin(): PluginSpi =
      when (deviceType) {
        AndroidPcscPluginFactory.DeviceType.BLE -> AndroidBlePcscPluginAdapter(name, context)
        AndroidPcscPluginFactory.DeviceType.USB -> AndroidUsbPcscPluginAdapter(name, context)
      }

  override fun getCommonApiVersion(): String = CommonApiProperties.VERSION

  override fun getPluginApiVersion(): String = PluginApiProperties.VERSION
}
