/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.pcsclike.communication

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi
import com.springcard.pcsclike.SCardReaderList

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class BleLayer(scardReaderList: SCardReaderList, bluetoothDevice: BluetoothDevice) :
    CommunicationLayer(scardReaderList) {

  private val TAG = this::class.java.simpleName

  override var lowLayer = BleLowLevel(scardReaderList, bluetoothDevice) as LowLevelLayer

  override fun wakeUp() {
    scardReaderList.enterExclusive()
    scardReaderList.machineState.setNewState(State.WakingUp)
    /* Subscribe to Service changed to wake-up device */
    (lowLayer as BleLowLevel).enableNotifOnCcidStatus()
  }
}
