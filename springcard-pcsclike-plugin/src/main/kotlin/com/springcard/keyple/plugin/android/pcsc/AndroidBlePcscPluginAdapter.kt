/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import com.springcard.keyple.plugin.android.pcsc.spi.DeviceScannerSpi
import com.springcard.pcsclike.SCardReaderList
import com.springcard.pcsclike.communication.GattAttributesD600
import com.springcard.pcsclike.communication.GattAttributesSpringCore
import org.eclipse.keyple.core.plugin.ReaderIOException
import timber.log.Timber

/**
 * Provides the specific means to manage BLE devices.
 * @since 1.0.0
 */
internal class AndroidBlePcscPluginAdapter(name: String, context: Context) :
    AbstractAndroidPcscPluginAdapter(name, context) {
  private val bluetoothDeviceList: MutableMap<String, BluetoothDevice> = mutableMapOf()
  private val bluetoothDeviceInfoMap: MutableMap<String, DeviceInfo> = mutableMapOf()
  private lateinit var bluetoothScanner: BluetoothLeScanner
  private lateinit var deviceScannerSpi: DeviceScannerSpi
  private var scanThread: Thread? = null
  private val handler: Handler = Handler(Looper.getMainLooper())
  private var stopOnFirstDeviceDiscovered: Boolean = false
  private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    bluetoothManager.adapter
  }

  /** Specific scanning for BLE devices. */
  override fun scanDevices(
      timeout: Long,
      stopOnFirstDeviceDiscovered: Boolean,
      deviceScannerSpi: DeviceScannerSpi
  ) {
    this.stopOnFirstDeviceDiscovered = stopOnFirstDeviceDiscovered
    this.deviceScannerSpi = deviceScannerSpi
    bluetoothScanner = this.bluetoothAdapter!!.bluetoothLeScanner
    scanBleDevices(timeout * 1000)
  }

  /**
   * Specific BLE device connection
   * @param identifier The BLE device identifier
   */
  override fun connectToDevice(identifier: String) {
    Timber.d("Connection to BLE device: %s", identifier)
    val bluetoothDevice = bluetoothDeviceList[identifier]
    if (bluetoothDevice != null) {
      SCardReaderList.create(context, bluetoothDevice, scardCallbacks)
    } else {
      throw IllegalStateException("Device with address $identifier not found")
    }
  }

  /**
   * Initiates the BLE scanning with the dedicated Android API.
   *
   * Provides filters to identify the expected BLE services UUID.
   */
  private fun scan() {
    /* Scan settings */
    val settings = ScanSettings.Builder()
    settings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
    val settingsBuilt = settings.build()

    /* Filter for SpringCard service */
    val scanFilters = ArrayList<ScanFilter>()
    try {
      val scanFilterD600 =
          ScanFilter.Builder()
              .setServiceUuid(
                  ParcelUuid(GattAttributesD600.UUID_SPRINGCARD_RFID_SCAN_PCSC_LIKE_SERVICE))
              .build()
      val scanFilterSpringCorePlain =
          ScanFilter.Builder()
              .setServiceUuid(
                  ParcelUuid(GattAttributesSpringCore.UUID_SPRINGCARD_CCID_PLAIN_SERVICE))
              .build()
      val scanFilterSpringCoreBonded =
          ScanFilter.Builder()
              .setServiceUuid(
                  ParcelUuid(GattAttributesSpringCore.UUID_SPRINGCARD_CCID_BONDED_SERVICE))
              .build()
      scanFilters.add(scanFilterD600)
      scanFilters.add(scanFilterSpringCorePlain)
      scanFilters.add(scanFilterSpringCoreBonded)
    } catch (e: Exception) {
      Timber.e(e)
    }
    try {
      bluetoothScanner.startScan(scanFilters, settingsBuilt, leScanCallback)
    } catch (e: SecurityException) {
      Timber.e(e, "Unexpected permission exception.")
    }
    Timber.d("BLE scanning started...")
  }

  /** BluetoothScanner callback */
  private val leScanCallback =
      object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
          var deviceName: String
          try {
            deviceName = result.device.name
          } catch (e: SecurityException) {
            deviceName = ""
            Timber.e(e, "Unexpected permission exception")
          }
          val bleDeviceInfo: DeviceInfo =
              if (result.scanRecord!!.deviceName != null) {
                DeviceInfo(
                    result.device.address, "${result.scanRecord!!.deviceName!!}", "${result.rssi}")
              } else {
                DeviceInfo(
                    result.device.address,
                    "${result.device.address} ${result.rssi}",
                    "${result.rssi}")
              }
          if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
            if (!bluetoothDeviceInfoMap.containsKey(bleDeviceInfo.identifier)) {
              Timber.d("BLE device added: %s", bleDeviceInfo.toString())
              bluetoothDeviceInfoMap[bleDeviceInfo.identifier] = bleDeviceInfo
              bluetoothDeviceList[bleDeviceInfo.identifier] = result.device
              if (stopOnFirstDeviceDiscovered) {
                // shorten timer
                handler.removeCallbacks(notifyScanResults)
                handler.postDelayed(notifyScanResults, 0)
              }
            }
          } else if (callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST &&
              bluetoothDeviceInfoMap.containsKey(result.device.address)) {
            Timber.d("BLE device removed: %s", result.device.address)
            bluetoothDeviceList.remove(result.device.address)
            bluetoothDeviceInfoMap.remove(result.device.address)
          }
        }
      }

  /** Launches the BLE scanning in a separated thread and stops it after the provided delay */
  private fun scanBleDevices(scanDelay: Long) {
    try {

      if (scanThread != null) {
        Timber.d("BLE scan Thread already running.")
        return
      }
      Timber.d("Scan BLE devices for %d ms", scanDelay)
      scanThread = Thread(scanRunnable)
      scanThread!!.start()

      handler.postDelayed(notifyScanResults, scanDelay) // Delay Period
    } catch (e: Exception) {
      Timber.e(e)
      throw ReaderIOException("BLE scanning failure.", e)
    }
  }

  /** Body of the thread dedicated to the scan */
  private val scanRunnable = Runnable { scan() }

  /** Device discovery notifier */
  private val notifyScanResults =
      Runnable {
        Timber.d(
            "Notifying scan results (${bluetoothDeviceInfoMap.size} device(s) found), stop BLE scanning.")
        try {
          bluetoothScanner.stopScan(object : ScanCallback() {})
        } catch (e: SecurityException) {
          Timber.e(e, "Unexpected permission exception.")
        }
        deviceScannerSpi.onDeviceDiscovered(bluetoothDeviceInfoMap.values)
      }
}
