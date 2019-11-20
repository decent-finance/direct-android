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

package com.cexdirect.lib.network

import com.cexdirect.lib.DispatcherRegistry
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@ExperimentalCoroutinesApi
@OpenForTesting
class OrderFlow(private val service: OrderService) {

    fun createNewOrder(data: NewOrderData) =
        flow { emit(service.createNewOrder(NewOrderBody(data))) }
            .flowOn(DispatcherRegistry.io)

    fun checkCode(data: CheckCodeData) =
        flow { emit(service.checkCode(CheckCodeBody(data))) }
            .flowOn(DispatcherRegistry.io)

    fun resendCheckCode(orderId: String) =
        flow {
            emit(service.resendCheckCode(ResendCheckCodeBody(ResendCheckCodeData(orderId = orderId))))
        }.flowOn(DispatcherRegistry.io)

    fun checkOrderInfo() =
        flow {
            emit(service.checkOrderInfo(OrderInfoBody()))
        }.flowOn(DispatcherRegistry.io)

    fun sendPaymentData(paymentData: PaymentData) =
        flow { emit(service.sendPaymentData(PaymentBody(paymentData))) }
            .flowOn(DispatcherRegistry.io)

    fun updatePaymentData(paymentData: PaymentData) =
        flow { emit(service.updatePaymentData(PaymentBody(paymentData))) }
            .flowOn(DispatcherRegistry.io)

    fun uploadImage(block: () -> ImageBody) =
        flow { emit(service.uploadImage(block.invoke())) }
            .flowOn(DispatcherRegistry.io)

    fun getVerificationKey(data: PublicKeyData) =
        flow { emit(service.getVerificationPublicKey(PublicKeyBody(data))) }
            .flowOn(DispatcherRegistry.io)

    fun getProcessingKey(data: PublicKeyData) =
        flow { emit(service.getProcessingPublicKey(PublicKeyBody(data))) }
            .flowOn(DispatcherRegistry.io)

    fun changeEmail(request: ChangeEmailRequest) =
        flow { emit(service.changeEmail(ChangeEmailBody(request))) }
            .flowOn(DispatcherRegistry.io)

    fun sendToVerification(block: () -> VerificationData) =
        flow { emit(service.sendToVerification(VerificationBody(data = block.invoke()))) }
            .flowOn(DispatcherRegistry.io)

    fun sendToProcessing(block: () -> VerificationData) =
        flow { emit(service.sendToProcessing(VerificationBody(data = block.invoke()))) }
            .flowOn(DispatcherRegistry.io)
}
