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
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.cexdirect.lib.R

class DocumentTypeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), RadioCheckable {

    var label: String?
    private var activeBg: Int
    private var inactiveBg: Int
    private var activeTextColor: Int
    private var inactiveTextColor: Int

    var clickListener: OnClickListener? = null
    var touchListener: OnTouchListener? = null

    private var chkd = false
    val listeners = ArrayList<RadioCheckable.OnCheckedChangeListener>()

    private val textView: TextView

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DocumentTypeButton, 0, 0)
        label = a.getString(R.styleable.DocumentTypeButton_text)
        activeBg = a.getColor(
            R.styleable.DocumentTypeButton_activeBackgroundColor,
            context.getColorCompat(R.color.cexd_finish_bg)
        )
        inactiveBg = a.getColor(
            R.styleable.DocumentTypeButton_inactiveBackgroundColor,
            context.getColorCompat(R.color.cexd_inactive_bg)
        )
        activeTextColor = a.getColor(
            R.styleable.DocumentTypeButton_activeTextColor,
            context.getColorCompat(R.color.cexd_textfield_text)
        )
        inactiveTextColor = a.getColor(
            R.styleable.DocumentTypeButton_inactiveTextColor,
            context.getColorCompat(R.color.cexd_inactive_text)
        )
        a.recycle()

        LayoutInflater.from(context).inflate(R.layout.layout_type_selector, this, true)
        textView = findViewById(R.id.ltsText)
        textView.apply {
            text = label
            setTextColor(activeTextColor)
        }

        setCustomTouchListener()
        setBackgroundColor(activeBg)
    }

    override fun addOnCheckedChangeListener(listener: RadioCheckable.OnCheckedChangeListener) {
        listeners.add(listener)
    }

    override fun removeOnCheckedChangeListener(listener: RadioCheckable.OnCheckedChangeListener) {
        listeners.remove(listener)
    }

    override fun isChecked(): Boolean = chkd

    override fun toggle() {
        isChecked = !chkd
    }

    override fun setChecked(checked: Boolean) {
        chkd = checked
        listeners.forEach {
            it.onCheckedChanged(this, checked)
        }
        if (checked) {
            setCheckedState()
        } else {
            setUncheckedState()
        }
    }

    private fun setCheckedState() {
        setBackgroundColor(activeBg)
        textView.setTextColor(activeTextColor)
    }

    private fun setUncheckedState() {
        setBackgroundColor(inactiveBg)
        textView.setTextColor(inactiveTextColor)
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        touchListener = l
    }

    private fun onTouchDown(motionEvent: MotionEvent) {
        isChecked = true
    }

    private fun onTouchUp(motionEvent: MotionEvent) {
        clickListener?.onClick(this)
    }

    private fun setCustomTouchListener() {
        super.setOnTouchListener(TouchListener())
    }

    private inner class TouchListener : OnTouchListener {

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onTouchDown(event)
                MotionEvent.ACTION_UP -> onTouchUp(event)
            }

            touchListener?.onTouch(v, event)

            return true
        }
    }
}
