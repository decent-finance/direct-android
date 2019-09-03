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

package com.cexdirect.lib.util

import java.util.regex.Pattern

val emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)!!

fun checkEmailStatus(email: String?) =
    if (email.isNullOrBlank()) {
        FieldStatus.EMPTY
    } else {
        if (emailPattern.matcher(email).matches()) {
            FieldStatus.VALID
        } else {
            FieldStatus.INVALID
        }
    }

enum class FieldStatus {
    EMPTY, INVALID, VALID
}
