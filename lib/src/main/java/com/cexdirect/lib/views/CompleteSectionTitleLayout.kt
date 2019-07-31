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
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.LayoutCompleteSectionTitleBinding

class CompleteSectionTitleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    val binding: LayoutCompleteSectionTitleBinding
    val title: String?

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CompleteSectionTitleLayout, 0, 0)
        title = typedArray.getString(R.styleable.CompleteSectionTitleLayout_title)
        typedArray.recycle()
        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_border, context.theme)
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        binding = LayoutCompleteSectionTitleBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setTitle(title)
    }

    fun setTitle(title: String?) {
        binding.title = title
    }

    fun setArrowDirection(direction: Int) {
        binding.lcstArrow.apply {
            pivotX = measuredWidth / 2f
            pivotY = measuredHeight / 2f

            rotation = when (direction) {
                DIRECTION_DOWN -> 0f
                DIRECTION_UP -> 180f
                else -> error("Illegal value passed")
            }
        }
    }

    companion object {
        const val DIRECTION_DOWN = -1
        const val DIRECTION_UP = -2
    }
}

@BindingAdapter("arrowDirection")
fun CompleteSectionTitleLayout.applyArrowDirection(direction: Int) {
    setArrowDirection(direction)
}
