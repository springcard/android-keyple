/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsclike.example.calypso

import com.springcard.keyple.plugin.android.pcsclike.example.activity.MainActivity
import org.calypsonet.terminal.calypso.WriteAccessLevel
import org.calypsonet.terminal.calypso.card.CalypsoCard
import org.calypsonet.terminal.calypso.sam.CalypsoSam
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting
import org.calypsonet.terminal.reader.CardReader
import org.calypsonet.terminal.reader.CardReaderEvent
import org.calypsonet.terminal.reader.ObservableCardReader
import org.calypsonet.terminal.reader.selection.CardSelectionManager
import org.eclipse.keyple.card.calypso.CalypsoExtensionService
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.Plugin
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.core.service.resource.CardResourceProfileConfigurator
import org.eclipse.keyple.core.service.resource.CardResourceService
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider
import org.eclipse.keyple.core.service.resource.PluginsConfigurator
import org.eclipse.keyple.core.util.ByteArrayUtil
import timber.log.Timber

/**
 * Manages card selection and demo transactions (with and without security depending on the presence
 * of a SAM).
 */
internal class CardManager(private val activity: MainActivity) {
  private var cardResourceService: CardResourceService? = null
  private var cardSecuritySetting: CardSecuritySetting? = null
  private lateinit var cardSelectionManager: CardSelectionManager
  private lateinit var cardReader: ObservableReader
  private var timestamp: Long = 0

  /**
   * Schedules a card selection targeting three applications:
   * - Keyple demo kit card,
   * - Navigo B prime card,
   * - Navigo B card.
   *
   * In all three cases, the environment file is read as soon as the application is selected.
   *
   * Unknown cards are notified (NotificationMode.ALWAYS) with the CARD_INSERTED event.
   * @param cardReader The card reader.
   */
  fun initiateScheduledCardSelection(cardReader: CardReader) {
    this.cardReader = cardReader as ObservableReader
    val smartCardService = SmartCardServiceProvider.getService()
    cardSelectionManager = smartCardService.createCardSelectionManager()
    val calypsoCardExtensionProvider: CalypsoExtensionService
    try {
      calypsoCardExtensionProvider = CalypsoExtensionService.getInstance()

      cardSelectionManager.prepareSelection(
          calypsoCardExtensionProvider
              .createCardSelection()
              .filterByDfName(CardInfo.KEYPLE_KIT_AID)
              .prepareReadRecord(CardInfo.SFI_EnvironmentAndHolder, CardInfo.RECORD_NUMBER_1))

      cardSelectionManager.prepareSelection(
          calypsoCardExtensionProvider
              .createCardSelection()
              .filterByDfName(CardInfo.NAVIGO_B_AID)
              .prepareReadRecord(CardInfo.SFI_EnvironmentAndHolder, CardInfo.RECORD_NUMBER_1))

      cardSelectionManager.prepareSelection(
          calypsoCardExtensionProvider
              .createCardSelection()
              .filterByDfName(CardInfo.NAVIGO_BPRIME_AID)
              .prepareReadRecord(CardInfo.SFI_EnvironmentAndHolder, CardInfo.RECORD_NUMBER_1))

      cardSelectionManager.scheduleCardSelectionScenario(
          cardReader,
          ObservableCardReader.DetectionMode.REPEATING,
          ObservableCardReader.NotificationMode.ALWAYS)
    } catch (e: KeyplePluginException) {
      Timber.e(e)
    } catch (e: Exception) {
      Timber.e(e)
    }
    activity.onResult("Card reader ready.")
  }

