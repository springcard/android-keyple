/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.example.activity

import com.springcard.keyple.plugin.android.pcsc.AndroidPcscPlugin
import com.springcard.keyple.plugin.android.pcsc.AndroidPcscPluginFactory
import com.springcard.keyple.plugin.android.pcsc.AndroidPcscPluginFactoryProvider
import com.springcard.keyple.plugin.android.pcsc.AndroidPcscReader
import com.springcard.keyple.plugin.android.pcsc.DeviceInfo
import com.springcard.keyple.plugin.android.pcsc.example.calypso.CardManager
import com.springcard.keyple.plugin.android.pcsc.spi.DeviceScannerSpi
import org.calypsonet.terminal.reader.CardReaderEvent
import org.calypsonet.terminal.reader.ObservableCardReader
import org.calypsonet.terminal.reader.spi.CardReaderObserverSpi
import org.eclipse.keyple.card.calypso.CalypsoExtensionService
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory
import org.eclipse.keyple.core.service.ObservablePlugin
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.PluginEvent
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.core.service.spi.PluginObserverSpi
import timber.log.Timber

/** Manages the initialization and observation of plugins and readers. */
internal class ReaderManager(private val activity: MainActivity) :
    DeviceScannerSpi, PluginObserverSpi, CardReaderObserverSpi {

  private lateinit var androidPcscPlugin: ObservablePlugin
  private var cardReader: ObservableReader? = null
  private val cardManager: CardManager = CardManager(activity)
  private val calypsoCardExtensionProvider: CalypsoExtensionService =
      CalypsoExtensionService.getInstance()

  /**
   * Initializes the card reader discovery.
   *
   * Creates an observable plugin for the provided device type and adds an observer on it to handle
   * plugin events triggered upon reader connections.
   * @param deviceType The type of device, USB or BLE.
   * @return True if the operation succeeded.
   */
  fun initReaders(deviceType: AndroidPcscPluginFactory.DeviceType): Boolean {
    val pluginFactory: KeyplePluginExtensionFactory?
    try {
      Timber.d("Creation of a plugin factory for device type %s", deviceType.name)
      pluginFactory = AndroidPcscPluginFactoryProvider.getFactory(deviceType, activity)
    } catch (e: Exception) {
      activity.onResult("Unable to create plugin factory.")
      return false
    }
    val smartCardService = SmartCardServiceProvider.getService()
    // check the card extension, any version inconsistencies will be logged
    smartCardService.checkCardExtension(calypsoCardExtensionProvider)
    // Register the AndroidPcsc plugin with SmartCardService, get the corresponding generic Plugin
    // in
    // return
    androidPcscPlugin = smartCardService.registerPlugin(pluginFactory) as ObservablePlugin
    androidPcscPlugin.getExtension(AndroidPcscPlugin::class.java).scanDevices(2, true, this)
    androidPcscPlugin.setPluginObservationExceptionHandler { pluginName, e ->
      Timber.e(e, "An unexpected plugin error occurred in '%s':", pluginName)
    }
    androidPcscPlugin.addObserver(this)

    activity.onResult("Plugin '${androidPcscPlugin.name}' created and observed")

    return true
  }

  override fun onDeviceDiscovered(deviceInfoList: MutableCollection<DeviceInfo>) {
    for (deviceInfo in deviceInfoList) {
      Timber.i("Discovered devices: %s", deviceInfo)
    }
    activity.onResult("Device discovery is finished.\n${deviceInfoList.size} device(s) discovered.")
    for (deviceInfo in deviceInfoList) {
      activity.onResult("Reader: " + deviceInfo.textInfo)
    }
    // connect to first discovered device (we should ask the user)
    if (deviceInfoList.isNotEmpty()) {
      val device = deviceInfoList.first()
      androidPcscPlugin
          .getExtension(AndroidPcscPlugin::class.java)
          .connectToDevice(device.identifier)
    }
  }

  /** Starts the card discovery */
  fun startCardDetection() {
    cardReader?.startCardDetection(ObservableCardReader.DetectionMode.REPEATING)
  }

  /** Stops the card discovery */
  fun stopCardDetection() {
    cardReader?.stopCardDetection()
  }

  /** Stops started services cleanly */
  fun cleanUp() {
    // stop propagating the reader events
    cardReader?.removeObserver(this)
    // Unregister all plugins
    SmartCardServiceProvider.getService().plugins.forEach {
      SmartCardServiceProvider.getService().unregisterPlugin(it.name)
    }
    // cleanup card manager (stop card resource service)
    cardManager.cleanUp()
  }

  override fun onPluginEvent(pluginEvent: PluginEvent?) {
    if (pluginEvent != null) {
      var logMessage =
          "Plugin Event: plugin=${pluginEvent.pluginName}, event=${pluginEvent.type?.name}"
      for (readerName in pluginEvent.readerNames) {
        logMessage += ", reader=$readerName"
      }
      Timber.d(logMessage)
      activity.onAction("Set up reader(s).")
      var cardReaderAvailable = false
      var samReaderAvailable = false
      if (pluginEvent.type == PluginEvent.Type.READER_CONNECTED) {
        // the card and SAM readers are assigned according to their name.
        // Here the card reader contains 'contactless' in its name and the SAM reader contains
        // 'SAM'.
        for (readerName in pluginEvent.readerNames) {
          if (readerName.toUpperCase().contains("CONTACTLESS")) {
            cardReaderAvailable = true
            onCardReaderConnected(readerName)
          } else if (readerName.toUpperCase().contains("SAM")) {
            samReaderAvailable = true
            onSamReaderConnected(readerName)
          }
        }
        // If a SAM reader is present, the presence of a SAM will be made later.
        if (!samReaderAvailable) {
          // nous n'avons pas trouvé de lecteur SAM.
          activity.onResult("No SAM reader available. Continue without security")
        }
        if (cardReaderAvailable) {
          activity.onHeader("Waiting for a card...")
          // notify the parent activity the availability of the reader(s)
          activity.onReaderReady()
        }
      }
      if (pluginEvent.type == PluginEvent.Type.READER_DISCONNECTED) {
        for (readerName in pluginEvent.readerNames) {
          activity.onAction("Reader '$readerName' dicconnected.")
        }
        activity.onReaderDisconnected()
      }
    }
  }

  /**
   * Invoked when a card reader is connected.
   *
   * Starts the observation of the reader and prepares a card selection scenario.
   * @param readerName The name of the reader.
   */
  private fun onCardReaderConnected(readerName: String) {
    cardReader = androidPcscPlugin.getReader(readerName) as ObservableReader

    if (cardReader != null) {
      // we assume here that the reader is contactless
      cardReader!!.getExtension(AndroidPcscReader::class.java).setContactless(true)

      // we provide a simplified exception handler
      cardReader!!.setReaderObservationExceptionHandler { pluginName, readerName, e ->
        Timber.e("An unexpected reader error occurred: %s:%s", pluginName, readerName)
      }

      // we place ourself as an observer of this reader
      cardReader!!.addObserver(this)

      cardReader!!.startCardDetection(ObservableCardReader.DetectionMode.REPEATING)

      cardManager.initiateScheduledCardSelection(cardReader!!)
    } else {
      Timber.e("Unexpected reader not found error")
    }
  }

  /**
   * Invoked when a SAM reader is connected.
   *
   * Sets up the security settings to manage card secure session with the SAM when available.
   * @param readerName The name of the reader.
   */
  private fun onSamReaderConnected(readerName: String) {
    cardManager.setupSecurityService(androidPcscPlugin, readerName)
  }

  override fun onReaderEvent(readerEvent: CardReaderEvent?) {
    cardManager.onReaderEvent(readerEvent)
  }
}
