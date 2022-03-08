/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.example.calypso

import org.calypsonet.terminal.calypso.sam.CalypsoSam
import org.eclipse.keyple.core.util.ByteArrayUtil

/**
 * Helper class to provide specific elements to handle Calypso cards.
 *
 * * AIDs for application selection
 * * Files info (SFI, rec number, etc) for
 *
 * * Environment and Holder
 * * Event Log
 * * Contract List
 * * Contracts
 * * Counters
 */
internal object CardInfo {
  /** Calypso default AID */
  const val KEYPLE_KIT_AID = "315449432e49434131"
  const val NAVIGO_B_AID = "A0000004040125090101"
  const val NAVIGO_BPRIME_AID = "315449432E494341"

  const val RECORD_NUMBER_1 = 1
  const val RECORD_NUMBER_2 = 2
  const val RECORD_NUMBER_3 = 3
  const val RECORD_NUMBER_4 = 4
  const val SFI_EnvironmentAndHolder = 0x07.toByte()
  const val SFI_EventLog = 0x08.toByte()
  const val SFI_ContractList = 0x1E.toByte()
  const val SFI_Contracts = 0x09.toByte()
  const val SFI_Counter1 = 0x19.toByte()
  val eventLog_dataFill: ByteArray =
      ByteArrayUtil.fromHex("00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC")

  val SamType = CalypsoSam.ProductType.SAM_C1

  const val RECORD_SIZE = 29
}
