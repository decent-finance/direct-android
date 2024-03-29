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

package com.cexdirect.lib.order.confirmation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.FragmentPaymentConfirmationBinding
import com.cexdirect.lib.error.paymentRejected
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.webview.Client
import com.cexdirect.lib.network.ws.CODE_BAD_REQUEST
import com.cexdirect.lib.order.BaseOrderFragment
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import javax.inject.Inject

class PaymentConfirmationFragment : BaseOrderFragment() {

    @Inject
    lateinit var webViewClient: Client

    private lateinit var binding: FragmentPaymentConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<FragmentPaymentConfirmationBinding>(
        inflater,
        R.layout.fragment_payment_confirmation,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        setup3dsWebView()

        model.apply {
            statusWatcher.setScreenChanged()
            subscribeToOrderInfo().observe(viewLifecycleOwner, socketObserver(
                onOk = {
                    updateConfirmationStatus(
                        it,
                        { showLoader() },
                        { hideLoader() },
                        {
                            // TODO: for now, only REJECTED is possible here
                            requireContext().paymentRejected(it, extractAmounts(), extractRefundExtras())
                            finish()
                        }
                    )
                },
                onFail = { purchaseFailed(it.message, extractAmounts()) }
            ))
            editEmailEvent.observe(viewLifecycleOwner, Observer {
                ChangeEmailDialog().show(childFragmentManager, "changeEmail")
            })
            checkCodeRequest.observe(viewLifecycleOwner, requestObserver(
                onOk = { /* Don't do anything here, because order status will be updated via WS */ },
                onFail = {
                    if (it.code == CODE_BAD_REQUEST) {
                        setCodeInvalid()
                        hideLoader()
                    } else {
                        purchaseFailed(it.message, extractAmounts())
                    }
                },
                final = {}
            ))
            changeCheckCodeRequest.observe(viewLifecycleOwner, requestObserver(
                onOk = {
                    toast(R.string.cexd_check_mail)
                    restartResendTimer()
                },
                onFail = { purchaseFailed(it.message, extractAmounts()) }
            ))
            changeEmailRequest.observe(viewLifecycleOwner, requestObserver(
                onOk = {
                    updateUserEmail(emailChangedEvent.value ?: it!!)
                    toast(R.string.cexd_email_updated)
                    restartResendTimer()
                },
                onFail = { purchaseFailed(it.message, extractAmounts()) }
            ))
        }.let { binding.model = it }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setup3dsWebView() {
        binding.fpc3ds.apply {
            webViewClient = this@PaymentConfirmationFragment.webViewClient
            webChromeClient = WebChromeClient()
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = false
                domStorageEnabled = true
            }
        }
    }
}
