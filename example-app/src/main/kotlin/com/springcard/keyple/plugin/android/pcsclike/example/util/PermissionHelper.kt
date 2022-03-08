/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike.example.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

object PermissionHelper {

  private fun isPermissionGranted(activity: Activity, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(activity, permission) ==
        PackageManager.PERMISSION_GRANTED
  }

  fun checkPermission(context: Activity, permissions: Array<String>, requestCode: Int): Boolean {
    val permissionDenied = permissions.filter { !isPermissionGranted(context, it) }

    if (permissionDenied.isNotEmpty()) {
      val permissionsToAsk = arrayOfNulls<String>(permissionDenied.size)
      for ((position, permission) in permissionDenied.withIndex()) {
        permissionsToAsk[position] = permission
        Timber.i("Permission %s denied", permission)
      }
      ActivityCompat.requestPermissions(context, permissionsToAsk, requestCode)
      return false
    }
    return true
  }
}
