/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.pcsclike.communication

import android.content.Context

interface LowLevelLayer {

  fun connect(ctx: Context)

  fun disconnect()

  fun close()

  fun write(data: List<Byte>)
}
