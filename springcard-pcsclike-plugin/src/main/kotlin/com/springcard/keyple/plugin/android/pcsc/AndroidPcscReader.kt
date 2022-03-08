/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc

import org.eclipse.keyple.core.common.KeypleReaderExtension

/**
 * The Android Pcsc reader interface defines the methods of the Keyple Reader Extension.
 * @since 1.0.0
 */
interface AndroidPcscReader : KeypleReaderExtension {

  /**
   * Defines the type of reader that may be useful to a Keyple Card Extension.
   * @param contactless True if the reader is contactless type.
   * @since 1.0.0
   */
  fun setContactless(contactless: Boolean): AndroidPcscReader
}
