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

package com.cexdirect.lib.util

import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.TradeInputFilter
import com.cexdirect.lib.databinding.ItemReturnBinding
import com.cexdirect.lib.network.models.MonetaryData
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.terms.showTerms
import com.cexdirect.lib.verification.OrderStep
import com.cexdirect.lib.verification.StepsPagerAdapter
import com.cexdirect.lib.verification.confirmation._3dsData
import com.cexdirect.lib.views.SuperDuperViewPager
import ru.noties.markwon.Markwon
import java.net.URLEncoder

@BindingAdapter("showSoftInputOnFocus")
fun EditText.makeShowSoftInput(show: Boolean) {
    requestFocus()
    showSoftInputOnFocus = show
}

@BindingAdapter("onFocused", "onUnfocused", requireAll = false)
fun EditText.setFocusListener(onFocused: Runnable?, onUnfocused: Runnable?) {
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            onFocused?.run()
        } else {
            onUnfocused?.run()
        }
    }
}

@BindingAdapter("offscreenPages")
fun ViewPager.applyOffscreenPageLimit(limit: Int) {
    offscreenPageLimit = limit
}

@BindingAdapter("currentPosition", "pagerAdapter", "orderStep", requireAll = true)
fun SuperDuperViewPager.applyAdapter(position: Int, adapter: StepsPagerAdapter, step: OrderStep) {
    if (adapter != getAdapter()) {
        setAdapter(adapter)
        addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    when (orderStep) {
                        OrderStep.LOCATION_EMAIL, OrderStep.PAYMENT_BASE -> {
                            // nop
                        }
                        else -> setCurrentItem(currentPos, true)
                    }
                    return
                }
                if (position > currentPos) {
                    setCurrentItem(currentPos, true)
                } else if (position < currentPos) {
                    setCurrentItem(currentPos, true)
                }
            }
        })
    }
    if (currentPos != position) {
        currentPos = position
        setCurrentItem(position, false)
    }
    if (orderStep != step) {
        orderStep = step
        val binding = DataBindingUtil.findBinding<ItemReturnBinding>(
            findViewWithTag<View>("return")
        ) ?: error("Could not find binding. Child count: $childCount")
        when (orderStep) {
            OrderStep.LOCATION_EMAIL, OrderStep.PAYMENT_BASE -> binding.editVisible = true
            else -> binding.editVisible = false
        }
    }
}


@BindingAdapter("pic")
fun ImageView.applyPic(id: Int) {
    val drawable = ResourcesCompat.getDrawable(resources, id, context.theme)
    setImageDrawable(drawable)
}

@BindingAdapter("adapter")
fun RecyclerView.applyAdapter(adapter: RecyclerView.Adapter<*>) {
    this.adapter = adapter
}

@BindingAdapter("isActivated")
fun View.applyActivation(activated: Boolean) {
    this.isActivated = activated
}

@InverseBindingAdapter(attribute = "input", event = "inputAttrChanged")
fun EditText.getCurrentText() = text.toString()

@BindingAdapter("inputAttrChanged")
fun EditText.applyTextWatcher(listener: InverseBindingListener) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            listener.onChange()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    })
}

@BindingAdapter("input", "tradeInputFilter")
fun EditText.setCurrentText(text: String?, filter: TradeInputFilter?) {
    filter?.let { filters = arrayOf(it) }
    if (this.text.toString() != text) {
        this.setText(text)
        this.setSelection(this.text.length)
    }
}

@BindingAdapter("_3dsData")
fun WebView.apply3DsData(_3dsData: _3dsData?) {
    _3dsData?.takeIf { it.hasData() }
        ?._3dsExtras
        ?.apply { this["TermUrl"] = _3dsData.termUrl }
        ?.mapValues { URLEncoder.encode(it.value, "UTF-8") }
        ?.asIterable()
        ?.joinToString("&") { (key, value) -> "$key=$value" }
        ?.toByteArray()
        ?.let { this.postUrl(_3dsData._3dsUrl, it) }
}

@BindingAdapter("content")
fun TextView.loadContent(content: String) {
    Markwon.create(context)
        .let {
            val node = it.parse(content)
            val spanned = it.render(node)
            it.setParsedMarkdown(this, spanned)
        }
}

@BindingAdapter("coinIcon")
fun ImageView.applyCoinIcon(data: MonetaryData?) {
    data?.let { setImageResource(symbolMap[it.currency].orDefault().iconId) }
}

@BindingAdapter("legal")
fun TextView.applyLegalText(rules: Set<RuleData>) {
    movementMethod = LinkMovementMethod.getInstance()
    rules.fold(SpannableStringBuilder(context.getString(R.string.cexd_agree)), { acc, ruleData ->
        val spannableString = SpannableString(ruleData.formattedName())
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                context.showTerms(ruleData.formattedName(), ruleData.value)
            }
        }
        spannableString.setSpan(
            clickableSpan,
            0,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        acc.apply {
            append(" ")
            append(spannableString)
            append(",")
        }
    }).let {
        val last = it.length - 1
        it.subSequence(0, last)
    }.let {
        text = it
    }
}

@BindingAdapter("android:imeOptions")
fun EditText.applyImeOption(options: Int) {
    imeOptions = options
}

@BindingAdapter("onLongClick")
fun View.applyLongClickListener(runnable: Runnable) {
    setOnLongClickListener {
        runnable.run()
        true
    }
}
