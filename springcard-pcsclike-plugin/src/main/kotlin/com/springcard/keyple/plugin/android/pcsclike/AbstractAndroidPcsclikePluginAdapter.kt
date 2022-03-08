/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike

import android.content.Context
import android.os.ConditionVariable
import com.springcard.keyple.plugin.android.pcsclike.spi.DeviceScannerSpi
import com.springcard.pcsclike.SCardChannel
import com.springcard.pcsclike.SCardError
import com.springcard.pcsclike.SCardReader
import com.springcard.pcsclike.SCardReaderList
import com.springcard.pcsclike.SCardReaderListCallback
import org.eclipse.keyple.core.plugin.ReaderIOException
import org.eclipse.keyple.core.plugin.spi.ObservablePluginSpi
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi
import timber.log.Timber

/**
 * Class providing the common features of both USB and BLE links
 * @since 1.0.0
 */
internal abstract class AbstractAndroidPcsclikePluginAdapter(
    private var name: String,
    var context: Context
) : AndroidPcsclikePlugin, ObservablePluginSpi {

  private val WAIT_RESPONSE_TIMEOUT: Long = 5000
  private val MONITORING_CYCLE_DURATION_MS = 1000

  protected var readerList: SCardReaderList? = null
  private val sCardReaders: MutableMap<String, SCardReader> = mutableMapOf()
  private val readerSpis: MutableMap<String, AndroidPcsclikeReaderAdapter> = mutableMapOf()
  private val waitControlResponse = ConditionVariable()
  private lateinit var controlResponse: ByteArray

  abstract override fun scanDevices(
      timeout: Long,
      stopOnFirstDeviceDiscovered: Boolean,
      deviceScannerSpi: DeviceScannerSpi
  )

  abstract override fun connectToDevice(identifier: String)

  override fun getMonitoringCycleDuration(): Int {
    return MONITORING_CYCLE_DURATION_MS
  }

  override fun getName(): String {
    return name
  }

  override fun searchAvailableReaders(): MutableSet<ReaderSpi> {
    for (sCardReader in sCardReaders.values) {
      readerSpis.put(sCardReader.name, AndroidPcsclikeReaderAdapter(sCardReader))
    }
    return readerSpis.values.toMutableSet()
  }

  override fun searchAvailableReaderNames(): MutableSet<String> {
    return sCardReaders.keys
  }

  override fun searchReader(readerName: String): ReaderSpi? {
    for ((name, sCardReader) in sCardReaders) {
      if (readerName == name) {
        val reader = AndroidPcsclikeReaderAdapter(sCardReader)
        readerSpis[sCardReader.name] = reader
        return reader
      }
    }
    return null
  }

  override fun onUnregister() {
    SCardReaderList.clearCache()
    Timber.i("Plugin unregistered.")
  }

  override fun transmitControl(dataIn: ByteArray?): ByteArray {
    if (dataIn == null) {
      throw IllegalStateException("transmitControl data cannot be null")
    }
    if (sCardReaders.isNotEmpty()) {
      // use the reader list to transmit controls
      readerList?.control(dataIn)
      waitControlResponse.block(WAIT_RESPONSE_TIMEOUT)
      waitControlResponse.close()
      return controlResponse
    } else {
      throw ReaderIOException(this.getName() + ": sCardReaders is empty.")
    }
  }

  /** Invoked when a response to control is received */
  private fun onCardControlResponseReceived(controlResponse: ByteArray) {
    Timber.d("Reader '%s', %d bytes received from the reader", name, controlResponse.size)
    this.controlResponse = controlResponse
    waitControlResponse.open()
  }

  /** Implementation of the callback methods defined by @SCardReaderList */
  var scardCallbacks: SCardReaderListCallback =
      object : SCardReaderListCallback() {
        override fun onReaderListCreated(sCardReaderList: SCardReaderList) {
          for (i in 0 until sCardReaderList.slotCount) {
            Timber.v("Add reader: %s", sCardReaderList.slots[i])
            sCardReaderList.getReader(i)?.let { sCardReaders.put(it.name, it) }
          }
          readerList = sCardReaderList
        }

        override fun onReaderListClosed(readerList: SCardReaderList?) {
          sCardReaders.clear()
        }

        override fun onControlResponse(readerList: SCardReaderList, response: ByteArray) {
          onCardControlResponseReceived(response)
        }

        override fun onReaderStatus(
            slot: SCardReader,
            cardPresent: Boolean,
            cardConnected: Boolean
        ) {
          Timber.v(
              "onReaderStatus: reader=%s, cardPresent=%s, cardConnected=%s",
              slot.name,
              cardPresent,
              cardConnected)
          readerSpis[slot.name]?.onCardPresenceChange(cardPresent)
        }

        override fun onCardConnected(channel: SCardChannel) {
          Timber.v("onCardConnected: reader=%s", channel.parent.name)
          readerSpis[channel.parent.name]?.onCardConnected()
        }

        override fun onCardDisconnected(channel: SCardChannel) {
          // disconnection already notified by @onReaderStatus
        }

        override fun onTransmitResponse(channel: SCardChannel, response: ByteArray) {
          readerSpis[channel.parent.name]?.onCardResponseReceived(response)
        }

        override fun onReaderListError(readerList: SCardReaderList?, error: SCardError) {
          Timber.v("onReaderListError: (%d) %s", error.code.value, error.code.name)
        }

        override fun onReaderOrCardError(readerOrCard: Any, error: SCardError) {
          Timber.v("onReaderOrCardError (%d) %s", error.code.value, error.code.name)
          readerSpis[(readerOrCard as SCardReader).name]?.onReaderOrCardError()
        }

        override fun onReaderListState(readerList: SCardReaderList, isInLowPowerMode: Boolean) {
          Timber.v("onReaderListState: isInLowPowerMode %b", isInLowPowerMode)
        }
      }
}
