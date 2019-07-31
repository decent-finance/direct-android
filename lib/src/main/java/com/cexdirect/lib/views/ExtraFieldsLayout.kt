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

package com.cexdirect.lib.views

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.*
import com.cexdirect.lib._network.models.Additional
import com.cexdirect.lib.databinding.LayoutExtraFieldBinding
import com.google.android.material.textfield.TextInputEditText

class ExtraFieldsLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var additional: Map<String, Additional> = emptyMap()
        set(value) {
            field = value
            removeAllViews()
            updateLayout()
        }

    var input = ObservableArrayMap<String, String>()

    private fun updateLayout() {
        val inflater = LayoutInflater.from(context)
        additional.forEach { entry ->
            if (entry.value.req) {
                val binding = LayoutExtraFieldBinding.inflate(inflater, this, false)
                binding.apply {
                    entry.value.value?.takeIf { it.isNotBlank() }?.let { this@ExtraFieldsLayout.input[entry.key] = it }
                    editable = entry.value.editable
                    key = entry.key
                    input = this@ExtraFieldsLayout.input
                    rule = fieldRules[entry.key]
                }
                addView(binding.root)
            }
        }
    }
}

@InverseBindingAdapter(attribute = "input", event = "inputAttrChanged")
fun ExtraFieldsLayout.retrieveInput() = this.input

@BindingAdapter("inputAttrChanged")
fun ExtraFieldsLayout.applyInputChangeListener(listener: InverseBindingListener) {
    this.input.addOnMapChangedCallback(
        object : ObservableMap.OnMapChangedCallback<ObservableMap<String?, String?>, String?, String?>() {
            override fun onMapChanged(sender: ObservableMap<String?, String?>, key: String?) {
                listener.onChange()
            }
        }
    )
}

@BindingAdapter("input")
fun ExtraFieldsLayout.applyInput(input: ObservableArrayMap<String, String>) {
    this.input = input
}

@BindingAdapter("additionalFields")
fun ExtraFieldsLayout.applyAdditionalFields(additional: Map<String, Additional>) {
    this.additional = additional
}

val fieldRules = HashMap<String, ExtraFieldRule>().apply {
    put("billingSsn", ExtraFieldRule(InputType.TYPE_CLASS_NUMBER, null, "SSN"))
    put("billingCity", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "City"))
    put("userRuPhone", ExtraFieldRule(InputType.TYPE_CLASS_PHONE, null, "Phone Number"))
    put("billingState", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "State"))
    put("userLastName", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Last Name"))
    put("userFirstName", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "First Name"))
    put("billingCountry", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Country"))
    put("billingZipCode", ExtraFieldRule(InputType.TYPE_CLASS_NUMBER, null, "ZIP Code"))
    put("userMiddleName", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Middle Name"))
    put("userDateOfBirth", ExtraFieldRule(InputType.TYPE_CLASS_DATETIME, null, "Date of Birth"))
    put("userResidentialCity", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Residential City"))
    put("userResidentialStreet", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Residential Street"))
    put("userResidentialCountry", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Residential Country"))
    put("userRuPassportIssuedBy", ExtraFieldRule(InputType.TYPE_TEXT_FLAG_CAP_WORDS, null, "Issued by"))
    put("userResidentialAptSuite", ExtraFieldRule(InputType.TYPE_CLASS_TEXT, null, "Residential Apt"))
    put("userResidentialPostcode", ExtraFieldRule(InputType.TYPE_CLASS_TEXT, null, "Residential Postcode"))
    put("userRuPassportIssueDate", ExtraFieldRule(InputType.TYPE_CLASS_DATETIME, null, "Issue Date"))
    put("userResidentialPostcodeUK", ExtraFieldRule(InputType.TYPE_CLASS_TEXT, null, "Residential Postcode"))
}

data class ExtraFieldRule(
    val inputType: Int,
    val inputFilter: InputFilter?,
    val description: String
)

@BindingAdapter("android:inputType")
fun TextInputEditText.applyInputType(inputType: Int) {
    this.inputType = inputType
}

@BindingAdapter("inputFilter")
fun TextInputEditText.applyInputFilter(filter: InputFilter?) {
    filter?.let {
        filters = arrayOf(filter)
    }
}
