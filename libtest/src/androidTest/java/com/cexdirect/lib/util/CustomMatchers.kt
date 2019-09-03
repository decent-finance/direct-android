package com.cexdirect.lib.util

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.ViewAssertion
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withTextInputLayout(@StringRes id: Int) =
        withTextInputLayout(InstrumentationRegistry.getInstrumentation().targetContext.getString(id))

fun withTextInputLayout(expectedErrorText: String): Matcher<View> = object : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description?) {}

    override fun matchesSafely(item: View?): Boolean {
        if (item !is TextInputLayout) return false
        val error = item.hint ?: return false
        val hint = error.toString()
        return expectedErrorText == hint
    }
}

fun hasVisibility(visibility: Int): ViewAssertion =
        ViewAssertion { view, _ -> visibility == view?.visibility }
