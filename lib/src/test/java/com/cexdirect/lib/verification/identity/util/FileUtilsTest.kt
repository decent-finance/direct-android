/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

package com.cexdirect.lib.verification.identity.util

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class FileUtilsTest {

    @Test
    fun callFailureForTiffImage() {
        val failure = mock<(type: FailType) -> Unit>()
        val resolver = mock<ContentResolver> {
            on { getType(any()) } doReturn "image/tiff"
        }
        val context = mock<Context> {
            on { contentResolver } doReturn resolver
        }

        checkAndSet(context, mock(), {}, failure)

        verify(failure).invoke(eq(FailType.UNSUPPORTED_FORMAT))
    }

    @Test
    fun callFailureForPdfFile() {
        val failure = mock<(type: FailType) -> Unit>()
        val resolver = mock<ContentResolver> {
            on { getType(any()) } doReturn "application/pdf"
        }
        val context = mock<Context> {
            on { contentResolver } doReturn resolver
        }

        checkAndSet(context, mock(), {}, failure)

        verify(failure).invoke(eq(FailType.UNSUPPORTED_FORMAT))
    }

    @Test
    fun callFailureForFileLength() {
        val failure = mock<(type: FailType) -> Unit>()
        val fileDescriptor = mock<AssetFileDescriptor> {
            on { length } doReturn 20 * BYTES_IN_MB.toLong()
        }
        val resolver = mock<ContentResolver> {
            on { getType(any()) } doReturn "image/png"
            on {
                openTypedAssetFileDescriptor(
                    any(),
                    eq("image/*"),
                    anyOrNull()
                )
            } doReturn fileDescriptor
        }
        val context = mock<Context> {
            on { contentResolver } doReturn resolver
        }

        checkAndSet(context, mock(), {}, failure)

        verify(failure).invoke(eq(FailType.SIZE_INVALID))
    }
}
