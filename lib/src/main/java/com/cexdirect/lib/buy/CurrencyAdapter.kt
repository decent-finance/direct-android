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
import androidx.recyclerview.widget.RecyclerView
import com.cexdirect.lib._util.orDefault
import com.cexdirect.lib._util.symbolMap
import com.cexdirect.lib.databinding.ItemPairBinding

class CurrencyAdapter(private val clickEvent: ClickEvent) : RecyclerView.Adapter<PairViewHolder>(),
    ClickListener {

    var items: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PairViewHolder(ItemPairBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PairViewHolder, position: Int) {
        holder.bind(this, items[position])
    }

    override fun select(text: String) {
        clickEvent.postValue(text)
    }
}

class PairViewHolder(private val binding: ItemPairBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(listener: ClickListener, symbol: String) {
        binding.listener = listener
        binding.symbol = symbol
        binding.fullName = symbolMap[symbol].orDefault().fullName
    }
}
