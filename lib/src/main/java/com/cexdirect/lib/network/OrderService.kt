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
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

@Suppress("DeferredIsResult")
interface OrderService {

    @PUT("api/v1/orders/new")
    suspend fun createNewOrder(@Body body: NewOrderBody): OrderDataResponse

    @POST("api/v1/orders/send2processing")
    suspend fun sendToProcessing(@Body body: ProcessingBody): ApiResponse<Void>

    @POST("api/v1/orders/check")
    suspend fun checkCode(@Body body: CheckCodeBody): OrderDataResponse

    @POST("api/v1/orders/resend-check-code")
    suspend fun resendCheckCode(@Body body: ResendCheckCodeBody): OrderDataResponse

    @POST("api/v1/orders/info")
    suspend fun checkOrderInfo(@Body body: OrderInfoBody): OrderInfoResponse

    @PUT("api/v1/orders/payment")
    suspend fun sendPaymentData(@Body body: PaymentBody): OrderInfoResponse

    @POST("api/v1/orders/payment")
    suspend fun updatePaymentData(@Body body: PaymentBody): OrderInfoResponse

    @PUT("api/v1/orders/image")
    suspend fun uploadImage(@Body body: ImageBody): ApiResponse<Void>

    @POST("api/v1/orders/crypto/verification")
    suspend fun getVerificationPublicKey(@Body body: PublicKeyBody): PublicKeyResponse

    @POST("api/v1/orders/crypto/processing")
    suspend fun getProcessingPublicKey(@Body body: PublicKeyBody): PublicKeyResponse

    @PUT("api/v1/orders/email")
    suspend fun changeEmail(@Body body: ChangeEmailBody): ChangeEmailResponse

    @POST("api/v1/orders/send2verification")
    suspend fun sendToVerification(@Body body: VerificationBody): ApiResponse<Void>

    @POST("api/v1/orders/send2processing")
    suspend fun sendToProcessing(@Body body: VerificationBody): ApiResponse<Void>
}
