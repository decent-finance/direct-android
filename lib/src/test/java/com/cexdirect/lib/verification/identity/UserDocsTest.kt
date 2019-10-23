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
import com.cexdirect.lib.network.models.Images
import com.cexdirect.lib.util.FieldStatus
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
        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        userDocs.documentType = DocumentType.PASSPORT

        assertThat(userDocs.requiredImagesAmount).isEqualTo(1)
    }

    @Test
    fun setTwoReqPhotosForIdCard() {
        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        userDocs.documentType = DocumentType.ID_CARD

        assertThat(userDocs.requiredImagesAmount).isEqualTo(2)
    }

    @Test
    fun setTwoReqPhotosForDriverLicence() {
        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        userDocs.documentType = DocumentType.DRIVER_LICENCE

        assertThat(userDocs.requiredImagesAmount).isEqualTo(2)
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
        userDocs.requiredImagesAmount = 2

        userDocs.imagesBase64.let {
            it["abc"] = mock()
            it["def"] = mock()
        }

        assertThat(userDocs.shouldSendPhoto).isTrue()
    }

    @Test
    fun dontSetSendPhotosToTrueWhenReqAmountNotReached() {
        userDocs.requiredImagesAmount = 2

        userDocs.imagesBase64.let {
            it["abc"] = mock()
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
            .hasFieldOrPropertyWithValue("documentImage", R.drawable.ic_pic_id_front)
            .hasFieldOrPropertyWithValue("documentImageBack", R.drawable.ic_pic_id_back)
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
            .hasFieldOrPropertyWithValue("documentImage", R.drawable.ic_pic_license_front)
            .hasFieldOrPropertyWithValue("documentImageBack", R.drawable.ic_pic_license_back)
        verify(stringProvider).provideString(eq(R.string.cexd_take_pic_licence))
    }

    @Test
    fun invokeUploadWhenSelfieSet() {
        val mock: Runnable = mock()
        userDocs.uploadAction = { mock.run() }

        userDocs.selfieBase64 = mock()

        verify(mock).run()
    }

    @Test
    fun setSelfieStatusToInvalidAfterForceValidateWhenEmpty() {
        whenever(stringProvider.provideString(anyInt())).thenReturn("")
        userDocs.documentTypeSelected = true
        userDocs.requiredImages = Images(false, true)

        userDocs.forceValidate()

        assertThat(userDocs.selfieStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun dontSetSelfieStatusToInvalidAfterForceValidateWhenEmpty() {
        userDocs.documentTypeSelected = true
        userDocs.requiredImages = Images(false, false)

        userDocs.forceValidate()

        assertThat(userDocs.selfieStatus).isEqualTo(FieldStatus.EMPTY)
    }

    @Test
    fun validateSelfieStatus() {
        userDocs.documentTypeSelected = true
        userDocs.requiredImages = Images(false, true)
        userDocs.uploadAction = { }
        userDocs.selfieStatus = FieldStatus.VALID

        userDocs.forceValidate()

        assertThat(userDocs.selfieStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun setDocFrontStatusToInvalidWhenUnsupportedFormat() {
        whenever(stringProvider.provideString(eq(R.string.cexd_wrong_format))).thenReturn("Test")
        userDocs.currentPhotoType = PhotoType.ID

        userDocs.setUnsupportedFormat()

        assertThat(userDocs)
            .hasFieldOrPropertyWithValue("documentFrontErrorText", "Test")
            .hasFieldOrPropertyWithValue("documentFrontStatus", FieldStatus.INVALID)
    }

    @Test
    fun setDocBackStatusToInvalidWhenUnsupportedFormat() {
        whenever(stringProvider.provideString(eq(R.string.cexd_wrong_format))).thenReturn("Test")
        userDocs.currentPhotoType = PhotoType.ID_BACK

        userDocs.setUnsupportedFormat()

        assertThat(userDocs)
            .hasFieldOrPropertyWithValue("documentBackErrorText", "Test")
            .hasFieldOrPropertyWithValue("documentBackStatus", FieldStatus.INVALID)
    }

    @Test
    fun setSelfieStatusToInvalidWhenUnsupportedFormat() {
        whenever(stringProvider.provideString(eq(R.string.cexd_wrong_format))).thenReturn("Test")
        userDocs.currentPhotoType = PhotoType.SELFIE

        userDocs.setUnsupportedFormat()

        assertThat(userDocs)
            .hasFieldOrPropertyWithValue("selfieErrorText", "Test")
            .hasFieldOrPropertyWithValue("selfieStatus", FieldStatus.INVALID)
    }
}
