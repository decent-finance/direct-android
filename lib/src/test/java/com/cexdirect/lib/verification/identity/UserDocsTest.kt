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

package com.cexdirect.lib.verification.identity

import com.cexdirect.lib.R
import com.cexdirect.lib.StringProvider
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class UserDocsTest {

    lateinit var userDocs: UserDocs

    @Mock
    lateinit var stringProvider: StringProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        userDocs = UserDocs(stringProvider)
    }

    @After
    fun tearDown() {
        reset(stringProvider)
    }

    @Test
    fun setOneReqPhotoForPassport() {
        userDocs.documentType = DocumentType.PASSPORT

        assertThat(userDocs.requiredPhotos).isEqualTo(1)
    }

    @Test
    fun setTwoReqPhotosForIdCard() {
        userDocs.documentType = DocumentType.ID_CARD

        assertThat(userDocs.requiredPhotos).isEqualTo(2)
    }

    @Test
    fun setTwoReqPhotosForDriverLicence() {
        userDocs.documentType = DocumentType.DRIVER_LICENCE

        assertThat(userDocs.requiredPhotos).isEqualTo(2)
    }

    @Test
    fun invokeUploadActionWhenShouldSendPhoto() {
        val mock: Runnable = mock()
        userDocs.uploadAction = { mock.run() }

        userDocs.shouldSendPhoto = true

        verify(mock).run()
    }

    @Test
    fun dontInvokeUploadActionWhenShouldNotSendPhoto() {
        val mock: Runnable = mock()
        userDocs.uploadAction = { mock.run() }

        userDocs.shouldSendPhoto = false

        verify(mock, never()).run()
    }

    @Test
    fun setSendPhotosToTrueWhenReqAmountReached() {
        userDocs.uploadAction = { }
        userDocs.requiredPhotos = 2

        userDocs.imagesBase64.let {
            it["abc"] = "abc"
            it["def"] = "def"
        }

        assertThat(userDocs.shouldSendPhoto).isTrue()
    }

    @Test
    fun notSetSendPhotosToTrueWhenReqAmountNotReached() {
        userDocs.requiredPhotos = 2

        userDocs.imagesBase64.let {
            it["abc"] = "abc"
        }

        assertThat(userDocs.shouldSendPhoto).isFalse()
    }

    @Test
    fun setIdCardData() {
        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        userDocs.selectedDocType = R.id.fiIdCard

        assertThat(userDocs)
            .hasFieldOrPropertyWithValue("documentTypeSelected", true)
            .hasFieldOrPropertyWithValue("documentType", DocumentType.ID_CARD)
            .hasFieldOrPropertyWithValue("documentImage", R.drawable.ic_pic_id_card)
            .hasFieldOrPropertyWithValue("documentImageBack", R.drawable.ic_pic_id_card)
        verify(stringProvider, atMost(2)).provideString(eq(R.string.cexd_take_pic_id))
    }

    @Test
    fun setPassportData() {
        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        userDocs.selectedDocType = R.id.fiPassport

        assertThat(userDocs)
            .hasFieldOrPropertyWithValue("documentTypeSelected", true)
            .hasFieldOrPropertyWithValue("documentType", DocumentType.PASSPORT)
            .hasFieldOrPropertyWithValue("documentImage", R.drawable.ic_pic_passport)
        verify(stringProvider).provideString(eq(R.string.cexd_take_pic_passport))
    }

    @Test
    fun setLicenceData() {
        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        userDocs.selectedDocType = R.id.fiDriversLicence

        assertThat(userDocs)
            .hasFieldOrPropertyWithValue("documentTypeSelected", true)
            .hasFieldOrPropertyWithValue("documentType", DocumentType.DRIVER_LICENCE)
            .hasFieldOrPropertyWithValue("documentImage", R.drawable.ic_pic_driver_license)
            .hasFieldOrPropertyWithValue("documentImageBack", R.drawable.ic_pic_driver_license)
        verify(stringProvider).provideString(eq(R.string.cexd_take_pic_licence))
    }
}
