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

package com.cexdirect.lib.verification.identity.img

import android.content.ContentResolver
import android.net.Uri
import com.cexdirect.lib.verification.identity.util.convertStreamToBase64
import java.io.FileInputStream

interface ImageReference {

    fun encodeToBase64(): String
}

class CameraImageReference(private val imgPath: String) : ImageReference {

    override fun encodeToBase64(): String =
        FileInputStream(imgPath).use { input -> convertStreamToBase64(input) }
}

class GalleryImageReference(
    private val imgUri: Uri,
    private val resolver: ContentResolver
) : ImageReference {

    override fun encodeToBase64(): String =
        resolver.openInputStream(imgUri)?.use { input -> convertStreamToBase64(input) }
            ?: error("Failed to open input stream")
}
