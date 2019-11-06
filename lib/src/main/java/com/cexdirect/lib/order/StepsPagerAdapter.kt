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

package com.cexdirect.lib.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.cexdirect.lib.R
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.VoidLiveEvent
import com.cexdirect.lib.databinding.ItemReturnBinding
import com.cexdirect.lib.databinding.ItemStepBinding

class StepsPagerAdapter(
    private val stringProvider: StringProvider,
    private val editClickEvent: VoidLiveEvent
) : PagerAdapter(), EditClickListener {

    private var title = ""

    private val steps = listOf(
        "Fill Information",
        "Payment Confirmation",
        "Finish"
    )

    override fun instantiateItem(container: ViewGroup, position: Int) =
        when (position) {
            0 -> DataBindingUtil.inflate<ItemReturnBinding>(
                LayoutInflater.from(container.context),
                R.layout.item_return,
                container,
                false
            )
                .apply {
                    text = title
                    editVisible = true
                    listener = this@StepsPagerAdapter
                }
                .let {
                    it.root.tag = "return"
                    container.addView(it.root)
                    it.root
                }
            else -> DataBindingUtil.inflate<ItemStepBinding>(
                LayoutInflater.from(container.context),
                R.layout.item_step,
                container,
                false
            )
                .apply { text = steps[position - 1] }
                .let {
                    container.addView(it.root)
                    it.root
                }
        }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount() = 4

    override fun onClick() {
        editClickEvent.call()
    }

    fun setOrderAmounts(cryptoAmount: String, crypto: String, fiatAmount: String, fiat: String) {
        title = stringProvider.provideString(
            R.string.cexd_title_get,
            cryptoAmount,
            crypto,
            fiatAmount,
            fiat
        )
    }
}

interface EditClickListener {

    fun onClick()
}
