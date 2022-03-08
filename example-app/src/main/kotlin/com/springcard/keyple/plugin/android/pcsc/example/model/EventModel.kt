/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.example.model

open class EventModel(val type: Int, val text: String) {
  companion object {
    const val TYPE_HEADER = 0
    const val TYPE_ACTION = 1
    const val TYPE_RESULT = 2
  }
}
