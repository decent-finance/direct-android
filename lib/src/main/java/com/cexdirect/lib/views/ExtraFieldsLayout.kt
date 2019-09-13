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
import androidx.annotation.StringRes
import androidx.databinding.*
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.LayoutExtraFieldBinding
import com.cexdirect.lib.network.models.Additional
import com.cexdirect.lib.util.FieldStatus
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class ExtraFieldsLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var additional: Map<String, Additional> = emptyMap()
        set(value) {
            field = value.sortNameFields()

            removeAllViews()
            updateLayout()
        }

    var input = ObservableArrayMap<String, String>()
    var validation = ObservableArrayMap<String, FieldStatus>()

    private fun updateLayout() {
        val inflater = LayoutInflater.from(context)
        additional.forEach { entry ->
            if (entry.value.req) {
                val binding: LayoutExtraFieldBinding = DataBindingUtil.inflate(
                        inflater, R.layout.layout_extra_field, this, false
                )
                binding.apply {
                    entry.value.value?.takeIf { it.isNotBlank() }?.let { this@ExtraFieldsLayout.input[entry.key] = it }
                    editable = entry.value.editable
                    key = entry.key
                    input = this@ExtraFieldsLayout.input
                    rule = fieldRules[entry.key]
                    validation = this@ExtraFieldsLayout.validation
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

@BindingAdapter("input", "validation")
fun ExtraFieldsLayout.applyInput(input: ObservableArrayMap<String, String>, validation: ObservableArrayMap<String, FieldStatus>) {
    this.input = input
    this.validation = validation
}

@BindingAdapter("additionalFields")
fun ExtraFieldsLayout.applyAdditionalFields(additional: Map<String, Additional>) {
    this.additional = additional
}

val fieldRules = HashMap<String, ExtraFieldRule>().apply {
    put(
            "billingSsn",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_NUMBER,
                    null,
                    R.string.cexd_ssn,
                    R.string.cexd_invalid_ssn
            )
    )
    put(
            "billingCity",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_city,
                    R.string.cexd_invalid_city
            )
    )
    put(
            "userRuPhone",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_PHONE,
                    null,
                    R.string.cexd_phone_number,
                    R.string.cexd_invalid_phone
            )
    )
    put(
            "billingState",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_state
            )
    )
    put(
            "userLastName",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_last_name,
                    R.string.cexd_invalid_last_name
            )
    )
    put(
            "userFirstName",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_first_name,
                    R.string.cexd_invalid_first_name
            )
    )
    put(
            "billingCountry",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_country
            )
    )
    put(
            "billingZipCode",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_NUMBER,
                    null,
                    R.string.cexd_zip,
                    R.string.cexd_invalid_zip
            )
    )
    put(
            "userMiddleName",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_middle_name,
                    R.string.cexd_invalid_mid_name
            )
    )
    put(
            "userDateOfBirth",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_DATETIME,
                    null,
                    R.string.cexd_dob,
                    R.string.cexd_invalid_dob
            )
    )
    put(
            "userResidentialCity",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_residential_city,
                    R.string.cexd_invalid_city
            )
    )
    put(
            "userResidentialStreet",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_residential_street,
                    R.string.cexd_invalid_street
            )
    )
    put(
            "userResidentialCountry",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_residential_country,
                    R.string.cexd_invalid_country
            )
    )
    put(
            "userRuPassportIssuedBy",
            ExtraFieldRule(
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    null,
                    R.string.cexd_issued_by
            )
    )
    put(
            "userResidentialAptSuite",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_TEXT,
                    null,
                    R.string.cexd_residential_apt
            )
    )
    put(
            "userResidentialPostcode",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_TEXT,
                    null,
                    R.string.cexd_residential_postcode,
                    R.string.cexd_invalid_mid_postcode
            )
    )
    put(
            "userRuPassportIssueDate",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_DATETIME,
                    null,
                    R.string.cexd_issue_date
            )
    )
    put(
            "userResidentialPostcodeUK",
            ExtraFieldRule(
                    InputType.TYPE_CLASS_TEXT,
                    null,
                    R.string.cexd_residential_postcode,
                    R.string.cexd_invalid_mid_postcode
            )
    )
}

data class ExtraFieldRule(
        val inputType: Int,
        val inputFilter: InputFilter?,
        val description: Int,
        val validationMessage: Int = R.string.cexd_invalid_generic_field
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

@BindingAdapter("android:hint")
fun TextInputLayout.applyHintRes(@StringRes id: Int) {
    hint = context.getString(id)
}

fun Map<String, Additional>.sortNameFields() =
        if (this.containsKey("userLastName") && this.containsKey("userFirstName")) {
            val keys = ArrayList<String>().apply { addAll(this@sortNameFields.keys) }
            val lastNameIndex = keys.indexOf("userLastName")
            val firstNameIndex = keys.indexOf("userFirstName")

            if (lastNameIndex < firstNameIndex) {
                Collections.swap(keys, lastNameIndex, firstNameIndex)
            }

            val sorted = LinkedHashMap<String, Additional>()
            keys.forEach {
                sorted[it] = this[it] ?: error("")
            }
            sorted
        } else {
            this
        }
