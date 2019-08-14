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

package com.cexdirect.lib._util

import android.os.Handler
import android.os.Looper
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
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cexdirect.lib.R
import com.cexdirect.lib._network.models.MonetaryData
import com.cexdirect.lib.buy.TradeInputFilter
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.terms.showTerms
import com.cexdirect.lib.verification.confirmation._3dsData
import com.cexdirect.lib.views.SuperDuperViewPager
import ru.noties.markwon.Markwon
import java.net.URLEncoder

const val FIRE_ON_EXIT_DELAY = 200L

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

@BindingAdapter("currentPosition", "pagerAdapter", requireAll = true)
fun SuperDuperViewPager.applyAdapter(position: Int, adapter: PagerAdapter) {
    setAdapter(adapter)
    currentPos = position
    setCurrentItem(position, false)
}

@BindingAdapter("onExit")
fun SuperDuperViewPager.applyPageListener(r: Runnable) {
    addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            if (position == 0) {
                Handler(Looper.getMainLooper()).postDelayed(r, FIRE_ON_EXIT_DELAY)
            }
            if (position > currentPos) {
                setCurrentItem(currentPos, true)
            } else if (position in 2 until currentPos) {
                setCurrentItem(currentPos, true)
            }
        }
    })
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
fun ImageView.applyCoinIcon(data: MonetaryData) {
    setImageResource(symbolMap[data.currency].orDefault().iconId)
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
