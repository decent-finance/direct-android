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
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import com.cexdirect.lib.databinding.LayoutBtnLegalBinding
import com.cexdirect.lib.network.models.RuleData

class LegalButtonLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var rules = emptySet<RuleData>()
        set(value) {
            field = value
            updateButtons()
        }

    val listener = object : LegalClickListener {
        override fun onClicked(data: RuleData) {
            clickEvent?.postValue(data)
        }
    }

    var clickEvent: LegalClickEvent? = null

    init {
        orientation = VERTICAL
    }

    private fun updateButtons() {
        if (childCount > 0) removeAllViews()
        val inflater = LayoutInflater.from(context)
        rules.foldIndexed(LinkedHashSet<LinearLayout>(), { index, acc, ruleData ->
            val rowHolder =
                if (index % 2 == 0) {
                    LinearLayout(context).apply {
                        layoutParams = LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                        )
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                } else {
                    acc.last()
                }

            LayoutBtnLegalBinding.inflate(inflater, this, false).apply {
                data = ruleData
                listener = this@LegalButtonLayout.listener
            }.root.let { rowHolder.addView(it) }

            acc.apply { add(rowHolder) }
        }).forEach { addView(it) }
    }
}

interface LegalClickListener {
    fun onClicked(data: RuleData)
}

@BindingAdapter("rules", "clickEvent", requireAll = true)
fun LegalButtonLayout.applyOnButtonClickListener(rules: Set<RuleData>, event: LegalClickEvent) {
    this.clickEvent = event
    this.rules = rules
}

