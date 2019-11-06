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

import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.enqueueWith
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.livedatax.switchMap
import kotlinx.coroutines.CoroutineScope

class OrderProcessingApi(
    paymentApi: PaymentApi,
    private val orderApi: OrderApi,
    private val messenger: Messenger,
    private val scope: CoroutineScope
) {

    private lateinit var newOrderData: NewOrderData
    private lateinit var onOrderCreated: (orderId: String) -> Unit
    private lateinit var walletAddressData: () -> WalletAddressData
    private lateinit var verificationPubKey: () -> String
    private lateinit var verificationData: (data: PublicKeyResponseData) -> VerificationData
    private lateinit var processingPubKey: () -> String
    private lateinit var processingData: (data: PublicKeyResponseData) -> VerificationData
    private lateinit var imageData: () -> ImageBody
    private lateinit var basePaymentData: () -> PaymentData
    private lateinit var extraPaymentData: () -> PaymentData
    private lateinit var newEmail: String
    private lateinit var orderId: String
    private lateinit var code: String

    private val newOrder = orderApi.createNewOrder(scope) { newOrderData }
    val newOrderInfo = newOrder.switchMap {
        it.enqueueWith({
            onOrderCreated.invoke(it.data!!.orderId)
            orderApi.checkOrderInfo(scope).apply { execute() }
        })
    }

    private val walletCheckResult =
        paymentApi.verifyWalletAddress(scope) { walletAddressData.invoke() }
    val verificationResult = walletCheckResult.switchMap {
        it.enqueueWith({
            orderApi.getVerificationKey(scope) { PublicKeyData(verificationPubKey.invoke()) }
                .apply { execute() }
        })
    }.switchMap {
        it.enqueueWith({
            orderApi.sendToVerification(scope) { verificationData.invoke(it.data!!) }
                .apply { execute() }
        })
    }

    private val processingKey =
        orderApi.getProcessingKey(scope) { PublicKeyData(processingPubKey.invoke()) }
    val processingResult = processingKey.switchMap {
        it.enqueueWith({
            orderApi.sendToProcessing(scope) { processingData.invoke(it.data!!) }
                .apply { execute() }
        })
    }

    val uploadResult = orderApi.uploadImage(scope) {
        imageData.invoke()
    }

    val basePaymentDataResult = orderApi.sendPaymentData(scope) { basePaymentData.invoke() }

    val extraPaymentDataResult = orderApi.updatePaymentData(scope) { extraPaymentData.invoke() }

    val changeEmailResult = orderApi.changeEmail(scope) { ChangeEmailRequest(newEmail = newEmail) }

    val newCheckCode = orderApi.resendCheckCode(scope) { orderId }

    val checkCode = orderApi.checkCode(scope) { CheckCodeData(orderId, code) }

    fun createNewOrder(data: NewOrderData, onOrderCreated: (orderId: String) -> Unit) {
        this.newOrderData = data
        this.onOrderCreated = onOrderCreated
        newOrder.execute()
    }

    fun startVerification(
        walletAddress: () -> WalletAddressData,
        publicKey: () -> String,
        verificationData: (data: PublicKeyResponseData) -> VerificationData
    ) {
        this.walletAddressData = walletAddress
        this.verificationPubKey = publicKey
        this.verificationData = verificationData
        walletCheckResult.execute()
    }

    fun startProcessing(
        processingPubKey: () -> String,
        processingData: (data: PublicKeyResponseData) -> VerificationData
    ) {
        this.processingPubKey = processingPubKey
        this.processingData = processingData
        processingKey.execute()
    }

    fun uploadPhoto(imageData: () -> ImageBody) {
        this.imageData = imageData
        uploadResult.execute()
    }

    fun sendBasePaymentData(paymentData: () -> PaymentData) {
        this.basePaymentData = paymentData
        basePaymentDataResult.execute()
    }

    fun sendExtraPaymentData(paymentData: () -> PaymentData) {
        this.extraPaymentData = paymentData
        extraPaymentDataResult.execute()
    }

    fun changeEmail(newEmail: String) {
        this.newEmail = newEmail
        changeEmailResult.execute()
    }

    fun requestNewCheckCode(orderId: String) {
        this.orderId = orderId
        newCheckCode.execute()
    }

    fun checkCode(orderId: String, code: String) {
        this.orderId = orderId
        this.code = code
        checkCode.execute()
    }
}
