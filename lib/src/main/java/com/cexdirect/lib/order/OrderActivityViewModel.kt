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

package com.cexdirect.lib.order

import androidx.annotation.VisibleForTesting
import androidx.databinding.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.order.confirmation.CheckCode
import com.cexdirect.lib.order.confirmation.TdsData
import com.cexdirect.lib.order.events.UploadPhotoEvent
import com.cexdirect.lib.order.identity.*
import com.cexdirect.lib.order.identity.country.CountryAdapter
import com.cexdirect.lib.order.identity.country.CountryClickEvent
import com.cexdirect.lib.order.identity.img.ImageReference
import com.cexdirect.lib.util.DH
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.symbolMap
import com.cexdirect.lib.views.CollapsibleLayout
import com.cexdirect.livedatax.switchMap

@Suppress("MagicNumber")
class OrderActivityViewModel(
    paymentApi: PaymentApi,
    orderApi: OrderApi,
    stringProvider: StringProvider,
    private val messenger: Messenger,
    dh: DH,
    val emailChangedEvent: StringLiveEvent
) : LegalViewModel() {

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
    val userDocs = UserDocs(stringProvider)
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
    val tdsData = ObservableField<TdsData?>()

    val paymentInfo = ObservableField<PaymentInfo>()
    val txId = ObservableField("")

    val statusWatcher = StatusWatcher()

    // --- Requests --- //
    private val api = OrderProcessingApi(paymentApi, orderApi, messenger, this)
    val newOrderInfoRequest = api.newOrderInfo
    val sendToVerificationRequest = api.verificationResult
    val sendToProcessingRequest = api.processingResult
    val uploadPhotoRequest: LiveData<Resource<Void>> = api.uploadResult
    val sendBasePaymentDataRequest: LiveData<Resource<OrderInfoData>> = api.basePaymentDataResult
    val sendExtraPaymentDataRequest = api.extraPaymentDataResult
    val changeEmailRequest = emailChangedEvent.switchMap {
        api.apply { changeEmail(it) }.changeEmailResult
    }
    val changeCheckCodeRequest = api.newCheckCode
    val checkCodeRequest = api.checkCode
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
        userDocs.uploadAction = {
            api.uploadPhoto {
                when (it) {
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
        }
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

    private fun updateOrderId(orderId: String) {
        this.orderId.set(orderId)
        Direct.pendingOrderId = orderId
    }

    fun copyOrderId() {
        copyEvent.postValue(orderId.get())
    }

    private fun createOrder() {
        userEmail.forceValidate()
        userCountry.forceValidate()

        if (userEmail.isValid() && userCountry.isValid()) {
            Direct.userEmail = userEmail.email
            api.createNewOrder(
                NewOrderData(
                    userEmail.email,
                    userCountry.selectedCountry.code,
                    userCountry.selectedState.code.ifBlank { null },
                    MonetaryData(
                        orderAmounts.selectedFiatAmount,
                        orderAmounts.selectedFiatCurrency
                    ),
                    MonetaryData(
                        orderAmounts.selectedCryptoAmount,
                        orderAmounts.selectedCryptoCurrency
                    )
                )
            ) { updateOrderId(it) }
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
            api.sendBasePaymentData {
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
        }
    }

    private fun paymentDataValid() = userDocs.isValid() &&
            userCardData.isValid() &&
            userTerms.accepted() &&
            userWallet.isValid() &&
            ssnPresent()

    private fun startVerificationChain() {
        api.startVerification(
            { WalletAddressData(userWallet.address, orderAmounts.selectedCryptoCurrency) },
            { userCardData.getPublicKey() },
            {
                VerificationData(
                    secretId = it.secretId,
                    cardData = userCardData.generateVerificationCardData(it.publicKey)
                )
            }
        )
    }

    fun uploadExtraPaymentData() {
        forceValidateExtras()

        if (extrasValid()) {
            api.sendExtraPaymentData {
                extras.apply {
                    // Do not send the following entries
                    remove("userResidentialCountry")
                    remove("billingCountry")
                    remove("billingState")
                }.let { PaymentData(paymentData = null, additional = it, termUrl = null) }
            }
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

    fun setImage(imgRef: ImageReference) {
        userDocs.setImage(imgRef)
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
            OrderStatus.PSS_READY -> statusWatcher.updateAndDo(OrderStatus.PSS_READY) {
                api.startProcessing(
                    { userCardData.getPublicKey() },
                    {
                        VerificationData(
                            secretId = it.secretId,
                            cardData = userCardData.generateProcessingCardData(it.publicKey)
                        )
                    }
                )
            }
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
        api.checkCode(orderId.get()!!, checkCode.code)
    }

    fun requestNewCheckCode() {
        api.requestNewCheckCode(orderId.get()!!)
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
    fun askFor3ds(threeDS: Tds) {
        orderStep.set(OrderStep.TDS)
        tdsData.set(TdsData(threeDS.url, threeDS.data, threeDS.txId, orderId.get()!!))
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
        private val emailChangedEvent: StringLiveEvent
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OrderActivityViewModel(
                paymentApi,
                orderApi,
                stringProvider,
                messenger,
                dh,
                emailChangedEvent
            ) as T
    }
}