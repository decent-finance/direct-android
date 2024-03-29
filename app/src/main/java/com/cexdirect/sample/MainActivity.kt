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

package com.cexdirect.sample

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cexdirect.lib.Direct

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnLaunch).setOnClickListener {
            Direct.startDirect()
        }

        findViewById<TextView>(R.id.tvBranchName).text = BuildConfig.BRANCH_NAME

        findViewById<TextView>(R.id.tvBuild).text = getString(
            R.string.build_date,
            BuildConfig.BUILD_DATE
        )

        findViewById<CheckBox>(R.id.cbLeakToggle).apply {
            if (BuildConfig.DEBUG) {
                setOnCheckedChangeListener { _, isChecked ->
                    toggleWatcher(isChecked)
                }
            } else {
                visibility = View.GONE
            }
        }
    }
}
