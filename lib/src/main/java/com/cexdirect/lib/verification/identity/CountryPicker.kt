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

package com.cexdirect.lib.verification.identity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cexdirect.lib.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mukesh.countrypicker.Country
import java.util.*

class CCountryPicker : BottomSheetDialogFragment() {

    private val countriesList = ArrayList<Country>()
    private var selectedCountriesList: MutableList<Country> = ArrayList()
    private var countryListAdapter: CountryListAdapter? = null
    private var listener: CCountryPickerListener? = null

    init {
//        setCountriesList(Country.getAllCountries())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_country_picker, container, false)!!

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        selectedCountriesList.addAll(this.countriesList)
//        countryListAdapter = CountryListAdapter(context!!, this.selectedCountriesList)
//
//        fcpList.apply {
//            this.adapter = countryListAdapter
//            onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//                if (this@CCountryPicker.listener != null) {
//                    val country = this@CCountryPicker.selectedCountriesList[position]
//                    this@CCountryPicker.listener!!.onSelectCountry(country.name, country.code, country.dialCode, country.flag)
//                }
//            }
//        }
//
//        fcpSearch.setOnClickListener {
//            fcpTitleLayout.visibility = View.GONE
//            fcpSearchLayout.visibility = View.VISIBLE
//        }
//
//        fcpCloseSearch.setOnClickListener {
//            fcpSearchLayout.visibility = View.GONE
//            fcpTitleLayout.visibility = View.VISIBLE
//            search("")
//        }
//
//        fcpQuery.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                search(s.toString())
//            }
//        })
//
//        fcpCancel.setOnClickListener {
//            dismiss()
//        }
//    }

    override fun onStart() {
        super.onStart()
        view?.apply {
            post {
                val parent = parent as View
                val params = parent.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
                val height = this.measuredHeight - (72 * resources.displayMetrics.density).toInt()
                (params.behavior as com.google.android.material.bottomsheet.BottomSheetBehavior).peekHeight = height
                params.height = height
                parent.layoutParams = params
            }
        }
    }

    fun setListener(listener: CCountryPickerListener) {
        this.listener = listener
    }

    private fun search(text: String) {
        this.selectedCountriesList.clear()
        val var2 = this.countriesList.iterator()

        while (var2.hasNext()) {
            val country = var2.next()
            if (country.name.toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
                this.selectedCountriesList.add(country)
            }
        }

        this.countryListAdapter!!.notifyDataSetChanged()
    }

    private fun getCoordinatorLayoutWrapper(view: View): androidx.coordinatorlayout.widget.CoordinatorLayout {
        view.parent.takeIf { it is androidx.coordinatorlayout.widget.CoordinatorLayout }?.let {
            return it as androidx.coordinatorlayout.widget.CoordinatorLayout
        }

        return getCoordinatorLayoutWrapper(view.parent as View)
    }

    fun setCountriesList(newCountries: List<Country>) {
        this.countriesList.clear()
        this.countriesList.addAll(newCountries)
    }
}

interface CCountryPickerListener {
    fun onSelectCountry(name: String, code: String, dialCode: String, flagId: Int)
}

class CountryListAdapter(private val mContext: Context, private var countries: List<Country>) : BaseAdapter() {
    internal var inflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return this.countries.size
    }

    override fun getItem(position: Int): Country {
        return countries[position]
    }

    override fun getItemId(position: Int): Long {
        return countries[position].flag.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val country = getItem(position)
        val v = view ?: this.inflater.inflate(R.layout.country_row, parent, false)

        val cell = Cell.from(v)
        cell.name.text = country.name
        country.loadFlagByCode(this.mContext)
        if (country.flag != -1) {
            cell.flag.setImageResource(country.flag)
        }
        cell.dialCode.text = country.dialCode

        return v
    }

    internal class Cell constructor(
        var name: TextView,
        var flag: ImageView,
        var dialCode: TextView
    ) {
        companion object {
            @JvmStatic
            fun from(view: View): Cell {
                return when {
                    view.tag == null -> {
                        val cell = Cell(
                            view.findViewById(R.id.cr_name),
                            view.findViewById(R.id.cr_flag),
                            view.findViewById(R.id.cr_dial_code)
                        )
                        view.tag = cell
                        cell
                    }
                    else -> view.tag as Cell
                }
            }
        }
    }
}
