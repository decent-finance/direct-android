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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.FragmentLocationNotSupportedBinding
import com.cexdirect.lib.order.OrderActivity
import com.mcxiaoke.koi.ext.finish

class LocationNotSupportedFragment : BaseErrorFragment() {

    private lateinit var binding: FragmentLocationNotSupportedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<FragmentLocationNotSupportedBinding>(
        inflater,
        R.layout.fragment_location_not_supported,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.goBack.observe(viewLifecycleOwner, Observer {
            Intent(requireActivity(), OrderActivity::class.java).apply {
                val intent = requireActivity().intent
                putExtra("email", intent.getStringExtra("email"))
                putExtra("countryCode", intent.getStringExtra("code"))
                model.extractAmounts().let {
                    putExtra("cryptoAmount", it.cryptoAmount)
                    putExtra("crypto", it.cryptoCurrency)
                    putExtra("fiatAmount", it.fiatAmount)
                    putExtra("fiat", it.fiatCurrency)
                }
            }.let { startActivity(it) }
            finish()
        })
        binding.model = model
    }
}
