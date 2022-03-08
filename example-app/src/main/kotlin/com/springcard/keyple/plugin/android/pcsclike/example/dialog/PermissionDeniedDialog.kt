/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike.example.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.springcard.keyple.plugin.android.pcsclike.example.R

class PermissionDeniedDialog : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let {
      // Use the Builder class for convenient dialog construction
      val builder = AlertDialog.Builder(it)
      builder.setCancelable(false).setMessage(R.string.permission_denied_message).setPositiveButton(
          android.R.string.cancel) { _, _ ->
        dismiss()
        it.finish()
      }
      // Create the AlertDialog object and return it
      builder.create()
    }
        ?: throw IllegalStateException("Activity cannot be null")
  }
}
