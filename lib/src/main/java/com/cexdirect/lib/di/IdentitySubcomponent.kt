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

package com.cexdirect.lib.di

import com.cexdirect.lib.di.annotation.IdentityScope
import com.cexdirect.lib.verification.BaseVerificationFragment
import com.cexdirect.lib.verification.VerificationActivity
import com.cexdirect.lib.verification.confirmation.ChangeEmailDialog
import com.cexdirect.lib.verification.confirmation.PaymentConfirmationFragment
import com.cexdirect.lib.verification.identity.CvvInfoDialog
import com.cexdirect.lib.verification.identity.IdentityFragment
import com.cexdirect.lib.verification.identity.PhotoSourceDialog
import com.cexdirect.lib.verification.identity.country.BaseCountryPickerDialog
import com.cexdirect.lib.verification.receipt.ReceiptFragment
import com.cexdirect.lib.verification.scanner.QrScannerActivity
import dagger.Subcomponent

@Subcomponent(modules = [SharedStateModule::class, IdentityVmModule::class])
@IdentityScope
interface IdentitySubcomponent {

    fun inject(identityFragment: IdentityFragment)

    fun inject(baseVerificationFragment: BaseVerificationFragment)

    fun inject(dialog: PhotoSourceDialog)

    fun inject(verificationActivity: VerificationActivity)

    fun inject(paymentConfirmationFragment: PaymentConfirmationFragment)

    fun inject(finishFragment: ReceiptFragment)

    fun inject(changeEmailDialog: ChangeEmailDialog)

    fun inject(cvvInfoDialog: CvvInfoDialog)

    fun inject(countryPickerDialog: BaseCountryPickerDialog)

    fun inject(qrScannerActivity: QrScannerActivity)
}
