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

package com.cexdirect.lib._network

import com.cexdirect.lib.ExecutableLiveData
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib._network.models.*
import kotlinx.coroutines.CoroutineScope

@OpenForTesting
class OrderApi(private val service: OrderService) {

    fun createNewOrder(
        scope: CoroutineScope,
        block: () -> NewOrderData
    ) = ExecutableLiveData(scope) {
        service.createNewOrder(NewOrderBody(block.invoke()))
    }

    fun checkCode(scope: CoroutineScope, block: () -> CheckCodeData) =
        ExecutableLiveData(scope) {
            service.checkCode(CheckCodeBody(block.invoke()))
        }

    fun resendCheckCode(scope: CoroutineScope, orderId: String) =
        ExecutableLiveData(scope) {
            service.resendCheckCode(ResendCheckCodeBody(ResendCheckCodeData(orderId = orderId)))
        }

    fun checkOrderInfo(scope: CoroutineScope) =
        ExecutableLiveData(scope) {
            service.checkOrderInfo(OrderInfoBody())
        }

    fun sendPaymentData(scope: CoroutineScope, block: () -> PaymentData) =
        ExecutableLiveData(scope) {
            service.sendPaymentData(PaymentBody(block.invoke()))
        }

    fun updatePaymentData(scope: CoroutineScope, block: () -> PaymentData) =
        ExecutableLiveData(scope) {
            service.updatePaymentData(PaymentBody(block.invoke()))
        }

    fun uploadImage(scope: CoroutineScope, block: () -> ImageBody) =
        ExecutableLiveData(scope) {
            service.uploadImage(block.invoke())
        }

    fun getVerificationKey(scope: CoroutineScope, block: () -> PublicKeyData) =
        ExecutableLiveData(scope) {
            service.getVerificationPublicKey(PublicKeyBody(block.invoke()))
        }

    fun getProcessingKey(scope: CoroutineScope, block: () -> PublicKeyData) =
        ExecutableLiveData(scope) {
            service.getProcessingPublicKey(PublicKeyBody(block.invoke()))
        }

    fun changeEmail(scope: CoroutineScope, block: () -> ChangeEmailRequest) =
        ExecutableLiveData(scope) {
            service.changeEmail(ChangeEmailBody(block.invoke()))
        }

    fun sendToVerification(scope: CoroutineScope, block: () -> VerificationData) =
        ExecutableLiveData(scope) {
            service.sendToVerification(VerificationBody(data = block.invoke()))
        }

    fun sendToProcessing(scope: CoroutineScope, block: () -> VerificationData) =
        ExecutableLiveData(scope) {
            service.sendToProcessing(VerificationBody(data = block.invoke()))
        }
}
