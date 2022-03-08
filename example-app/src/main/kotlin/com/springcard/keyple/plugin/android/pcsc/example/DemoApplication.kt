/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.example

import android.app.Application
import androidx.multidex.MultiDex
import timber.log.Timber

class DemoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    MultiDex.install(this)
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}
