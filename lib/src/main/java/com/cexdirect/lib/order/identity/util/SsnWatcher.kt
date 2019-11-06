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

package com.cexdirect.lib.order.identity.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

@Suppress("MagicNumber")
class SsnWatcher(private val target: EditText) : TextWatcher {

    private var deleteSpace = false

    override fun afterTextChanged(editable: Editable?) {
        target.removeTextChangedListener(this)

        val cursorPosition = target.selectionStart
        val withSpaces = formatText(editable.toString())
        target.setText(withSpaces)

        target.setSelection(cursorPosition + (withSpaces.length - (editable?.length ?: 0)))

        if (deleteSpace) {
            deleteSpace = false
        }

        target.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (start > 0) {
            val charDeleted = s?.subSequence(start - 1, start)
            deleteSpace = "-" == charDeleted.toString()
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // do nothing
    }

    private fun formatText(text: CharSequence): String {
        val formatted = StringBuilder()
        if (text.length == 4 || text.length == 7) {
            if (!deleteSpace) {
                formatted.append(
                    text.subSequence(
                        0,
                        text.length - 1
                    ).toString() + "-" + text[text.length - 1]
                )
            } else {
                formatted.append(text.subSequence(0, text.length - 1))
            }
        } else {
            formatted.append(text)
        }
        return formatted.toString()
    }
}
