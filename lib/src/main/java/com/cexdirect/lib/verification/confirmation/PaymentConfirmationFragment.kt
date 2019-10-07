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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.FragmentPaymentConfirmationBinding
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.error.verificationError
import com.cexdirect.lib.network.webview.Client
import com.cexdirect.lib.network.ws.CODE_BAD_REQUEST
import com.cexdirect.lib.verification.BaseVerificationFragment
import com.cexdirect.lib.verification.events.StickyViewEvent
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import javax.inject.Inject

class PaymentConfirmationFragment : BaseVerificationFragment() {

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    @Inject
    lateinit var webViewClient: Client

    private lateinit var binding: FragmentPaymentConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        FragmentPaymentConfirmationBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        setup3dsWebView()

        model.apply {
            statusWatcher.setScreenChanged()
            subscribeToOrderInfo().observe(this@PaymentConfirmationFragment, socketObserver(
                onOk = {
                    updateConfirmationStatus(it!!) {
                        context!!.verificationError("Rejected")
                        finish()
                    }
                },
                onFail = { purchaseFailed(it.message) }
            ))
            resendCodeEvent.observe(this@PaymentConfirmationFragment, Observer {
                requestCheckCode()
            })
            editEmailEvent.observe(this@PaymentConfirmationFragment, Observer {
                ChangeEmailDialog().show(childFragmentManager, "changeEmail")
            })
            checkCode.observe(this@PaymentConfirmationFragment, restObserver(
                onOk = { /* Don't do anything here, because order status will be updated via WS */ },
                onFail = {
                    if (it.code == CODE_BAD_REQUEST) {
                        toast(R.string.cexd_wrong_code)
                    } else {
                        purchaseFailed(it.message)
                    }
                }
            ))
            resendCheckCode.observe(this@PaymentConfirmationFragment, restObserver(
                onOk = { toast(R.string.cexd_check_mail) },
                onFail = { purchaseFailed(it.message) }
            ))
            changeEmail.observe(this@PaymentConfirmationFragment, restObserver(
                onOk = {
                    updateUserEmail(emailChangedEvent.value ?: it!!)
                    toast(R.string.cexd_email_updated)
                },
                onFail = { purchaseFailed(it.message) }
            ))
        }.let { binding.model = it }

        stickyViewEvent.value = R.id.fpcSubmit
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setup3dsWebView() {
        binding.fpc3ds.apply {
            webViewClient = this@PaymentConfirmationFragment.webViewClient
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = false
                domStorageEnabled = true
            }
        }
    }
}
