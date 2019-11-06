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
import com.cexdirect.lib.order.BaseOrderFragment
import com.cexdirect.lib.order.OrderActivity
import com.cexdirect.lib.order.confirmation.ChangeEmailDialog
import com.cexdirect.lib.order.confirmation.PaymentConfirmationFragment
import com.cexdirect.lib.order.identity.CvvInfoDialog
import com.cexdirect.lib.order.identity.IdentityFragment
import com.cexdirect.lib.order.identity.PhotoSourceDialog
import com.cexdirect.lib.order.identity.country.BaseCountryPickerDialog
import com.cexdirect.lib.order.receipt.ReceiptFragment
import com.cexdirect.lib.order.scanner.QrScannerActivity
import dagger.Subcomponent

@Subcomponent(modules = [SharedStateModule::class, IdentityVmModule::class])
@IdentityScope
interface IdentitySubcomponent {

    fun inject(identityFragment: IdentityFragment)

    fun inject(baseVerificationFragment: BaseOrderFragment)

    fun inject(dialog: PhotoSourceDialog)

    fun inject(verificationActivity: OrderActivity)

    fun inject(paymentConfirmationFragment: PaymentConfirmationFragment)

    fun inject(finishFragment: ReceiptFragment)

    fun inject(changeEmailDialog: ChangeEmailDialog)

    fun inject(cvvInfoDialog: CvvInfoDialog)

    fun inject(countryPickerDialog: BaseCountryPickerDialog)

    fun inject(qrScannerActivity: QrScannerActivity)
}