  /**
   * Processes the incoming card event (insertion / removal).
   *
   * A transaction is carried out with the cards that have been successfully selected. This
   * transaction will be done with or without security depending on whether a SAM is present or not.
   */
  fun onReaderEvent(readerEvent: CardReaderEvent?) {

    timestamp = System.currentTimeMillis()
    activity.onResult("New ReaderEvent received : ${readerEvent?.type?.name}")

    when (readerEvent?.type) {
      CardReaderEvent.Type.CARD_MATCHED -> {
        val selectionsResult =
            cardSelectionManager.parseScheduledCardSelectionsResponse(
                readerEvent.scheduledCardSelectionsResponse)
        val calypsoCard = selectionsResult.activeSmartCard as CalypsoCard
        activity.onResult(
            "Card ${ByteArrayUtil.toHex(calypsoCard.applicationSerialNumber)} detected with DFNAME: ${ByteArrayUtil.toHex(calypsoCard.dfName)}")
        val efEnvironmentHolder = calypsoCard.getFileBySfi(CardInfo.SFI_EnvironmentAndHolder)
        activity.onResult(
            "Environment and Holder file:\n${
                            ByteArrayUtil.toHex(
                                efEnvironmentHolder.data.content
                            )
                        }")
        try {
          runCardTransaction(cardReader, calypsoCard, cardSecuritySetting)
          val counter =
              calypsoCard
                  .getFileBySfi(CardInfo.SFI_Counter1)
                  .data
                  .getContentAsCounterValue(CardInfo.RECORD_NUMBER_1)
          val eventLog =
              ByteArrayUtil.toHex(calypsoCard.getFileBySfi(CardInfo.SFI_EventLog).data.content)
          val contractList =
              ByteArrayUtil.toHex(calypsoCard.getFileBySfi(CardInfo.SFI_ContractList).data.content)
          val contracts =
              ByteArrayUtil.toHex(calypsoCard.getFileBySfi(CardInfo.SFI_Contracts).data.content)
          activity.onResult("Contract list: $contractList")
          activity.onResult("Contract: $contracts")
          activity.onResult("Counter value: $counter")
          activity.onResult("EventLog file:\n$eventLog")
          activity.onAction("Waiting for card removal...")
        } catch (e: KeyplePluginException) {
          Timber.e(e)
          activity.onResult("Exception: ${e.message}")
        } catch (e: Exception) {
          Timber.e(e)
          activity.onResult("Exception: ${e.message}")
        }
        cardReader.finalizeCardProcessing()
      }
      CardReaderEvent.Type.CARD_INSERTED -> {
        activity.onResult("Card detected but AID didn't match the selection")
        activity.onAction("Waiting for card removal...")
        cardReader.finalizeCardProcessing()
      }
      CardReaderEvent.Type.CARD_REMOVED -> {
        activity.onResult("Card removed")
        activity.onHeader("Waiting for a card...")
      }
      else -> {
        // Do nothing
      }
    }
  }

  /** Runs the card transaction with or without security depending of the availability of a SAM. */
  private fun runCardTransaction(
      cardReader: CardReader,
      calypsoCard: CalypsoCard,
      cardSecuritySetting: CardSecuritySetting?
  ) {
    if (cardSecuritySetting == null) {
      runCardTransactionWithoutSam(cardReader, calypsoCard)
    } else {
      runCardTransactionWithSam(cardReader, calypsoCard, cardSecuritySetting)
    }
  }

  /** Runs the card transaction when no SAM is available. */
  private fun runCardTransactionWithoutSam(cardReader: CardReader, calypsoCard: CalypsoCard) {
    val calypsoCardExtensionProvider = CalypsoExtensionService.getInstance()
    val cardTransaction =
        calypsoCardExtensionProvider.createCardTransactionWithoutSecurity(cardReader, calypsoCard)

    // Read 4 files
    // record size is optional when out of a secure session
    cardTransaction
        .prepareReadRecord(CardInfo.SFI_EventLog, CardInfo.RECORD_NUMBER_1)
        .prepareReadRecord(CardInfo.SFI_ContractList, CardInfo.RECORD_NUMBER_1)
        .prepareReadRecord(CardInfo.SFI_Contracts, CardInfo.RECORD_NUMBER_1)
        .prepareReadRecord(CardInfo.SFI_Counter1, CardInfo.RECORD_NUMBER_1)

    cardTransaction.processCardCommands()

    val transactionTime = System.currentTimeMillis() - timestamp
    Timber.d("Card transaction ended successfully (%d ms)", transactionTime)
    // Please note! The indicated transaction time does not take into account the selection phase
    activity.onResult("Transaction time $transactionTime ms")
  }

