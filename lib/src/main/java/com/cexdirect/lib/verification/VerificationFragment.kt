/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

package com.cexdirect.lib.verification

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.VerificationActivityFactory
import com.cexdirect.lib.buy.BuyFragmentArgs
import com.cexdirect.lib.databinding.ActivityVerificationBinding
import com.cexdirect.lib.verification.confirmation.PaymentConfirmationFragment
import com.cexdirect.lib.verification.events.StickyViewEvent
import com.cexdirect.lib.verification.identity.IdentityFragment
import com.cexdirect.lib.verification.receipt.ReceiptFragment
import com.mcxiaoke.koi.ext.toast
import javax.inject.Inject

class VerificationFragment : BaseFragment() {

    @field:[Inject VerificationActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    @VisibleForTesting
    val model: VerificationActivityViewModel by fragmentViewModelProvider { modelFactory }

    private val args by navArgs<VerificationFragmentArgs>()

    private val fragments =
        listOf(IdentityFragment(), PaymentConfirmationFragment(), ReceiptFragment())

    private val goToBuyActivityObserver = Observer<Void> {
        val args = BuyFragmentArgs(
            model.orderAmounts.selectedFiatAmount,
            model.orderAmounts.selectedFiatCurrency,
            model.orderAmounts.selectedCryptoCurrency
        ).toBundle()
        findNavController().navigate(R.id.action_global_buyFragment, args)
    }

    private lateinit var binding: ActivityVerificationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<ActivityVerificationBinding>(
        inflater, R.layout.activity_verification, container, false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
        binding.avStepsViewPager.pageMargin = VIEWPAGER_ELEMENT_MARGIN

        model.apply {
            setOrderAmounts(args.crypto, args.cryptoAmount, args.fiat, args.fiatAmount)
            applyLegalObservers()
            stepChangeEvent.observe(this@VerificationFragment, Observer {
                model.proceed()
                replaceFragment(model.currentStep.get() - 1)
            })
            editClickEvent.observe(this@VerificationFragment, goToBuyActivityObserver)
            returnEvent.observe(this@VerificationFragment, goToBuyActivityObserver)
            copyEvent.observe(this@VerificationFragment, Observer { orderId ->
                if (!orderId.isNullOrBlank()) {
                    (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                        ClipData.newPlainText(getString(R.string.cexd_order_id_label), orderId)
                    toast(getString(R.string.cexd_order_id_copied))
                }
            })
        }.let { binding.model = it }

        stickyViewEvent.observe(this, Observer {
            if (it != View.NO_ID) {
                binding.avScroll.initFooterView(it)
            } else {
                binding.avScroll.freeFooter()
                binding.avScroll.requestLayout()
            }
        })
        replaceFragment(0)
    }

    @VisibleForTesting
    fun replaceFragment(position: Int) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.avFragmentFrame, fragments[position])
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Direct.releaseIdentitySubcomponent()
    }

    companion object {
        const val VIEWPAGER_ELEMENT_MARGIN = 10
    }
}
