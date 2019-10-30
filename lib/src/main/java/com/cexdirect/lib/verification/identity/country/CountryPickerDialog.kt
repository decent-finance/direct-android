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

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseBottomSheetDialog
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.DialogCountryPickerBinding
import com.cexdirect.lib.di.annotation.VerificationActivityFactory
import com.cexdirect.lib.network.models.CountryData
import com.cexdirect.lib.verification.VerificationActivityViewModel
import javax.inject.Inject

abstract class BaseCountryPickerDialog : BaseBottomSheetDialog() {

    @field:[Inject VerificationActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    protected val model: VerificationActivityViewModel by viewModelProvider { modelFactory }

    private lateinit var binding: DialogCountryPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<DialogCountryPickerBinding>(
        inflater,
        R.layout.dialog_country_picker,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        binding.title = getTitle()
        model.apply {
            countryAdapter.set(getCountryAdapter().apply {
                items = getCountries()
                selectedCountry = getSelectedCountry()
            })
            currentCountryData = getCountries()
            countryClickEvent.observe(this@BaseCountryPickerDialog, Observer {
                selectCountry(it)
                dismiss()
            })
            countryPickerExitEvent.observe(this@BaseCountryPickerDialog, Observer {
                dismiss()
            })
            toggleSearchEvent.observe(this@BaseCountryPickerDialog, Observer {
                showCountrySearch.set(it)
            })
        }.let {
            binding.model = it
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        model.clearSearch()
    }

    protected abstract fun getTitle(): String

    protected abstract fun getCountryAdapter(): CountryAdapter

    protected abstract fun selectCountry(countryData: CountryData)

    protected abstract fun getCountries(): List<CountryData>

    protected abstract fun getSelectedCountry(): CountryData
}

class CountryPickerDialog : BaseCountryPickerDialog() {

    override fun getTitle() = getString(R.string.cexd_choose_country)

    override fun getCountryAdapter() = CountryAdapter(model.countryClickEvent)

    override fun selectCountry(countryData: CountryData) {
        model.userCountry.selectedCountry = countryData
    }

    override fun getCountries() = Direct.countries.sortByName()

    override fun getSelectedCountry() = model.userCountry.selectedCountry
}

class StatePickerDialog : BaseCountryPickerDialog() {

    override fun getTitle() = getString(R.string.cexd_choose_state)

    override fun getCountryAdapter() = CountryAdapter(model.countryClickEvent)

    override fun selectCountry(countryData: CountryData) {
        model.userCountry.selectedState = countryData
    }

    override fun getCountries() =
        Direct.countries.find { it.states != null }?.states?.sortByName() ?: emptyList()

    override fun getSelectedCountry() = model.userCountry.selectedState
}

fun List<CountryData>.sortByName() = this.sortedBy { it.name }
