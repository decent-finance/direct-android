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

package com.cexdirect.lib._util

import com.cexdirect.lib._network.models.Precision
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

fun Double.formatAmount(precision: Precision) =
    NumberFormat.getNumberInstance(Locale.US).apply {
        when (precision.visibleRoundRule) {
            "bigger" -> {
                roundingMode = RoundingMode.HALF_UP
                minimumFractionDigits = precision.visiblePrecision
                maximumFractionDigits = precision.visiblePrecision
            }
            "smaller" -> {
                // TODO set proper rounding mode?
                roundingMode = RoundingMode.DOWN
                minimumFractionDigits = precision.visiblePrecision
                maximumFractionDigits = precision.visiblePrecision
            }
            "trunk" -> {
                roundingMode = RoundingMode.DOWN
                minimumFractionDigits = precision.visiblePrecision
                maximumFractionDigits = precision.visiblePrecision
            }
            "dynamic" -> {
            }
        }
    }.format(this).replace(",", "")

fun String.binifyCardNumber() = this.takeLast(4)
