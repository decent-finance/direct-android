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

package com.cexdirect.lib.verification.identity.country

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.ItemCountryBinding
import com.cexdirect.lib.network.models.CountryData
import com.cexdirect.lib.network.models.emptyCountry

class CountryAdapter(private val clickEvent: CountryClickEvent) :
    RecyclerView.Adapter<CountryViewHolder>(), CountryClickListener {

    var selectedCountry = emptyCountry()

    var items: List<CountryData> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CountryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_country,
                parent,
                false
            )
        )


    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(items[position], this, items[position] == selectedCountry)
    }

    override fun onCountrySelected(country: CountryData) {
        clickEvent.postValue(country)
    }
}

class CountryViewHolder(private val binding: ItemCountryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        country: CountryData,
        listener: CountryAdapter,
        isSelected: Boolean
    ) {
        binding.country = country
        binding.listener = listener
        binding.isSelected = isSelected
    }
}

interface CountryClickListener {
    fun onCountrySelected(country: CountryData)
}
