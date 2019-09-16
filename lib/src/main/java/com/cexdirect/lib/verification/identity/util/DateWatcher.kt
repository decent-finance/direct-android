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

package com.cexdirect.lib.verification.identity.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

@Suppress("MagicNumber")
class DateWatcher(private val editText: EditText) : TextWatcher {

    private var datePrevLength: Int = 0

    override fun afterTextChanged(p0: Editable?) {
        var date: String = editText.text.toString()
        if (date.isNotEmpty() && !CardUtils.dateWithSlash(date)) return
        if (date.length == 1 && date == "/") {
            date = "0"
            editText.setText(date)
            editText.setSelection(editText.text.toString().length)
        }
        if (date.length == 1) {
            val dateNumber = date.toInt()
            if (dateNumber > 1 && dateNumber <= 9) {
                date = CardUtils.getWithLeadingZeros(dateNumber, 1)
                editText.setText(date)
                editText.setSelection(editText.text.toString().length)
            }
        }
        if (date.length == 2) {
            if (datePrevLength <= 1) {
                date += "/"
                editText.setText(date)
                editText.setSelection(editText.text.toString().length)
            }
        }
        if (date.length == 3) {
            if (datePrevLength == 2) {
                if (date.takeLast(1) != "/" && CardUtils.onlyNumbers(date.takeLast(1))) {
                    date = date.take(2) + "/" + date.takeLast(1)
                    editText.setText(date)
                    editText.setSelection(editText.text.toString().length)
                    return
                }
            }
            if (datePrevLength == 4) {
                date = date.take(2)
                editText.apply {
                    setText(date)
                    setSelection(editText.text.toString().length)
                }
            }
        }
        datePrevLength = date.length // always the latest row in this method
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
}
