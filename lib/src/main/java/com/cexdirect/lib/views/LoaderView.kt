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

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.ContentFrameLayout
import androidx.core.content.res.ResourcesCompat
import com.cexdirect.lib.R

class LoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val contentView =
        ((context as Activity).window.decorView as FrameLayout).findViewById<ContentFrameLayout>(
            android.R.id.content
        )

    private val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

    init {
        View.inflate(context, R.layout.layout_loader, this)
        setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.cexd_transparent,
                context.theme
            )
        )
        isClickable = true
        isFocusable = true
    }

    fun show() {
        hideKeyboard()
        contentView.indexOfChild(this)
            .takeIf { it == -1 }
            ?.let {
                contentView.addView(this, params)
                requestFocus()
            }
    }

    fun hide() {
        contentView.indexOfChild(this)
            .takeIf { it > -1 }
            ?.let { contentView.removeView(this) }
    }

    private fun hideKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
