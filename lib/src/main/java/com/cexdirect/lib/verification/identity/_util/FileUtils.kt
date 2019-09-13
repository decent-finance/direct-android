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

fun convertAndSet(imgFilePath: String, block: (imgBase64: String) -> Unit, failure: () -> Unit) {
    val imgFile = File(imgFilePath)
    if (fileSizeValid(imgFile.length())) {
        FileInputStream(imgFile).use { input ->
            convertStreamToBase64(input)?.let { block.invoke(it) }
        }
    } else {
        failure.invoke()
    }
}

fun convertAndSet(
    context: Context,
    uri: Uri,
    success: (imgBase64: String) -> Unit,
    failure: () -> Unit
) {
    val fileLength =
        context.contentResolver.openTypedAssetFileDescriptor(uri, "*/*", null)!!.length
    if (fileSizeValid(fileLength)) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            convertStreamToBase64(input)?.let { success.invoke(it) }
        }
    } else {
        failure.invoke()
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

fun fileSizeValid(length: Long) =
    (length / (BYTES_IN_KB * BYTES_IN_KB)) <= MAX_FILE_SIZE_MB

const val MAX_FILE_SIZE_MB = 15
const val BYTES_IN_KB = 1024.0
