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

package com.cexdirect.lib.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.ErrorActivityFactory
import com.cexdirect.lib.databinding.ActivityErrorBinding
import javax.inject.Inject

class ErrorFragment : BaseFragment() {

    @field:[Inject ErrorActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val model: ErrorActivityViewModel by fragmentViewModelProvider { modelFactory }

    private val args by navArgs<ErrorFragmentArgs>()

    private lateinit var binding: ActivityErrorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<ActivityErrorBinding>(
        inflater,
        R.layout.activity_error,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.directComponent.inject(this)
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
        args.error?.takeIf { it.isNotBlank() }?.let { model.reason.set(it) }
            ?: model.reason.set("Unknown Error")
        model.applyLegalObservers()

        model.userEmail.set(Direct.userEmail)
        model.tryAgainEvent.observe(this, Observer {
            findNavController().navigate(R.id.action_errorFragment_to_checkFragment)
        })

        binding.model = model

        when (ErrorType.valueOf(arguments!!.getString("type")!!)) {
            ErrorType.PURCHASE_FAILED -> childFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, PurchaseFailedDescriptionFragment())
                .commit()
            ErrorType.VERIFICATION_ERROR -> childFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, VerificationErrorDescriptionFragment())
                .commit()
            ErrorType.LOCATION_NOT_SUPPORTED -> childFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, LocationNotSupportedDescriptionFragment())
                .commit()
        }

        model.stopSubscriptions()
    }
}

fun NavController.verificationError(reason: String?) {
    val args = ErrorFragmentArgs(ErrorType.VERIFICATION_ERROR.name, reason).toBundle()
    navigate(R.id.action_global_errorFragment, args)
}

fun NavController.purchaseFailed(reason: String?) {
    val args = ErrorFragmentArgs(ErrorType.PURCHASE_FAILED.name, reason).toBundle()
    navigate(R.id.action_global_errorFragment, args)
}

fun NavController.locationNotSupported() {
    val args = ErrorFragmentArgs(ErrorType.PURCHASE_FAILED.name, null).toBundle()
    navigate(R.id.action_global_errorFragment, args)
}

enum class ErrorType {
    VERIFICATION_ERROR, LOCATION_NOT_SUPPORTED, PURCHASE_FAILED
}
