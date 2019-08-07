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

package com.cexdirect.lib.buy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringDef
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.BuyActivityFactory
import com.cexdirect.lib.databinding.DialogPairSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class PairSelectionBottomSheetDialog : BottomSheetDialogFragment() {

    @field:[Inject BuyActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val viewModel by activityViewModelProvider<BuyActivityViewModel> { modelFactory }

    private lateinit var binding: DialogPairSelectorBinding
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.directComponent.inject(this)
        type = arguments!!.getString(KEY_TYPE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPairSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dpsPairs.apply {
            val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            decoration.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.divider, context.theme)!!)
            addItemDecoration(decoration)
        }
        binding.model = viewModel

        viewModel.currencyClickEvent.observe(this, Observer {
            when (type) {
                TYPE_BASE -> viewModel.setSelectedCryptoCurrency(it)
                TYPE_QUOTE -> viewModel.setSelectedFiatCurrency(it)
            }
            dismiss()
        })
        viewModel.closeSelectorEvent.observe(this, Observer { dismiss() })
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    private inline fun <reified VM : BaseObservableViewModel> activityViewModelProvider(crossinline factory: () -> ViewModelProvider.Factory) =
        lazy { ViewModelProviders.of(activity!!, factory()).get(VM::class.java) }

    companion object {
        const val KEY_TYPE = "typ"
        const val TYPE_BASE = "base"
        const val TYPE_QUOTE = "quote"

        @JvmStatic
        fun create(@Type type: String) =
            PairSelectionBottomSheetDialog().apply {
                val args = Bundle().apply { putString(KEY_TYPE, type) }
                arguments = args
            }
    }
}

@Retention(AnnotationRetention.SOURCE)
@StringDef(PairSelectionBottomSheetDialog.TYPE_BASE, PairSelectionBottomSheetDialog.TYPE_QUOTE)
annotation class Type
