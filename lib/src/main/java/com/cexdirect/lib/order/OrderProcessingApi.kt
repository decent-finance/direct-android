/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.network.ws.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@OpenForTesting
class OrderProcessingApi(
    private val paymentFlow: PaymentFlow,
    private val orderFlow: OrderFlow,
    private val messenger: Messenger
) {

    val newOrderResult = MutableLiveData<Resource<OrderInfoData>>()
    val verificationResult = MutableLiveData<Resource<Void>>()
    val processingResult = MutableLiveData<Resource<Void>>()
    val uploadResult = MutableLiveData<Resource<Void>>()
    val basePaymentDataResult = MutableLiveData<Resource<OrderInfoData>>()
    val extraPaymentDataResult = MutableLiveData<Resource<OrderInfoData>>()
    val checkCode = MutableLiveData<Resource<OrderData>>()

    fun createNewOrder(
        scope: CoroutineScope,
        data: NewOrderData,
        onOrderCreated: (orderId: String) -> Unit
    ) {
        orderFlow.createNewOrder(data)
            .onStart { newOrderResult.value = Loading() }
            .onEach { onOrderCreated.invoke(it.data.orderId) }
            .flatMapConcat { orderFlow.checkOrderInfo() }
            .catch { newOrderResult.value = it.mapFailure() }
            .onEach { newOrderResult.value = Success(it.data) }
            .launchIn(scope)
    }

    fun startVerification(
        scope: CoroutineScope,
        walletAddress: WalletAddressData,
        pubKey: String,
        verificationData: (data: PublicKeyResponseData) -> VerificationData
    ) {
        paymentFlow.verifyWalletAddress(walletAddress)
            .onStart { verificationResult.value = Loading() }
            .flatMapConcat { orderFlow.getVerificationKey(PublicKeyData(pubKey)) }
            .flatMapConcat { orderFlow.sendToVerification { verificationData.invoke(it.data) } }
            .catch { verificationResult.value = it.mapFailure() }
            .onEach { verificationResult.value = Success() }
            .launchIn(scope)
    }

    fun startProcessing(
        scope: CoroutineScope,
        pubKey: String,
        processingData: (data: PublicKeyResponseData) -> VerificationData
    ) {
        orderFlow.getProcessingKey(PublicKeyData(pubKey))
            .onStart { processingResult.value = Loading() }
            .flatMapConcat { orderFlow.sendToProcessing { processingData.invoke(it.data) } }
            .catch { processingResult.value = it.mapFailure() }
            .onEach { processingResult.value = Success() }
            .launchIn(scope)
    }

    fun uploadPhoto(scope: CoroutineScope, imageData: () -> ImageBody) {
        orderFlow.uploadImage(imageData)
            .onStart { uploadResult.value = Loading() }
            .catch { uploadResult.value = it.mapFailure() }
            .onEach { uploadResult.value = Success() }
            .launchIn(scope)
    }

    fun sendBasePaymentData(scope: CoroutineScope, paymentData: PaymentData) {
        orderFlow.sendPaymentData(paymentData)
            .onStart { basePaymentDataResult.value = Loading() }
            .catch { basePaymentDataResult.value = it.mapFailure() }
            .onEach { basePaymentDataResult.value = Success(it.data) }
            .launchIn(scope)
    }

    fun sendExtraPaymentData(scope: CoroutineScope, paymentData: PaymentData) {
        orderFlow.updatePaymentData(paymentData)
            .onStart { extraPaymentDataResult.value = Loading() }
            .catch { extraPaymentDataResult.value = it.mapFailure() }
            .onEach { extraPaymentDataResult.value = Success(it.data) }
            .launchIn(scope)
    }

    fun changeEmail(scope: CoroutineScope, newEmail: String): LiveData<Resource<String>> =
        MutableLiveData<Resource<String>>().apply {
            orderFlow.changeEmail(ChangeEmailRequest(newEmail = newEmail))
                .onStart { this@apply.value = Loading() }
                .catch { this@apply.value = it.mapFailure() }
                .onEach { this@apply.value = Success(it.data.userEmail) }
                .launchIn(scope)
        }

    fun requestNewCheckCode(
        scope: CoroutineScope,
        orderId: String
    ): LiveData<Resource<OrderData>> =
        MutableLiveData<Resource<OrderData>>().apply {
            orderFlow.resendCheckCode(orderId)
                .onStart { this@apply.value = Loading() }
                .catch { this@apply.value = it.mapFailure() }
                .onEach { this@apply.value = Success(it.data) }
                .launchIn(scope)
        }

    fun checkCode(scope: CoroutineScope, orderId: String, code: String) {
        orderFlow.checkCode(CheckCodeData(orderId, code))
            .onStart { checkCode.value = Loading() }
            .catch { checkCode.value = it.mapFailure() }
            .onEach { checkCode.value = Success(it.data) }
            .launchIn(scope)
    }

    fun subscribeToOrderInfo() = messenger.subscribeToOrderInfo()

    fun clear() {
        messenger.clear()
    }

    fun removeOrderInfoSubscription() {
        messenger.removeOrderInfoSubscription()
    }
}
