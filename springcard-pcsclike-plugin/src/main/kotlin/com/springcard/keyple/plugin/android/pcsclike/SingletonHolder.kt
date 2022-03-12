/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike

/**
 * Singleton manager
 * @since 1.0.0
 */
internal open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
  private var creator: ((A) -> T)? = creator
  @Volatile private var instance: T? = null

  fun getInstance(arg: A): T {
    val checkInstance = instance
    if (checkInstance != null) {
      return checkInstance
    }

    return synchronized(this) {
      val checkInstanceAgain = instance
      if (checkInstanceAgain != null) {
        checkInstanceAgain
      } else {
        val created = creator!!(arg)
        instance = created
        creator = null
        created
      }
    }
  }
}
