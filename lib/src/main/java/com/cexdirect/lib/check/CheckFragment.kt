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

package com.cexdirect.lib.check

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.CheckActivityFactory
import com.cexdirect.lib.buy.BuyFragmentArgs
import com.cexdirect.lib.databinding.ActivityCheckBinding
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Success
import javax.inject.Inject

class CheckFragment : BaseFragment() {

    @field:[Inject CheckActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val model: CheckFragmentViewModel by fragmentViewModelProvider { modelFactory }

    private lateinit var binding: ActivityCheckBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<ActivityCheckBinding>(
        inflater,
        R.layout.activity_check,
        container,
        false
    ).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.directComponent.inject(this)
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
        model.apply {
            checkResult.observe(this@CheckFragment, Observer {
                when (it) {
                    is Loading -> showLoader()
                    is Success -> processPlacementInfo(it.data!!) { showStubScreen() }
                    is Failure -> showStubScreen()
                }
            })
            countryResult.observe(this@CheckFragment, Observer {
                when (it) {
                    is Success -> saveCountriesAndLoadRules(it.data!!)
                    is Failure -> showStubScreen()
                }
            })
            ruleResult.observe(this@CheckFragment, Observer {
                when (it) {
                    is Success -> saveRuleAndLoadNext(it.data!!) { launchDirect() }
                    is Failure -> showStubScreen()
                }
            })
        }.let {
            binding.model = it
            it.checkPlacement()
        }
    }

    private fun launchDirect() {
        hideLoader()
        val args = BuyFragmentArgs(null, null, null).toBundle()
        findNavController().navigate(R.id.action_checkFragment_to_buyFragment, args)
    }

    private fun showStubScreen() {
        hideLoader()
        findNavController().navigate(R.id.action_checkFragment_to_stubFragment)
    }
}
