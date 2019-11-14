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

package com.cexdirect.lib.order.receipt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.AmountData
import com.cexdirect.lib.buy.CalcActivity.Companion.startBuyActivity
import com.cexdirect.lib.databinding.FragmentReceiptBinding
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.order.BaseOrderFragment
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast

class ReceiptFragment : BaseOrderFragment() {

    private lateinit var binding: FragmentReceiptBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        DataBindingUtil.inflate<FragmentReceiptBinding>(
            inflater,
            R.layout.fragment_receipt,
            container,
            false
        ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = model

        model.apply {
            statusWatcher.setScreenChanged()
            subscribeToOrderInfo().observe(
                viewLifecycleOwner, socketObserver(
                onOk = { model.updatePaymentInfo(it) },
                onFail = { purchaseFailed(it.message, extractAmounts()) }
            ))
            buyMoreClick.observe(viewLifecycleOwner, Observer {
                context!!.startBuyActivity(
                    AmountData(
                        model.orderAmounts.selectedFiatAmount,
                        model.orderAmounts.selectedFiatCurrency,
                        model.orderAmounts.selectedCryptoCurrency
                    )
                )
                finish()
            })
            txIdCopyEvent.observe(viewLifecycleOwner, Observer { txId ->
                this@ReceiptFragment.copyTxId(txId)
            })
            txIdOpenEvent.observe(viewLifecycleOwner, Observer { openTxDetailsInBrowser(it) })
        }
    }

    private fun copyTxId(txId: String?) {
        if (!txId.isNullOrBlank()) {
            (context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                ClipData.newPlainText(getString(R.string.cexd_tx_label), txId)
            toast(getString(R.string.cexd_tx_id_copied))
        }
    }

    private fun openTxDetailsInBrowser(url: String) {
        Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }.let { it.resolveActivity(requireContext().packageManager)?.run { startActivity(it) } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        model.stopSubscriptions()
    }
}
