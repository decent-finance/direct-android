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
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.cexdirect.lib.R
import com.mcxiaoke.koi.ext.dpToPx

class CollapsibleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var layoutState = LayoutState.EMPTY
        set(value) {
            field = value
            updateLayoutState()
        }

    var contentState = ContentState.EXPANDED
        set(value) {
            field = value
            when (value) {
                ContentState.COLLAPSED -> collapse()
                ContentState.EXPANDED -> expand()
            }
        }

    private fun updateLayoutState() {
        when (layoutState) {
            LayoutState.EMPTY -> {
                (layoutParams as LayoutParams).apply {
                    setMargins(0, topMargin, 0, bottomMargin)
                    setPadding(0, 0, 0, 0)
                }.also { layoutParams = it }
                background = null
            }
            LayoutState.ENCLOSED -> {
                (layoutParams as LayoutParams).apply {
                    setMargins(DEFAULT_MARGIN_DP.dpToPx(), topMargin, DEFAULT_MARGIN_DP.dpToPx(), bottomMargin)
                    setPadding(0, DEFAULT_MARGIN_DP.dpToPx(), 0, DEFAULT_MARGIN_DP.dpToPx())
                }.also { layoutParams = it }
                background = ResourcesCompat.getDrawable(resources, R.drawable.shape_border, context.theme)
            }
        }
    }

    private fun expand() {
        visibility = View.VISIBLE
    }

    private fun collapse() {
        visibility = View.GONE
    }

    companion object {
        const val DEFAULT_MARGIN_DP = 10
    }

    enum class LayoutState { ENCLOSED, EMPTY }

    enum class ContentState { EXPANDED, COLLAPSED }
}

@BindingAdapter("layoutState")
fun CollapsibleLayout.applyLayoutState(state: CollapsibleLayout.LayoutState) {
    layoutState = state
}

@BindingAdapter("contentState")
fun CollapsibleLayout.applyContentState(state: CollapsibleLayout.ContentState) {
    contentState = state
    requestLayout()
}
