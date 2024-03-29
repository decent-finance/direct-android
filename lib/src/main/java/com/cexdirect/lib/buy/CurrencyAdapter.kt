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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cexdirect.lib.ClickListener
import com.cexdirect.lib.R
import com.cexdirect.lib.StringLiveEvent
import com.cexdirect.lib.databinding.ItemPairBinding
import com.cexdirect.lib.util.orDefault
import com.cexdirect.lib.util.symbolMap

class CurrencyAdapter(private val clickEvent: StringLiveEvent) :
    RecyclerView.Adapter<PairViewHolder>(),
    ClickListener {

    var items: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedCurrency = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PairViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_pair,
                parent,
                false
            )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PairViewHolder, position: Int) {
        val symbol = items[position]
        holder.bind(this, symbol, symbol == selectedCurrency)
    }

    override fun select(text: String) {
        clickEvent.postValue(text)
    }
}

class PairViewHolder(private val binding: ItemPairBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(listener: ClickListener, symbol: String, selected: Boolean) {
        binding.listener = listener
        binding.symbol = symbol
        binding.fullName = symbolMap[symbol].orDefault().fullName
        binding.selected = selected
    }
}
