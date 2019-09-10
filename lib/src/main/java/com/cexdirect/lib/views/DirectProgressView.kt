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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import com.cexdirect.lib.R

@Suppress("MagicNumber") // Because gladiolus
class DirectProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var colorFirst: Int = 0
    private var colorSecond: Int = 0

    private var startAngle: Float = 310F

    private val lineWidth = 10F
    private val padding = 10F

    private val oval = RectF()
        .apply {
            left = padding
            top = padding
        }

    private var angleProgress = 0F

    private val paint: Paint by lazy {
        Paint().apply {
            color = colorFirst
            strokeWidth = lineWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    private val archPaint: Paint by lazy {
        Paint().apply {
            color = colorSecond
            strokeWidth = lineWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DirectProgressView).apply {
            colorFirst =
                getColor(R.styleable.DirectProgressView_firstColor, Color.parseColor("#4DD8DF"))
            colorSecond =
                getColor(R.styleable.DirectProgressView_secondColor, Color.parseColor("#DEE0E2"))
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        oval.right = measuredWidth - padding
        oval.bottom = measuredWidth - padding

        startAnimation(CircleAnimation().apply {
            duration = 1500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.RESTART
            interpolator = AccelerateDecelerateInterpolator()

            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    archPaint.color = if (archPaint.color == colorFirst) colorSecond else colorFirst
                    paint.color = if (archPaint.color == colorSecond) colorFirst else colorSecond
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // nop
                }

                override fun onAnimationStart(animation: Animation?) {
                    // nop
                }
            })
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(oval.centerX(), oval.centerY(), (oval.right - padding) / 2, paint)
        canvas.drawArc(oval, startAngle, 310F, false, archPaint)
    }

    inner class CircleAnimation : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)

            angleProgress = 360 * interpolatedTime

            invalidate()
        }
    }
}
