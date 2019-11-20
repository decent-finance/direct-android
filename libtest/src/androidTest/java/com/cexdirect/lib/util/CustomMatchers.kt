package com.cexdirect.lib.util

import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.test.espresso.Root
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

fun isToast(): Matcher<Root> {
    return object : TypeSafeMatcher<Root>() {

        public override fun matchesSafely(root: Root): Boolean {
            val type = root.windowLayoutParams.get().type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken = root.decorView.windowToken
                val appToken = root.decorView.applicationWindowToken
                // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return windowToken === appToken
            }
            return false
        }


        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }
    }
}
