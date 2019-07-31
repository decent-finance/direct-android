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

package com.cexdirect.lib.verification.identity._util

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Base64OutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

fun convertAndSet(imgFilePath: String, block: (imgBase64: String) -> Unit) {
    FileInputStream(File(imgFilePath)).use { input ->
        convertStreamToBase64(input)?.let { block.invoke(it) }
    }
}

fun convertAndSet(context: Context, uri: Uri, block: (imgBase64: String) -> Unit) {
    context.contentResolver.openInputStream(uri)?.use { input ->
        convertStreamToBase64(input)?.let { block.invoke(it) }
    }
}

fun convertStreamToBase64(input: InputStream): String? {
    return ByteArrayOutputStream().use { output ->
        Base64OutputStream(output, Base64.DEFAULT).use { base64Output ->
            input.copyTo(base64Output)
            base64Output.flush()
            output.toString("UTF-8")
        }
    }
}
