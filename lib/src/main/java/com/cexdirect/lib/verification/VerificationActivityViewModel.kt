/*
 * Copyright 2019 CEX.​IO Ltd (UK)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.cexdirect.lib.verification

import androidx.annotation.VisibleForTesting
import androidx.databinding.*
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.enqueueWith
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.util.DH
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.symbolMap
import com.cexdirect.lib.verification.confirmation.CheckCode
import com.cexdirect.lib.verification.events.UploadPhotoEvent
import com.cexdirect.lib.verification.identity.*
import com.cexdirect.lib.verification.identity.country.CountryAdapter
import com.cexdirect.lib.verification.identity.country.CountryClickEvent
import com.cexdirect.lib.views.CollapsibleLayout

@Suppress("MagicNumber")
class VerificationActivityViewModel(
    paymentApi: PaymentApi,
    private val orderApi: OrderApi,
    stringProvider: StringProvider,
    private val messenger: Messenger,
    dh: DH,
    val emailChangedEvent: StringLiveEvent,
    dispatcherProvider: CoroutineDispatcherProvider
) : LegalViewModel(dispatcherProvider) {

    // --- Events --- //
    val nextClickEvent = VoidLiveEvent()
    val stepChangeEvent = VoidLiveEvent()
    val returnEvent = VoidLiveEvent()
    val copyEvent = StringLiveEvent()
    val chooseCountryEvent = VoidLiveEvent()
    val chooseStateEvent = VoidLiveEvent()
    val uploadPhotoEvent = UploadPhotoEvent()
    val cvvInfoEvent = VoidLiveEvent()
    val countryClickEvent = CountryClickEvent()
    val countryPickerExitEvent = VoidLiveEvent()
    val toggleSearchEvent = BooleanLiveEvent()
    val resendCodeEvent = VoidLiveEvent()
    val editEmailEvent = VoidLiveEvent()
    val buyMoreEvent = VoidLiveEvent()
    val txIdCopyEvent = StringLiveEvent()
    val txIdOpenEvent = StringLiveEvent()
    val scanQrEvent = VoidLiveEvent()
    val editClickEvent = VoidLiveEvent()
    val scrollRequestEvent = IntLiveEvent()
    // --- Events --- //

    val orderAmounts = OrderAmounts()
    val orderId = ObservableField("")
    val currentStep = ObservableInt(1)
    val pagerAdapter = ObservableField(StepsPagerAdapter(stringProvider, editClickEvent))

    val additionalFields = ObservableField<Map<String, Additional>>(emptyMap())

    val userEmail = UserEmail()
    val userCountry = UserCountry()
    val userCardData = UserCardData(dh)
    val userWallet = UserWallet()
    val userSsn = UserSsn()
    val userDocs: UserDocs /* intentionally specified explicitly */ =
        UserDocs(stringProvider).apply {
            uploadAction = { uploadImage.execute() }
        }
    val userTerms = Terms()
    var extras = ObservableArrayMap<String, String>()
    val validationMap = ObservableArrayMap<String, FieldStatus>()

    val orderStep = ObservableField(OrderStep.LOCATION_EMAIL)
    val locationEmailContentState = ObservableField(CollapsibleLayout.ContentState.EXPANDED)
    val paymentBaseContentState = ObservableField(CollapsibleLayout.ContentState.COLLAPSED)
    val paymentExtraContentState = ObservableField(CollapsibleLayout.ContentState.COLLAPSED)

    val legal = ObservableField(Direct.rules)

    val countryAdapter = ObservableField<CountryAdapter>()
    var currentCountryData: List<CountryData> = emptyList()
    val countrySearch = ObservableField("")
    val showCountrySearch = ObservableBoolean(false)

    val checkCode = CheckCode()
    val _3dsData = com.cexdirect.lib.verification.confirmation._3dsData()

    val paymentInfo = ObservableField<PaymentInfo>()
    val txId = ObservableField("")

    val statusWatcher = StatusWatcher()

    // --- Requests --- //
    val createOrder = orderApi.createNewOrder(this) {
        NewOrderData(
            userEmail.email,
            userCountry.selectedCountry.code,
            MonetaryData(Direct.pendingFiatAmount.amount, Direct.pendingFiatAmount.currency),
            MonetaryData(Direct.pendingCryptoAmount.amount, Direct.pendingCryptoAmount.currency)
        )
    }

    private val walletVerification =
        paymentApi.verifyWalletAddress(this) {
            WalletAddressData(userWallet.address, orderAmounts.selectedCryptoCurrency)
        }

    private val verificationKey =
        Transformations.switchMap(walletVerification) {
            it.enqueueWith({
                orderApi.getVerificationKey(this) {
                    PublicKeyData(userCardData.getPublicKey())
                }.apply { execute() }
            })
        }

    val verificationResult =
        Transformations.switchMap(verificationKey) { resource ->
            resource.enqueueWith({
                it.data.let {
                    orderApi.sendToVerification(this) {
                        VerificationData(
                            secretId = it!!.secretId,
                            cardData = userCardData.generateVerificationCardData(it.publicKey)
                        )
                    }.apply { execute() }
                }
            })
        }

    private val processingKey =
        orderApi.getProcessingKey(this) {
            PublicKeyData(userCardData.getPublicKey())
        }

    val processingResult = Transformations.switchMap(processingKey) { resource ->
        resource.enqueueWith({
            it.data.let {
                orderApi.sendToProcessing(this) {
                    VerificationData(
                        secretId = it!!.secretId,
                        cardData = userCardData.generateProcessingCardData(it.publicKey)
                    )
                }.apply { execute() }
            }
        })
    }

    val getOrderInfo = orderApi.checkOrderInfo(this)

    val uploadImage = orderApi.uploadImage(this) {
        when (userDocs.currentPhotoType) {
            PhotoType.SELFIE -> {
                ImageBody(
                    ImageData(
                        documentType = DocumentType.SELFIE.value,
                        base64image = userDocs.getSelfieArray()
                    )
                )
            }
            PhotoType.ID, PhotoType.ID_BACK -> {
                ImageBody(
                    ImageData(
                        documentType = userDocs.documentType.value,
                        base64image = userDocs.getDocumentPhotosArray()
                    )
                )
            }
        }
    }

    val basePaymentData = orderApi.sendPaymentData(this) {
        val payment = Payment(
            userCardData.getCardBin(),
            userCardData.expiry,
            Wallet(userWallet.address, userWallet.tag.ifEmpty { null })
        )

        val additional = if (userCountry.shouldShowState) {
            mapOf("billingSsn" to userSsn.getFormattedValue())
        } else {
            emptyMap()
        }
        PaymentData(payment, additional)
    }

    val extraPaymentData = orderApi.updatePaymentData(this) {
        extras.apply {
            // Do not send the following entries
            remove("userResidentialCountry")
            remove("billingCountry")
            remove("billingState")
        }.let { PaymentData(paymentData = null, additional = it, termUrl = null) }
    }

    val changeEmail = Transformations.switchMap(emailChangedEvent) {
        orderApi.changeEmail(this) { ChangeEmailRequest(newEmail = it) }.apply { execute() }
    }

    val newCheckCode = orderApi.resendCheckCode(this) { orderId.get()!! }

    val checkCodeResult = orderApi.checkCode(this) {
        CheckCodeData(orderId.get()!!, checkCode.code)
    }
    // --- Requests --- //

    init {
        countrySearch.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                countrySearch.get()?.takeIf { it.isNotBlank() }?.let { part ->
                    countryAdapter.get()!!.items =
                        currentCountryData.filter { it.name.contains(part, true) }
                } ?: run { countryAdapter.get()!!.items = currentCountryData }
            }
        })
        extras.addOnMapChangedCallback(
            object :
                ObservableMap.OnMapChangedCallback<ObservableMap<String?, String?>, String?, String?>() {
                override fun onMapChanged(sender: ObservableMap<String?, String?>, key: String?) {
                    key?.let {
                        if (extras[it].isNullOrBlank()) {
                            validationMap[it] = FieldStatus.EMPTY
                        } else {
                            validationMap[it] = FieldStatus.VALID
                        }
                    }
                }
            }
        )
        additionalFields.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                additionalFields.get()?.entries?.forEach {
                    if (it.value.req && it.value.editable) {
                        validationMap[it.key] = FieldStatus.EMPTY
                    }
                }
            }
        })
    }

    fun setOrderAmounts(crypto: String, cryptoAmount: String, fiat: String, fiatAmount: String) {
        orderAmounts.selectedCryptoCurrency = crypto
        orderAmounts.selectedCryptoAmount = cryptoAmount
        orderAmounts.selectedFiatCurrency = fiat
        orderAmounts.selectedFiatAmount = fiatAmount
        pagerAdapter.get()!!.setOrderAmounts(cryptoAmount, crypto, fiatAmount, fiat)
    }

    private fun changeOrderStep() {
        if (currentStep.get() < 3) {
            stepChangeEvent.call()
        } else {
            returnEvent.call()
        }
    }

    fun proceed() {
        if (currentStep.get() < 4) {
            currentStep.set(currentStep.get() + 1)
        }
    }

    fun updateOrderId(orderId: String) {
        this.orderId.set(orderId)
        Direct.pendingOrderId = orderId
    }

    fun copyOrderId() {
        copyEvent.postValue(orderId.get())
    }

    fun createOrder() {
        userEmail.forceValidate()
        userCountry.forceValidate()

        if (userEmail.isValid() && userCountry.isValid()) {
            Direct.userEmail = userEmail.email
            createOrder.execute()
        }
    }

    private fun ssnPresent() =
        if (userCountry.shouldShowState) userSsn.isSsnValid() else true

    private fun forceValidateExtras() {
        validationMap.entries.forEach {
            if (it.value == FieldStatus.EMPTY) {
                it.setValue(FieldStatus.INVALID)
            }
        }
    }

    private fun extrasValid() =
        validationMap.entries.takeIf { it.size > 0 }?.fold(false, { _, entry ->
            entry.value == FieldStatus.VALID
        }) ?: true

    fun nextStep() {
        nextClickEvent.call()
    }

    fun chooseCountry() {
        chooseCountryEvent.call()
    }

    fun chooseState() {
        chooseStateEvent.call()
    }

    fun uploadPhoto(type: PhotoType) {
        userDocs.currentPhotoType = type
        uploadPhotoEvent.value = type
    }

    fun subscribeToOrderInfo() = messenger.subscribeToOrderInfo()

    fun stopSubscriptions() {
        messenger.clear()
    }

    fun toggleLocationEmail() {
        when (locationEmailContentState.get()) {
            CollapsibleLayout.ContentState.COLLAPSED -> locationEmailContentState.set(
                CollapsibleLayout.ContentState.EXPANDED
            )
            CollapsibleLayout.ContentState.EXPANDED -> locationEmailContentState.set(
                CollapsibleLayout.ContentState.COLLAPSED
            )
        }
    }

    fun togglePaymentBase() {
        when (paymentBaseContentState.get()) {
            CollapsibleLayout.ContentState.COLLAPSED -> paymentBaseContentState.set(
                CollapsibleLayout.ContentState.EXPANDED
            )
            CollapsibleLayout.ContentState.EXPANDED -> paymentBaseContentState.set(
                CollapsibleLayout.ContentState.COLLAPSED
            )
        }
    }

    fun setPaymentBase() {
        getOrderInfo.execute()
        orderStep.set(OrderStep.PAYMENT_BASE)
        locationEmailContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
        paymentBaseContentState.set(CollapsibleLayout.ContentState.EXPANDED)
    }

    private fun uploadBasePaymentData() {
        userDocs.forceValidate()
        userCardData.forceValidate()
        userTerms.forceValidate()
        userWallet.forceValidate()
        if (userCountry.shouldShowState) {
            userSsn.forceValidate()
        }

        if (paymentDataValid()) {
            basePaymentData.execute()
        }
    }

    private fun paymentDataValid() = userDocs.isValid()
            && userCardData.isValid()
            && userTerms.accepted()
            && userWallet.isValid()
            && ssnPresent()

    private fun startVerificationChain() {
        walletVerification.execute()
    }

    fun uploadExtraPaymentData() {
        forceValidateExtras()

        if (extrasValid()) {
            extraPaymentData.execute()
        }
    }

    fun setRequiredImages(images: Images) {
        userDocs.requiredImages = images
    }

    fun showCvvInfo() {
        cvvInfoEvent.call()
    }

    fun scanQrCode() {
        scanQrEvent.call()
    }

    fun setImage(imageBase64: String) {
        userDocs.setImage(imageBase64)
    }

    fun closeCountryPicker() {
        countryPickerExitEvent.call()
    }

    fun showCountrySearch() {
        toggleSearchEvent.postValue(true)
    }

    fun hideCountrySearch() {
        toggleSearchEvent.postValue(false)
    }

    fun clearSearch() {
        countrySearch.set("")
        showCountrySearch.set(false)
    }

    fun setDocumentStatusToValid() {
        when (userDocs.currentPhotoType) {
            PhotoType.ID, PhotoType.ID_BACK -> {
                userDocs.documentFrontStatus = FieldStatus.VALID
                userDocs.documentBackStatus = FieldStatus.VALID
            }
            PhotoType.SELFIE -> {
                userDocs.selfieStatus = FieldStatus.VALID
            }
        }
    }

    fun setImageSizeInvalid() {
        userDocs.setImageSizeInvalid()
    }

    fun updateOrderStatus(
        data: OrderInfoData,
        rejectAction: () -> Unit,
        hideAction: () -> Unit,
        scrollAction: () -> Unit
    ) {
        when (data.orderStatus) {
            OrderStatus.REJECTED -> statusWatcher.updateAndDo(OrderStatus.REJECTED, rejectAction)
            OrderStatus.IVS_READY -> {
                statusWatcher.updateAndDo(OrderStatus.IVS_READY) {
                    startVerificationChain()
                }
            }
            OrderStatus.PSS_WAITDATA -> {
                statusWatcher.updateAndDo(OrderStatus.PSS_WAITDATA) {
                    data.additional
                        .takeIf { it.filter { it.value.req }.isNotEmpty() }
                        .let {
                            additionalFields.set(addCrutchedData(it))
                            orderStep.set(OrderStep.PAYMENT_EXTRA)
                            paymentBaseContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
                            paymentExtraContentState.set(CollapsibleLayout.ContentState.EXPANDED)
                        }
                    hideAction.invoke()
                    scrollAction.invoke()
                }
            }
            OrderStatus.PSS_READY -> statusWatcher.updateAndDo(OrderStatus.PSS_READY) { processingKey.execute() }
            OrderStatus.PSS_PENDING -> statusWatcher.updateAndDo(OrderStatus.PSS_PENDING) {}
            OrderStatus.PSS_3DS_REQUIRED, OrderStatus.WAITING_FOR_CONFIRMATION, OrderStatus.COMPLETE ->
                statusWatcher.updateAndDo(data.orderStatus) {
                    changeOrderStep()
                    hideAction.invoke()
                }
            else -> {
            }
        }
    }

    /**
     * Handle BE issues
     */
    private fun addCrutchedData(additional: Map<String, Additional>?): Map<String, Additional> =
        additional?.let {
            HashMap(it).apply {
                this["userResidentialCountry"] = Additional(
                    userCountry.selectedCountry.code,
                    this["userResidentialCountry"]!!.req,
                    false
                )
                this["billingCountry"] = Additional(
                    userCountry.selectedCountry.code,
                    this["billingCountry"]!!.req,
                    false
                )
                this["billingState"] = Additional(
                    userCountry.selectedState.code.ifEmpty { null },
                    this["billingState"]!!.req,
                    false
                )
            }
        } ?: emptyMap()

    fun handleNextClick() {
        when (orderStep.get()) {
            OrderStep.LOCATION_EMAIL -> createOrder()
            OrderStep.PAYMENT_BASE -> {
                if (statusWatcher.getStatus() == OrderStatus.INCOMPLETE) {
                    uploadBasePaymentData()
                } else if (statusWatcher.getStatus() == OrderStatus.IVS_READY) {
                    startVerificationChain()
                }
            }
            OrderStep.PAYMENT_EXTRA -> uploadExtraPaymentData()
            else -> error("Illegal step: $orderStep")
        }
    }


    fun resendCheckCode() {
        resendCodeEvent.call()
    }

    fun editEmail() {
        editEmailEvent.call()
    }

    fun submitCode() {
        checkCodeResult.execute()
    }

    fun requestNewCheckCode() {
        newCheckCode.execute()
    }

    fun updateUserEmail(email: String) {
        userEmail.email = email
        Direct.userEmail = email
    }

    fun updateConfirmationStatus(data: OrderInfoData, rejectAction: () -> Unit) {
        when (data.orderStatus) {
            OrderStatus.PSS_3DS_REQUIRED -> statusWatcher.updateAndDo(OrderStatus.PSS_3DS_REQUIRED) {
                askFor3ds(data.threeDS!!)
            }
            OrderStatus.WAITING_FOR_CONFIRMATION -> statusWatcher.updateAndDo(OrderStatus.WAITING_FOR_CONFIRMATION) {
                askForEmailConfirmation()
            }
            OrderStatus.COMPLETE -> statusWatcher.updateAndDo(OrderStatus.COMPLETE) { confirmOrder() }
            OrderStatus.REJECTED -> statusWatcher.updateAndDo(OrderStatus.REJECTED, rejectAction)
            else -> { /* do nothing */
            }
        }
    }

    @VisibleForTesting
    fun askFor3ds(threeDS: _3Ds) {
        orderStep.set(OrderStep.TDS)
        _3dsData.apply {
            _3dsUrl = threeDS.url
            _3dsExtras = threeDS.data
            txId = threeDS.txId
        }
    }

    @VisibleForTesting
    fun askForEmailConfirmation() {
        orderStep.set(OrderStep.EMAIL_CONFIRMATION)
        checkCode.startTimer()
    }

    fun restartResendTimer() {
        checkCode.restartTimer()
    }

    private fun confirmOrder() {
        orderStep.set(OrderStep.CONFIRMED)
        changeOrderStep()
    }

    fun buyMore() {
        buyMoreEvent.call()
    }

    fun updatePaymentInfo(data: OrderInfoData) {
        when (data.orderStatus) {
            OrderStatus.COMPLETE -> statusWatcher.updateAndDo(OrderStatus.COMPLETE) {
                paymentInfo.set(data.paymentInfo)
            }
            OrderStatus.FINISHED -> statusWatcher.updateAndDo(OrderStatus.FINISHED) {
                paymentInfo.set(data.paymentInfo)
                txId.set(data.paymentInfo!!.txId!!)
                messenger.removeOrderInfoSubscription()
            }
            else -> { // do nothing
            }
        }
    }

    fun copyTxId(txId: String) {
        txIdCopyEvent.postValue(txId)
    }

    fun openTxDetails(txId: String) {
        symbolMap.getValue(orderAmounts.selectedCryptoCurrency).transactionBrowserAddress?.let {
            txIdOpenEvent.postValue("$it$txId")
        }
    }

    fun setUnsupportedFormat() {
        userDocs.setUnsupportedFormat()
    }

    fun requestScrollTo(coordinate: Int) {
        scrollRequestEvent.postValue(coordinate)
    }

    class Factory(
        private val paymentApi: PaymentApi,
        private val orderApi: OrderApi,
        private val stringProvider: StringProvider,
        private val messenger: Messenger,
        private val dh: DH,
        private val emailChangedEvent: StringLiveEvent,
        private val dispatcherProvider: CoroutineDispatcherProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VerificationActivityViewModel(
                paymentApi,
                orderApi,
                stringProvider,
                messenger,
                dh,
                emailChangedEvent,
                dispatcherProvider
            ) as T
    }
}

