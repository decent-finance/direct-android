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

package com.cexdirect.lib.terms

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import com.cexdirect.lib.R
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore
class TermsActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(TermsActivity::class.java, true, false)

    @Test
    fun displayGivenData() {
        val givenIntent = Intent().apply {
            putExtra("title", "Title")
            putExtra("content", "Content")
        }

        activityRule.launchActivity(givenIntent)

        onView(withText("Title")).check(matches(isDisplayed()))
        onView(withText("Content")).check(matches(isDisplayed()))
    }

    @Test
    fun exitOnOkClick() {
        val givenIntent = Intent().apply {
            putExtra("title", "Title")
            putExtra("content", "Content")
        }

        activityRule.launchActivity(givenIntent)
        onView(withText(R.string.cexd_ok)).perform(click())

        assertThat(activityRule.activity.isFinishing).isTrue()
    }
}
