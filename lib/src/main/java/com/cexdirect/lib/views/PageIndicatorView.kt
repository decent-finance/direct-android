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

package com.cexdirect.lib.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingAdapter
import com.cexdirect.lib.R
import com.mcxiaoke.koi.ext.dpToPx

class PageIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var current = 0
        set(value) {
            field = value
            invalidate()
        }

    var total: Int

    val radius = RADIUS_DP.dpToPx().toFloat()
    val radiusBig = RADIUS_DP_BIG.dpToPx().toFloat()

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH_DP.dpToPx().toFloat()
    }

    init {
        val array = context.theme.obtainStyledAttributes(
            attrs, R.styleable.PageIndicatorView, 0, 0
        )
        activePaint.color = array.getColor(
            R.styleable.PageIndicatorView_colorActive,
            context.getColorCompat(R.color.cexd_indicator_active)
        )
        inactivePaint.color = array.getColor(
            R.styleable.PageIndicatorView_colorInactive,
            context.getColorCompat(R.color.cexd_indicator_inactive)
        )
        selectionPaint.color = array.getColor(
            R.styleable.PageIndicatorView_colorSelection,
            context.getColorCompat(R.color.cexd_indicator_selected)
        )
        total = array.getInt(
            R.styleable.PageIndicatorView_total,
            DEFAULT_INDICATOR_AMOUNT
        )
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = WIDTH_DP.dpToPx() * total + WIDTH_DP.dpToPx() * (total - 1)
        val height = HEIGHT_DP.dpToPx()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        repeat(total) {
            when {
                isSelected(it) -> drawSelected(it, canvas)
                isActive(it) -> drawActive(it, canvas)
                else -> drawInactive(it, canvas)
            }
        }
    }

    private fun drawActive(it: Int, canvas: Canvas) {
        val offset = getOffset(it)
        canvas.drawCircle(
            offset.toFloat(), CIRCLE_CENTER_DP.dpToPx().toFloat(), radius, activePaint
        )
    }

    private fun getOffset(it: Int) = (WIDTH_DP.dpToPx() * it) * 2 + (WIDTH_DP / 2).dpToPx()

    private fun drawInactive(it: Int, canvas: Canvas) {
        val offset = getOffset(it)
        canvas.drawCircle(
            offset.toFloat(), CIRCLE_CENTER_DP.dpToPx().toFloat(), radius, inactivePaint
        )
    }

    private fun drawSelected(it: Int, canvas: Canvas) {
        val offset = getOffset(it)
        canvas.drawCircle(
            offset.toFloat(), CIRCLE_CENTER_DP.dpToPx().toFloat(), radius, activePaint
        )
        canvas.drawCircle(
            offset.toFloat(), CIRCLE_CENTER_DP.dpToPx().toFloat(), radiusBig, selectionPaint
        )
    }

    private fun isActive(pos: Int) = current >= pos
    private fun isSelected(pos: Int) = current == pos

    companion object {
        const val RADIUS_DP = 5
        const val RADIUS_DP_BIG = 8
        const val WIDTH_DP = 16
        const val HEIGHT_DP = 18
        const val DEFAULT_INDICATOR_AMOUNT = 4
        const val STROKE_WIDTH_DP = 1
        const val CIRCLE_CENTER_DP = 9
    }
}

@BindingAdapter("currentStep")
fun PageIndicatorView.applyCurrent(current: Int) {
    this.current = current
}
