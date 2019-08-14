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

package com.cexdirect.lib.verification.identity

import android.view.View
import androidx.databinding.*
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib._network.OrderApi
import com.cexdirect.lib._network.PaymentApi
import com.cexdirect.lib._network.enqueueWith
import com.cexdirect.lib._network.models.*
import com.cexdirect.lib._network.ws.Messenger
import com.cexdirect.lib._util.*
import com.cexdirect.lib.verification.NextClickEvent
import com.cexdirect.lib.views.CollapsibleLayout
import com.google.gson.Gson
import com.mukesh.countrypicker.Country
import org.bouncycastle.util.encoders.Base64

class IdentityFragmentViewModel private constructor(
    private val paymentApi: PaymentApi,
    private val orderApi: OrderApi,
    private val stringProvider: StringProvider,
    private val messenger: Messenger,
    private val dh: DH,
    dispatcherProvider: CoroutineDispatcherProvider
) : BaseObservableViewModel(dispatcherProvider) {

    val additionalFields = ObservableField<Map<String, Additional>>(emptyMap())
    val images = ObservableField(
        Images(
            isIdentityDocumentsRequired = false,
            isSelfieRequired = false
        )
    )

    var currentPhotoType: PhotoType? = null

    val nextClickEvent = NextClickEvent()
    val chooseCountryEvent = ChooseCountryEvent()
    val uploadPhotoEvent = UploadPhotoEvent()
    val cvvInfoEvent = CvvInfoEvent()

    val userEmail = ObservableField("")
    val emailStatus = userEmail.map { checkEmailStatus(it) }
    val userCountry = ObservableField<Country>()
    val shouldShowUsInputs = userCountry.map {
        "United States" == userCountry.get()?.name
    }
    val userState = ObservableField("")

    val cardNumber = ObservableField("")
    val cardExpiry = ObservableField("")
    val cardCvv = ObservableField("")
    val walletAddress = ObservableField("")
    lateinit var walletCurrency: String
    val walletTag = ObservableField<String>()
    val isWalletValid = ObservableField(true)

    val documentTypeSelected = ObservableBoolean(false)
    val documentTypeText = ObservableField(stringProvider.provideString(R.string.cexd_take_pic_id))
    val documentImage = ObservableInt(R.drawable.ic_pic_id_card)
    val documentImageBack = ObservableInt(R.drawable.ic_pic_id_card)
    val termsAccepted = ObservableBoolean(false)

    val locationEmailContentState = ObservableField(CollapsibleLayout.ContentState.EXPANDED)
    val paymentBaseContentState = ObservableField(CollapsibleLayout.ContentState.COLLAPSED)
    val paymentExtraContentState = ObservableField(CollapsibleLayout.ContentState.COLLAPSED)

    val legal = ObservableField(Direct.rules)

    var extras = ObservableArrayMap<String, String>()

    val selectedViewId = ObservableInt(View.NO_ID)

    val verificationStep = ObservableField(VerificationStep.LOCATION_EMAIL)

    val createOrder = orderApi.createNewOrder(this) {
        NewOrderData(
            userEmail.get()!!,
            userCountry.get()!!.code,
            MonetaryData(Direct.pendingFiatAmount.amount, Direct.pendingFiatAmount.currency),
            MonetaryData(Direct.pendingCryptoAmount.amount, Direct.pendingCryptoAmount.currency)
        )
    }

    val walletVerification = paymentApi.verifyWalletAddress(this) {
        WalletAddressData(walletAddress.get()!!, walletCurrency)
    }

    val verificationKey = Transformations.switchMap(walletVerification) {
        it.enqueueWith({
            orderApi.getVerificationKey(this) {
                PublicKeyData(publicKey = dh.publicKey.y.toByteArray().encodeToString())
            }.apply { execute() }
        })
    }

    val verificationResult = Transformations.switchMap(verificationKey) {
        it.enqueueWith({
            it.data.let {
                val vector = dh.byteGenerator(GENERATOR_BYTES)
                val expiry = cardExpiry.get()!!
                val cardData = Gson().toJson(
                    IvsCardData(cardNumber.get()!!.replace(" ", ""), expiry)
                )

                val chash = dh.encrypt(
                    dh.stringGenerator(GENERATOR_OFFSET),
                    cardData,
                    android.util.Base64.decode(it!!.publicKey, android.util.Base64.NO_WRAP),
                    vector
                )

                orderApi.sendToVerification(this) {
                    VerificationData(
                        secretId = it.secretId,
                        cardData = CardData(chash!!, Base64.toBase64String(vector))
                    )
                }.apply { execute() }
            }
        })
    }

    val processingKey = orderApi.getProcessingKey(this) {
        PublicKeyData(publicKey = dh.publicKey.y.toByteArray().encodeToString())
    }

    val processingResult = Transformations.switchMap(processingKey) {
        it.enqueueWith({
            it.data.let {
                val vector = dh.byteGenerator(GENERATOR_BYTES)
                val cardData = Gson().toJson(
                    PssCardData(cardNumber.get()!!.replace(" ", ""), cardCvv.get()!!)
                )

                val chash = dh.encrypt(
                    dh.stringGenerator(GENERATOR_OFFSET),
                    cardData,
                    android.util.Base64.decode(it!!.publicKey, android.util.Base64.NO_WRAP),
                    vector
                )

                orderApi.sendToProcessing(this) {
                    VerificationData(
                        secretId = it.secretId,
                        cardData = CardData(chash!!, Base64.toBase64String(vector))
                    )
                }.apply { execute() }
            }
        })
    }

    val orderInfo = orderApi.checkOrderInfo(this)

    val documentPhotos = DocumentPhotos()
    var selfieBase64 = ""

    val uploadImage = orderApi.uploadImage(this) {
        when (currentPhotoType) {
            PhotoType.SELFIE -> {
                ImageBody(
                    ImageData(
                        documentType = DocumentType.SELFIE.value,
                        base64image = arrayOf(selfieBase64)
                    )
                )
            }
            PhotoType.ID, PhotoType.ID_BACK -> {
                ImageBody(
                    ImageData(
                        documentType = documentPhotos.documentType.value,
                        base64image = documentPhotos.imagesBase64.values.toTypedArray()
                    )
                )
            }
            else -> error("Illegal state")
        }
    }

    init {
        selectedViewId.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (selectedViewId.get()) {
                    R.id.fiIdCard -> {
                        documentTypeSelected.set(true)
                        documentPhotos.documentType = DocumentType.ID_CARD
                        documentImage.set(R.drawable.ic_pic_id_card)
                        documentImageBack.set(R.drawable.ic_pic_id_card)
                        documentTypeText.set(stringProvider.provideString(R.string.cexd_take_pic_id))
                    }
                    R.id.fiPassport -> {
                        documentTypeSelected.set(true)
                        documentPhotos.documentType = DocumentType.PASSPORT
                        documentImage.set(R.drawable.ic_pic_passport)
                        documentTypeText.set(stringProvider.provideString(R.string.cexd_take_pic_passport))
                    }
                    R.id.fiDriversLicence -> {
                        documentTypeSelected.set(true)
                        documentPhotos.documentType = DocumentType.DRIVER_LICENCE
                        documentImage.set(R.drawable.ic_pic_driver_license)
                        documentImageBack.set(R.drawable.ic_pic_driver_license)
                        documentTypeText.set(stringProvider.provideString(R.string.cexd_take_pic_licence))
                    }
                    else -> {
                        documentTypeSelected.set(false)
                    }
                }
            }
        })
        documentPhotos.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == BR.shouldSendPhoto) {
                    if (documentPhotos.shouldSendPhoto) {
                        uploadImage.execute()
                    }
                }
            }
        })
        walletAddress.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isWalletValid.set(true)
            }
        })
    }

    fun canSendPaymentData() = cardNumber.get()!!.isNotBlank()
            && cardExpiry.get()!!.isNotBlank()
            && cardCvv.get()!!.isNotBlank()
            && walletAddress.get()!!.isNotBlank()
            && ssnPresent()

    private fun ssnPresent() = if (shouldShowUsInputs.get()!!) !extras["billingSsn"].isNullOrBlank() else true

    val paymentData = orderApi.sendPaymentData(this) {
        val payment =
            Payment(
                cardNumber.get()!!.binifyCardNumber(),
                cardExpiry.get()!!,
                Wallet(walletAddress.get()!!, walletTag.get())
            )

        val additional = if (shouldShowUsInputs.get()!!) mapOf("billingSsn" to extras["billingSsn"]!!) else emptyMap()
        PaymentData(payment, additional)
    }

    val updatePaymentData = orderApi.updatePaymentData(this) {
        PaymentData(paymentData = null, additional = extras, termUrl = null)
    }

    fun isCountrySelected(): Boolean {
        return if (shouldShowUsInputs.get() == true) {
            userCountry.get() != null && userState.get() != null
        } else {
            userCountry.get() != null
        }
    }

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

    fun uploadPhoto(type: PhotoType) {
        uploadPhotoEvent.value = type
        currentPhotoType = type
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

    fun setSelfie(img: String) {
        selfieBase64 = img
        uploadImage.execute()
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

    companion object {
        const val GENERATOR_BYTES = 16
        const val GENERATOR_OFFSET = 5
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val paymentApi: PaymentApi,
        private val orderApi: OrderApi,
        private val stringProvider: StringProvider,
        private val messenger: Messenger,
        private val dh: DH,
        private val dispatcherProvider: CoroutineDispatcherProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            IdentityFragmentViewModel(paymentApi, orderApi, stringProvider, messenger, dh, dispatcherProvider) as T
    }
}

class ChooseCountryEvent : SingleLiveEvent<Void>()
class UploadPhotoEvent : SingleLiveEvent<PhotoType>()
class CvvInfoEvent : SingleLiveEvent<Void>()
