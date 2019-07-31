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
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.annotation.IdRes
import com.cexdirect.lib.R

class FooterScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    val mDeviceHeight = resources.displayMetrics.heightPixels
    var anchoredHeight: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var scrolled = false
    var footerView: View? = null
        set(value) {
            field = value
            invalidate()
        }
    var footerViewLocation = IntArray(2)
    private var mIsFooterSticky: Boolean = false
    private var mStickyFooterInitialTranslation: Int = 0
    private var mStickyFooterInitialLocation: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.FooterScrollView, defStyleAttr, 0)
        anchoredHeight = a.getDimension(R.styleable.FooterScrollView_anchoredHeight, 0f)
        val footerId = a.getResourceId(R.styleable.FooterScrollView_anchoredView, View.NO_ID)
        a.recycle()

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (footerId != View.NO_ID) {
                    setupFooter(footerId)
                }
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun setFooterTranslation(translationY: Float) {
        footerView?.translationY = translationY
    }

    fun resetTranslation() {
        setFooterTranslation(0f)
    }

    //    fun isFooterSticky() = 0f != footerView?.translationY
    fun isFooterSticky() = 0f != footerView?.translationY

    private fun getRelativeTop(view: View): Int {
        return if (view.parent === view.rootView) {
            view.top
        } else {
            view.top + getRelativeTop(view.parent as View)
        }
    }

    fun setupFooter(@IdRes viewId: Int) {
        footerView = findViewById(viewId)
        calculateFooterLocation(footerView!!.top, getRelativeTop(footerView!!))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        footerView?.takeIf { !changed }?.let {
            it.getLocationInWindow(footerViewLocation)
            calculateFooterLocation(getRelativeTop(it), footerViewLocation.first())
        }
    }

    override fun onScrollChanged(mScrollX: Int, mScrollY: Int, oldX: Int, oldY: Int) {
        super.onScrollChanged(mScrollX, mScrollY, oldX, oldY)
        scrolled = true
        mIsFooterSticky = if (scrollY > mStickyFooterInitialLocation - mDeviceHeight + anchoredHeight) {
            resetTranslation()
            false
        } else {
            setFooterTranslation((mStickyFooterInitialTranslation + scrollY).toFloat())
            true
        }
    }

    override fun onSaveInstanceState() = Bundle().apply {
        putParcelable(SUPER_STATE, super.onSaveInstanceState())
        putBoolean(SCROLL_STATE, scrolled)
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            scrolled = state.getBoolean(SCROLL_STATE)
            val superState: Parcelable = state.getParcelable(SUPER_STATE)!!
            super.onRestoreInstanceState(superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun calculateFooterLocation(footerTop: Int, footerLocation: Int) {
        if (scrolled) {
            mStickyFooterInitialTranslation = mDeviceHeight - footerTop - anchoredHeight.toInt()
            mStickyFooterInitialLocation = footerTop
            if (footerLocation > mDeviceHeight - anchoredHeight) {
                setFooterTranslation(mStickyFooterInitialTranslation.toFloat())
                mIsFooterSticky = true
            } else {
                resetTranslation()
                mIsFooterSticky = false
            }
            mStickyFooterInitialTranslation = mDeviceHeight - footerTop - anchoredHeight.toInt()
            mStickyFooterInitialLocation = footerTop
            mStickyFooterInitialTranslation = (mDeviceHeight - mStickyFooterInitialLocation - anchoredHeight).toInt()
        } else {
            initStickyFooter(footerTop)
        }
    }

    fun initStickyFooter(initialStickyFooterLocation: Int) {
        mStickyFooterInitialLocation = initialStickyFooterLocation
        mStickyFooterInitialTranslation = mDeviceHeight - initialStickyFooterLocation - anchoredHeight.toInt()
        if (mStickyFooterInitialLocation > mDeviceHeight - anchoredHeight.toInt()) {
            setFooterTranslation(mStickyFooterInitialTranslation.toFloat())
            mIsFooterSticky = true
        }
    }

    companion object {

        @JvmStatic
        private val SCROLL_STATE = "scroll_state"

        @JvmStatic
        private val SUPER_STATE = "super_state"
    }
}
