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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

class SwitcherLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {


    var viewSwitchedListener: ViewSwitchedListener? = null

    private var hasSelectedChild = false
    private var selectedChildId = View.NO_ID

    private val hierarchyChangeListener = HierarchyChangeListener()
    private val childCheckListener = CheckedStateTracker()

    @SuppressLint("UseSparseArrays")
    private val childViewsMap = HashMap<Int, View>()

    init {
        super.setOnHierarchyChangeListener(hierarchyChangeListener)
    }

    fun check(@IdRes id: Int) {
        if (selectedChildId != id) {
            updateChildren(id)
        }
    }

    fun getSelectedView() = childViewsMap[selectedChildId]

    private fun updateChildren(@IdRes id: Int) {
        childViewsMap.keys.forEach {
            val view = childViewsMap[it]!!
            if (it == id) {
                (view as RadioCheckable).isChecked = true
                hasSelectedChild = true
                selectedChildId = view.id
                viewSwitchedListener?.onViewSwitched(view)
            } else {
                (view as RadioCheckable).isChecked = false
            }
        }
    }

    override fun removeView(view: View) {
        if (view is RadioCheckable) {
            childViewsMap.remove(view.id)
        }
        super.removeView(view)
    }

    override fun setOnHierarchyChangeListener(listener: OnHierarchyChangeListener?) {
        hierarchyChangeListener.otherListener = listener
    }

    inner class HierarchyChangeListener : OnHierarchyChangeListener {
        internal var otherListener: OnHierarchyChangeListener? = null

        override fun onChildViewRemoved(parent: View, child: View) {
            if (parent === this@SwitcherLayout && child is RadioCheckable) {
                childViewsMap.remove(child.id)
                child.removeOnCheckedChangeListener(childCheckListener)
            }
            otherListener?.onChildViewRemoved(parent, child)
        }

        override fun onChildViewAdded(parent: View, child: View) {
            if (parent === this@SwitcherLayout && child is RadioCheckable) {
                childViewsMap[child.id] = child
                if (!hasSelectedChild) {
                    child.isChecked = true
                }
                child.addOnCheckedChangeListener(childCheckListener)
            }
            otherListener?.onChildViewAdded(parent, child)
        }
    }

    interface ViewSwitchedListener {

        fun onViewSwitched(v: View)
    }

    private inner class CheckedStateTracker : RadioCheckable.OnCheckedChangeListener {
        var protectFromRecursion = false

        override fun onCheckedChanged(view: View, isChecked: Boolean) {
            // prevents from infinite recursion
            if (protectFromRecursion) {
                return
            }

            protectFromRecursion = true
            if (selectedChildId != view.id) {
                updateChildren(view.id)
                selectedChildId = view.id
            }
            protectFromRecursion = false
        }
    }
}

@BindingAdapter("selectedViewChanged")
fun SwitcherLayout.applyViewSwitchedListener(listener: InverseBindingListener) {
    viewSwitchedListener = object : SwitcherLayout.ViewSwitchedListener {
        override fun onViewSwitched(v: View) {
            listener.onChange()
        }
    }
}

@InverseBindingAdapter(attribute = "selectedViewId", event = "selectedViewChanged")
fun SwitcherLayout.getSelectedViewId() = this.getSelectedView()?.id ?: View.NO_ID

@BindingAdapter("selectedViewId")
fun SwitcherLayout.setSelectedViewId(@IdRes id: Int) {
    if (getSelectedView()?.id ?: View.NO_ID != id) {
        check(id)
    }
}
