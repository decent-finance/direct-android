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

package com.cexdirect.lib.verification.confirmation

import androidx.databinding.ObservableField
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.models.ChangeEmailRequest
import com.cexdirect.lib.network.models.CheckCodeData
import com.cexdirect.lib.network.models._3Ds
import com.cexdirect.lib.network.ws.Messenger

class PaymentConfirmationFragmentViewModel(
    private val orderApi: OrderApi,
    val emailChangedEvent: StringLiveEvent,
    private val messenger: Messenger,
    dispatcherProvider: CoroutineDispatcherProvider
) : BaseObservableViewModel(dispatcherProvider) {

    val confirmationCode = ObservableField("")
    val userEmail = ObservableField("")

    val confirmationStep = ObservableField<ConfirmationStep>()

    val resendCodeEvent = VoidLiveEvent()
    val editEmailEvent = VoidLiveEvent()

    val _3dsData = _3dsData()
    var orderId: String = Direct.pendingOrderId

    val changeEmail = Transformations.switchMap(emailChangedEvent) {
        orderApi.changeEmail(this) { ChangeEmailRequest(newEmail = it) }.apply { execute() }
    }!!

    val resendCheckCode =
        orderApi.resendCheckCode(this@PaymentConfirmationFragmentViewModel, orderId)

    fun resendCheckCode() {
        resendCodeEvent.call()
    }

    fun editEmail() {
        editEmailEvent.call()
    }

    val checkCode = orderApi.checkCode(this) {
        CheckCodeData(orderId, confirmationCode.get()!!)
    }

    fun submitCode() {
        checkCode.execute()
    }

    fun requestCheckCode() {
        resendCheckCode.execute()
    }

    fun subscribeToOrderInfo() = messenger.subscribeToOrderInfo()

    private fun unsubscribeFromOrderInfo() {
        messenger.removeOrderInfoSubscription()
    }

    fun updateUserEmail(email: String) {
        userEmail.set(email)
        Direct.userEmail = email
    }

    fun askFor3ds(threeDS: _3Ds) {
        confirmationStep.set(ConfirmationStep.TDS)
        _3dsData.apply {
            _3dsUrl = threeDS.url
            _3dsExtras = threeDS.data
            txId = threeDS.txId
        }
    }

    fun askForEmailConfirmation() {
        confirmationStep.set(ConfirmationStep.EMAIL_CONFIRMATION)
    }

    fun confirmOrder(block: () -> Unit) {
        confirmationStep.set(ConfirmationStep.CONFIRMED)
        unsubscribeFromOrderInfo()
        block.invoke()
    }

    class Factory(
        private val orderApi: OrderApi,
        private val emailChangedEvent: StringLiveEvent,
        private val messenger: Messenger,
        private val dispatcherProvider: CoroutineDispatcherProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            PaymentConfirmationFragmentViewModel(orderApi, emailChangedEvent, messenger, dispatcherProvider) as T
    }
}
