/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.example.activity

/**
 * Bridges the logic processing and the graphical interface to display various kinds of messages.
 */
internal interface EventNotifierSpi {
  fun onReaderReady()

  fun onHeader(header: String)

  fun onAction(action: String)

  fun onResult(result: String)
}