  /** Runs the card transaction when a SAM is available. */
  private fun runCardTransactionWithSam(
      cardReader: CardReader,
      calypsoCard: CalypsoCard,
      cardSecuritySetting: CardSecuritySetting
  ) {

    // Open a Secure Session with the DEBIT key
    // Read 3 files
    // record size is mandatory when inside of a secure session
    val cardTransactionManager =
        CalypsoExtensionService.getInstance()
            .createCardTransaction(cardReader, calypsoCard, cardSecuritySetting)
            .prepareReadRecords(
                CardInfo.SFI_EventLog,
                CardInfo.RECORD_NUMBER_1,
                CardInfo.RECORD_NUMBER_1,
                CardInfo.RECORD_SIZE)
            .prepareReadRecords(
                CardInfo.SFI_ContractList,
                CardInfo.RECORD_NUMBER_1,
                CardInfo.RECORD_NUMBER_1,
                CardInfo.RECORD_SIZE)
            .prepareReadRecords(
                CardInfo.SFI_Counter1,
                CardInfo.RECORD_NUMBER_1,
                CardInfo.RECORD_NUMBER_1,
                CardInfo.RECORD_SIZE)
            .processOpening(WriteAccessLevel.LOAD)

    /*
    Place for the analysis of the context and the list of contracts
    */

    // read the elected contract
    cardTransactionManager
        .prepareReadRecords(
            CardInfo.SFI_Contracts,
            CardInfo.RECORD_NUMBER_1,
            CardInfo.RECORD_NUMBER_1,
            CardInfo.RECORD_SIZE)
        .prepareIncreaseCounter(CardInfo.SFI_Counter1, 1, 1)
        .processCardCommands()

    /*
    Place for the analysis of the contracts
    */

    // add an event record and close the Secure Session
    cardTransactionManager
        .prepareAppendRecord(CardInfo.SFI_EventLog, CardInfo.eventLog_dataFill)
        .processClosing()

    val transactionTime = System.currentTimeMillis() - timestamp
    Timber.d("Card transaction ended successfully (%d ms)", transactionTime)
    // Please note! The indicated transaction time does not take into account the selection phase
    activity.onResult("Transaction time $transactionTime ms")
  }

  /**
   * Sets up the [CardResourceService] to provide a Calypso SAM C1 resource when requested.
   *
   * Creates a #CardSecuritySetting when a SAM resource
   *
   * @param plugin The plugin to which the SAM reader belongs.
   * @param readerNameRegex A regular expression matching the expected SAM reader name.
   * @param samProfileName A string defining the SAM profile.
   * @throws IllegalStateException If the expected card resource is not found.
   */
  fun setupSecurityService(plugin: Plugin, readerNameRegex: String?) {
    val samProfileName = CardInfo.SamType.name
    // create a card resource extension expecting a SAM "C1".
    val samSelection =
        CalypsoExtensionService.getInstance()
            .createSamSelection()
            .filterByProductType(CardInfo.SamType)

    val samCardResourceExtension =
        CalypsoExtensionService.getInstance().createSamResourceProfileExtension(samSelection)

    // Get the service
    cardResourceService = CardResourceServiceProvider.getService()

    // configure and start the card resource service
    cardResourceService!!
        .configurator
        .withPlugins(
            PluginsConfigurator.builder()
                .addPlugin(plugin) { reader: Reader ->
                  Timber.d("Nothing to configure for reader '%s'", reader.name)
                }
                .build())
        .withCardResourceProfiles(
            CardResourceProfileConfigurator.builder(samProfileName, samCardResourceExtension)
                .withReaderNameRegex(readerNameRegex)
                .build())
        .configure()

    cardResourceService!!.start()

    // verify the resource availability
    val samResource = cardResourceService!!.getCardResource(samProfileName)
    if (samResource != null) {
      cardSecuritySetting =
          CalypsoExtensionService.getInstance()
              .createCardSecuritySetting()
              .setSamResource(samResource.reader, samResource.smartCard as CalypsoSam)
              .enableMultipleSession()

      Timber.i(
          "A valid SAM resource was found for profile '$samProfileName' from reader '$readerNameRegex' in plugin '${plugin.name}'")
      activity.onResult("SAM resource found. Continue with security.")
    } else {
      Timber.i(
          "No valid SAM resource was found for profile '$samProfileName' from reader '$readerNameRegex' in plugin '${plugin.name}'")
      activity.onResult("No SAM resource found. Continue without security.")
    }
  }

  fun cleanUp() {
    Timber.d("Stopping card resource service...")
    cardResourceService?.stop()
  }
}
