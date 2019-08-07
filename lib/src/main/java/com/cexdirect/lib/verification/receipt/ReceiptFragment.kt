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

package com.cexdirect.lib.verification.receipt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.ReceiptFragmentFactory
import com.cexdirect.lib._network.Success
import com.cexdirect.lib._network.models.OrderStatus
import com.cexdirect.lib.buy.startBuyActivity
import com.cexdirect.lib.databinding.FragmentReceiptBinding
import com.cexdirect.lib.verification.BaseVerificationFragment
import com.cexdirect.lib.verification.StickyViewEvent
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import javax.inject.Inject

class ReceiptFragment : BaseVerificationFragment() {

    @field:[Inject ReceiptFragmentFactory]
    lateinit var fragmentModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    private val fragmentModel by fragmentViewModelProvider<ReceiptFragmentViewModel> { fragmentModelFactory }

    private lateinit var binding: FragmentReceiptBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FragmentReceiptBinding.inflate(inflater, container, false).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        binding.model = fragmentModel

        fragmentModel.apply {
            subscribeToOrderInfo().observe(this@ReceiptFragment, Observer {
                if (it is Success) {
                    val data = it.data!!
                    when (data.orderStatus) {
                        OrderStatus.COMPLETE -> {
                            fragmentModel.updatePaymentInfo(data.paymentInfo!!)
                        }
                        OrderStatus.FINISHED -> {
                            fragmentModel.updatePaymentInfo(data.paymentInfo!!)
                            Direct.directComponent.socket().stop()
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }
            })
            buyMoreEvent.observe(this@ReceiptFragment, Observer {
                context!!.startBuyActivity()
                finish()
            })
            txIdCopyEvent.observe(this@ReceiptFragment, Observer { txId ->
                if (!txId.isNullOrBlank()) {
                    (context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                            ClipData.newPlainText(getString(R.string.cexd_order_id_label), txId)
                    toast(getString(R.string.cexd_order_id_copied))
                }
            })
        }

        stickyViewEvent.value = R.id.ffBuyMore
    }

    override fun onStop() {
        super.onStop()
        if (activity!!.isFinishing) {
            Direct.directComponent.socket().stop()
        }
    }
}
