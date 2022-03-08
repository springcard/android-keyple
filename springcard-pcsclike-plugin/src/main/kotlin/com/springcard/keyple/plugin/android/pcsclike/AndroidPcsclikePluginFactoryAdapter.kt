/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike

import android.content.Context
import org.eclipse.keyple.core.common.CommonApiProperties
import org.eclipse.keyple.core.plugin.PluginApiProperties
import org.eclipse.keyple.core.plugin.spi.PluginFactorySpi
import org.eclipse.keyple.core.plugin.spi.PluginSpi

/**
 * Implementation of the AndroidPcsclikePluginFactory
 * @since 1.0.0
 */
internal class AndroidPcsclikePluginFactoryAdapter
internal constructor(
    private val deviceType: AndroidPcsclikePluginFactory.DeviceType,
    val context: Context
) : AndroidPcsclikePluginFactory, PluginFactorySpi {
  private val PLUGIN_NAME = "AndroidPcsclikePlugin"
  private val name = "${PLUGIN_NAME}_${deviceType.name}"

  override fun getPluginName(): String {
    return name
  }

  /** Instantiates the plugin according to the specified link. */
  override fun getPlugin(): PluginSpi =
      when (deviceType) {
        AndroidPcsclikePluginFactory.DeviceType.BLE ->
            AndroidBlePcsclikePluginAdapter(name, context)
        AndroidPcsclikePluginFactory.DeviceType.USB ->
            AndroidUsbPcsclikePluginAdapter(name, context)
      }

  override fun getCommonApiVersion(): String = CommonApiProperties.VERSION

  override fun getPluginApiVersion(): String = PluginApiProperties.VERSION
}
