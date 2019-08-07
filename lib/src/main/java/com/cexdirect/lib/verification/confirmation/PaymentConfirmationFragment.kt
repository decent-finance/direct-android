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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.PaymentConfirmationFragmentFactory
import com.cexdirect.lib._network.Failure
import com.cexdirect.lib._network.Loading
import com.cexdirect.lib._network.Success
import com.cexdirect.lib._network.models.OrderStatus
import com.cexdirect.lib._network.webview.Client
import com.cexdirect.lib.databinding.FragmentPaymentConfirmationBinding
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.error.verificationError
import com.cexdirect.lib.verification.BaseVerificationFragment
import com.cexdirect.lib.verification.StickyViewEvent
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class PaymentConfirmationFragment : BaseVerificationFragment() {

    @field:[Inject PaymentConfirmationFragmentFactory]
    lateinit var fragmentFactory: ViewModelProvider.Factory

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    @Inject
    lateinit var currentOrderStatus: AtomicReference<OrderStatus>

    @Inject
    lateinit var webViewClient: Client

    private lateinit var binding: FragmentPaymentConfirmationBinding

    private val fragmentModel by fragmentViewModelProvider<PaymentConfirmationFragmentViewModel> { fragmentFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentPaymentConfirmationBinding.inflate(inflater, container, false).apply {
                binding = this
            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        binding.model = fragmentModel

        binding.fpc3ds.webViewClient = webViewClient

        fragmentModel.apply {
            userEmail.set(Direct.userEmail)
            subscribeToOrderInfo().observe(this@PaymentConfirmationFragment, Observer {
                when (it) {
                    is Success -> {
                        val data = it.data!!
                        when (data.orderStatus) {
                            OrderStatus.PSS_3DS_REQUIRED -> {
                                if (currentOrderStatus.get() != OrderStatus.PSS_3DS_REQUIRED) {
                                    currentOrderStatus.set(OrderStatus.PSS_3DS_REQUIRED)
                                    fragmentModel.askFor3ds(data.threeDS!!)
                                }
                            }
                            OrderStatus.WAITING_FOR_CONFIRMATION -> {
                                if (currentOrderStatus.get() != OrderStatus.WAITING_FOR_CONFIRMATION) {
                                    currentOrderStatus.set(OrderStatus.WAITING_FOR_CONFIRMATION)
                                    fragmentModel.askForEmailConfirmation()
                                }
                            }
                            OrderStatus.COMPLETE -> {
                                if (currentOrderStatus.get() != OrderStatus.COMPLETE) {
                                    currentOrderStatus.set(OrderStatus.COMPLETE)
                                    fragmentModel.confirmOrder { model.nextStep() }
                                }
                            }
                            OrderStatus.REJECTED -> {
                                if (currentOrderStatus.get() != OrderStatus.REJECTED) {
                                    currentOrderStatus.set(OrderStatus.REJECTED)
                                    context!!.verificationError("Rejected")
                                    finish()
                                }
                            }
                            else -> {
                                // do nothing
                            }
                        }
                    }
                    is Failure -> {
                        purchaseFailed(it.message)
                    }
                }
            })
            checkCode.observe(this@PaymentConfirmationFragment, Observer {
                when (it) {
                    is Loading -> showLoader()
                    is Success -> {
                        hideLoader()
                        // Don't do anything else here, because order status will be updated via WS
                    }
                    is Failure -> {
                        hideLoader()
                        if (it.code == 400) {
                            toast(R.string.cexd_wrong_code)
                        } else {
                            purchaseFailed(it.message)
                        }
                    }
                }
            })
            resendCodeEvent.observe(this@PaymentConfirmationFragment, Observer {
                requestCheckCode()
            })
            resendCheckCode.observe(this@PaymentConfirmationFragment, Observer {
                when (it) {
                    is Loading -> showLoader()
                    is Success -> {
                        hideLoader()
                        toast(R.string.cexd_check_mail)
                    }
                    is Failure -> {
                        hideLoader()
                        purchaseFailed(it.message)
                    }
                }
            })
            editEmailEvent.observe(this@PaymentConfirmationFragment, Observer {
                ChangeEmailDialog().show(childFragmentManager, "changeEmail")
            })
            changeEmail.observe(this@PaymentConfirmationFragment, Observer {
                when (it) {
                    is Loading -> {
                        showLoader()
                    }
                    is Failure -> {
                        hideLoader()
                        purchaseFailed(it.message)
                    }
                    is Success -> {
                        hideLoader()
                        fragmentModel.updateUserEmail(fragmentModel.emailChangedEvent.value
                                ?: it.data!!)
                        toast(R.string.cexd_email_updated)
                    }
                }
            })
        }

        stickyViewEvent.value = R.id.fpcSubmit
    }
}
