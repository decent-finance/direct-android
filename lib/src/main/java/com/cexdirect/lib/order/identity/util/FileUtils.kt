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

@file:Suppress("MatchingDeclarationName")

package com.cexdirect.lib.order.identity.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Base64OutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

enum class FailType {
    SIZE_INVALID, UNSUPPORTED_FORMAT
}

fun checkAndSet(imgFilePath: String, success: (imgFilePath: String) -> Unit, failure: () -> Unit) {
    val imgFile = File(imgFilePath)
    if (fileSizeValid(imgFile.length())) {
        success.invoke(imgFilePath)
    } else {
        failure.invoke()
    }
}

fun checkAndSet(
    context: Context,
    uri: Uri,
    success: (imgUri: Uri) -> Unit,
    failure: (type: FailType) -> Unit
) {
    val resolver = context.contentResolver

    val mimeType = resolver.getType(uri) ?: ""
    if (!mimeType.startsWith("image/") or mimeType.contentEquals("image/tiff")) {
        failure.invoke(FailType.UNSUPPORTED_FORMAT)
        return
    }

    val fileDescriptor =
        resolver.openTypedAssetFileDescriptor(uri, "image/*", null)!!

    val fileLength = fileDescriptor.length
    if (fileSizeValid(fileLength)) {
        success.invoke(uri)
    } else {
        failure.invoke(FailType.SIZE_INVALID)
    }
}

fun convertStreamToBase64(input: InputStream): String = ByteArrayOutputStream().use { output ->
    Base64OutputStream(output, Base64.DEFAULT).use { base64Output ->
        input.copyTo(base64Output)
        base64Output.flush()
        output.toString("UTF-8")
    }
}

fun convertStreamToBytes(input: InputStream): ByteArray =
    ByteArrayOutputStream().use { out ->
        input.copyTo(out)
        out.toByteArray()
    }

fun fileSizeValid(length: Long) = (length / BYTES_IN_MB) <= MAX_FILE_SIZE_MB

const val MAX_FILE_SIZE_MB = 15
const val BYTES_IN_MB = 1024.0 * 1024
