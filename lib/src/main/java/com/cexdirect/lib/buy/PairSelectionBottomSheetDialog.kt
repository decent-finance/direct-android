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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.cexdirect.lib.BaseBottomSheetDialog
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.DialogPairSelectorBinding
import com.cexdirect.lib.di.annotation.BuyActivityFactory
import javax.inject.Inject

class PairSelectionBottomSheetDialog : BaseBottomSheetDialog() {

    @field:[Inject BuyActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val viewModel by viewModelProvider<BuyActivityViewModel> { modelFactory }

    private lateinit var binding: DialogPairSelectorBinding
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.directComponent.inject(this)
        type = arguments!!.getString(KEY_TYPE)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<DialogPairSelectorBinding>(
        inflater,
        R.layout.dialog_pair_selector,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            dpsPairs.apply {
                val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                decoration.setDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.divider,
                        context.theme
                    )!!
                )
                addItemDecoration(decoration)
            }
            model = viewModel
            title = when (type) {
                TYPE_BASE -> getString(R.string.cexd_choose_crypto)
                TYPE_QUOTE -> getString(R.string.cexd_choose_currency)
                else -> error("Illegal type")
            }
        }

        viewModel.apply {
            currencyClickEvent.observe(this@PairSelectionBottomSheetDialog, Observer {
                when (type) {
                    TYPE_BASE -> viewModel.setSelectedCryptoCurrency(it)
                    TYPE_QUOTE -> viewModel.setSelectedFiatCurrency(it)
                }
                dismiss()
            })
            closeSelectorEvent.observe(this@PairSelectionBottomSheetDialog, Observer { dismiss() })
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

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
