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
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.cexdirect.lib.ClickListener
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.ClickEvent
import com.cexdirect.lib.databinding.LayoutPopularValuesBinding

class PopularValuesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ClickListener {

    var popularValues: List<String> = emptyList()
        set(value) {
            field = value
            binding.values = field
        }

    var currency = "USD"
        set(value) {
            field = value
            binding.currency = field
        }

    var clickEvent: ClickEvent? = null

    val binding: LayoutPopularValuesBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.layout_popular_values,
        this,
        true
    )

    init {
        binding.listener = this
    }

    override fun select(text: String) {
        clickEvent?.value = text.splitToSequence(" ").first()
    }
}

@BindingAdapter("popularValues", "currency", requireAll = true)
fun PopularValuesView.setValues(values: List<String>, currency: String) {
    popularValues = values
    this.currency = currency
}

@BindingAdapter("clickEvent")
fun PopularValuesView.applyClickEvent(event: ClickEvent) {
    clickEvent = event
}
