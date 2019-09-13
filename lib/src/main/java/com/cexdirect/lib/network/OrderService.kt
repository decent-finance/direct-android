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

import com.cexdirect.lib.network.models.*
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

@Suppress("DeferredIsResult")
interface OrderService {

    @PUT("api/v1/orders/new")
    fun createNewOrder(@Body body: NewOrderBody): Deferred<OrderDataResponse>

    @POST("api/v1/orders/send2processing")
    fun sendToProcessing(@Body body: ProcessingBody): Deferred<ApiResponse<Void>>

    @POST("api/v1/orders/check")
    fun checkCode(@Body body: CheckCodeBody): Deferred<OrderDataResponse>

    @POST("api/v1/orders/resend-check-code")
    fun resendCheckCode(@Body body: ResendCheckCodeBody): Deferred<OrderDataResponse>

    @POST("api/v1/orders/info")
    fun checkOrderInfo(@Body body: OrderInfoBody): Deferred<OrderInfoResponse>

    @PUT("api/v1/orders/payment")
    fun sendPaymentData(@Body body: PaymentBody): Deferred<OrderInfoResponse>

    @POST("api/v1/orders/payment")
    fun updatePaymentData(@Body body: PaymentBody): Deferred<OrderInfoResponse>

    @PUT("api/v1/orders/image")
    fun uploadImage(@Body body: ImageBody): Deferred<ApiResponse<Void>>

    @POST("api/v1/orders/crypto/verification")
    fun getVerificationPublicKey(@Body body: PublicKeyBody): Deferred<PublicKeyResponse>

    @POST("api/v1/orders/crypto/processing")
    fun getProcessingPublicKey(@Body body: PublicKeyBody): Deferred<PublicKeyResponse>

    @PUT("api/v1/orders/email")
    fun changeEmail(@Body body: ChangeEmailBody): Deferred<ChangeEmailResponse>

    @POST("api/v1/orders/send2verification")
    fun sendToVerification(@Body body: VerificationBody): Deferred<ApiResponse<Void>>

    @POST("api/v1/orders/send2processing")
    fun sendToProcessing(@Body body: VerificationBody): Deferred<ApiResponse<Void>>
}
