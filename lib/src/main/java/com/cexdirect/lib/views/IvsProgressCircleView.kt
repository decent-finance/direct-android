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
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.getSize
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import androidx.core.content.res.ResourcesCompat
import com.cexdirect.lib.R
import com.mcxiaoke.koi.ext.dpToPx
import kotlin.math.min

@Suppress("MagicNumber")
class IvsProgressCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val oval = RectF()
    private val rect = Rect().apply {
        top = 0
        left = 0
    }

    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f

    private var progress = 0f

    private val circlePaint: Paint by lazy {
        Paint().apply {
            color = Color.parseColor("#DEE0E2")
            strokeWidth = 2.dpToPx().toFloat()
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    private val progressPaint: Paint by lazy {
        Paint().apply {
            color = Color.parseColor("#4DD8DF")
            strokeWidth = 4.dpToPx().toFloat()
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    private val transparentPaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ResourcesCompat.getColor(resources, R.color.cexd_screen_bg, context.theme)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, backgroundPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = min(getSize(widthMeasureSpec), 240.dpToPx())
        val height = min(getSize(heightMeasureSpec), 240.dpToPx())
        setMeasuredDimension(width, height)
        centerX = width / 2f
        centerY = height / 2f
        radius = min(width, height) / 2f - 4.dpToPx()

        oval.apply {
            top = centerY - radius
            bottom = centerY + radius
            left = centerX - radius
            right = centerX + radius
        }

        rect.apply {
            right = width
            bottom = height
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        startAnimation(CircleAnimation().apply {
            duration = TOTAL_DURATION_MILLIS
            repeatCount = Animation.INFINITE
            repeatMode = Animation.RESTART
            interpolator = LinearInterpolator()
        })
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)
        canvas.drawCircle(centerX, centerY, radius, transparentPaint)
        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        canvas.drawArc(oval, 270f, progress, false, progressPaint)
    }

    inner class CircleAnimation : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)
            progress = 360 * interpolatedTime
            invalidate()
        }
    }

    companion object {
        private const val TOTAL_DURATION_MILLIS = 2 * 60 * 1000L
    }
}
