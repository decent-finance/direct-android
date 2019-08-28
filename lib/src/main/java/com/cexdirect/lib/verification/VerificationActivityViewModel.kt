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

import androidx.databinding.*
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import com.cexdirect.lib.*
import com.cexdirect.lib._util.DH
import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.enqueueWith
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.network.ws.Messenger
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
    dispatcherProvider: CoroutineDispatcherProvider
) : LegalViewModel(dispatcherProvider) {

    // --- Events --- //
    val nextClickEvent = VoidLiveEvent()
    val returnEvent = VoidLiveEvent()
    val copyEvent = StringLiveEvent()
    val chooseCountryEvent = VoidLiveEvent()
    val chooseStateEvent = VoidLiveEvent()
    val uploadPhotoEvent = UploadPhotoEvent()
    val cvvInfoEvent = VoidLiveEvent()
    val countryClickEvent = CountryClickEvent()
    val countryPickerExitEvent = VoidLiveEvent()
    val toggleSearchEvent = BooleanLiveEvent()
    // --- Events --- //

    val orderAmounts = OrderAmounts()
    val orderId = ObservableField("")
    val currentStep = ObservableInt(1)
    val pagerAdapter = ObservableField<PagerAdapter>(StepsPagerAdapter())

    val additionalFields = ObservableField<Map<String, Additional>>(emptyMap())
    val images = ObservableField(
        Images(
            isIdentityDocumentsRequired = false,
            isSelfieRequired = false
        )
    )

    val userEmail = UserEmail()
    val userCountry = UserCountry()
    val userCardData = UserCardData(dh)
    val userWallet = UserWallet()
    val userDocs: UserDocs /* intentionally specified explicitly */ =
        UserDocs(stringProvider).apply {
            uploadAction = { uploadImage.execute() }
        }
    var extras = ObservableArrayMap<String, String>()

    val termsAccepted = ObservableBoolean(false)

    val verificationStep = ObservableField(VerificationStep.LOCATION_EMAIL)
    val locationEmailContentState = ObservableField(CollapsibleLayout.ContentState.EXPANDED)
    val paymentBaseContentState = ObservableField(CollapsibleLayout.ContentState.COLLAPSED)
    val paymentExtraContentState = ObservableField(CollapsibleLayout.ContentState.COLLAPSED)

    val legal = ObservableField(Direct.rules)

    val countryAdapter = ObservableField<CountryAdapter>()
    var currentCountryData: List<CountryData> = emptyList()
    val countrySearch = ObservableField("")
    val showCountrySearch = ObservableBoolean(false)

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
        }!!

    val processingKey =
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
    }!!

    val orderInfo = orderApi.checkOrderInfo(this)

    val uploadImage = orderApi.uploadImage(this) {
        when (userDocs.currentPhotoType) {
            PhotoType.SELFIE -> {
                ImageBody(
                    ImageData(
                        documentType = DocumentType.SELFIE.value,
                        base64image = arrayOf(userDocs.selfieBase64)
                    )
                )
            }
            PhotoType.ID, PhotoType.ID_BACK -> {
                ImageBody(
                    ImageData(
                        documentType = userDocs.documentType.value,
                        base64image = userDocs.imagesBase64.values.toTypedArray()
                    )
                )
            }
        }
    }

    val paymentData = orderApi.sendPaymentData(this) {
        val payment =
            Payment(
                userCardData.getCardBin(),
                userCardData.expiry,
                Wallet(userWallet.address, userWallet.tag)
            )

        val additional =
            if (userCountry.shouldShowState) mapOf("billingSsn" to extras["billingSsn"]!!) else emptyMap()
        PaymentData(payment, additional)
    }

    val updatePaymentData = orderApi.updatePaymentData(this) {
        PaymentData(paymentData = null, additional = extras, termUrl = null)
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
    }

    fun returnToStart() {
        returnEvent.call()
    }

    fun next() {
        if (currentStep.get() < 3) {
            nextClickEvent.call()
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

    fun canSendPaymentData() =
        userCardData.isCardDataPresent() && userWallet.walletDataPresent() && ssnPresent()

    private fun ssnPresent() =
        if (userCountry.shouldShowState) !extras["billingSsn"].isNullOrBlank() else true

    fun extrasValid(): Boolean =
        extras.entries.takeIf { it.size > 0 }?.fold(false, { _, entry ->
            entry.value.isNotBlank()
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

    fun unsubscribeFromOrderInfo() {
        messenger.removeOrderInfoSubscription()
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

    fun sendToVerification() {
        walletVerification.execute()
    }

    fun setPaymentBase() {
        orderInfo.execute()
        verificationStep.set(VerificationStep.PAYMENT_BASE)
        locationEmailContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
        paymentBaseContentState.set(CollapsibleLayout.ContentState.EXPANDED)
    }

    fun uploadPaymentData() {
        paymentData.execute()
    }

    fun startVerificationChain() {
        walletVerification.execute()
    }

    fun updatePaymentData() {
        updatePaymentData.execute()
    }

    fun setRequiredImages(images: Images) {
        this.images.set(images)
    }

    fun showCvvInfo() {
        cvvInfoEvent.call()
    }

    fun setImage(imageBase64: String) {
        when (userDocs.currentPhotoType) {
            PhotoType.SELFIE -> userDocs.selfieBase64 = imageBase64
            PhotoType.ID -> userDocs.setFrontPhoto(imageBase64)
            PhotoType.ID_BACK -> userDocs.setBackPhoto(imageBase64)
        }
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

    class Factory(
        private val paymentApi: PaymentApi,
        private val orderApi: OrderApi,
        private val stringProvider: StringProvider,
        private val messenger: Messenger,
        private val dh: DH,
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
                dispatcherProvider
            ) as T
    }
}

