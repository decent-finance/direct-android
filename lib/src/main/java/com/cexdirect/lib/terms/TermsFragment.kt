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

package com.cexdirect.lib.terms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.TermsActivityFactory
import com.cexdirect.lib.databinding.FragmentTermsBinding
import javax.inject.Inject

class TermsFragment : BaseFragment() {

    @field:[Inject TermsActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val args by navArgs<TermsFragmentArgs>()

    private val viewModel: TermsFragmentViewModel by fragmentViewModelProvider { modelFactory }

    private lateinit var binding: FragmentTermsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<FragmentTermsBinding>(
        inflater,
        R.layout.fragment_terms,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.directComponent.inject(this)
        viewModel.apply {
            title.set(args.title)
            content.set(args.content)
            okEvent.observe(this@TermsFragment, Observer {
                findNavController().navigate(R.id.action_termsFragment_pop)
            })
        }.let { binding.model = it }
    }
}
