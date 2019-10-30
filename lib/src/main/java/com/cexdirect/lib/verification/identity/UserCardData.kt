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

import androidx.annotation.VisibleForTesting
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.cexdirect.lib.BR
import com.cexdirect.lib.network.models.CardData
import com.cexdirect.lib.network.models.IvsCardData
import com.cexdirect.lib.network.models.PssCardData
import com.cexdirect.lib.util.DH
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.binifyCardNumber
import com.cexdirect.lib.util.encodeToString
import com.google.gson.Gson
import com.mcxiaoke.koi.ext.trimAllWhitespace
import org.bouncycastle.util.encoders.Base64
import java.util.*
import java.util.regex.Pattern

class UserCardData(private val dh: DH) : BaseObservable() {

    private val gson = Gson()

    @get:Bindable
    var number = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.number)
        }

    @get:Bindable
    var numberStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.numberStatus)
        }

    @get:Bindable
    var expiry = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.expiry)
        }

    @get:Bindable
    var expiryStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.expiryStatus)
        }

    @get:Bindable
    var cvv = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.cvv)
        }

    @get:Bindable
    var cvvStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.cvvStatus)
        }

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (propertyId) {
                    BR.number -> numberStatus = validateCardNumber(number)
                    BR.expiry -> expiryStatus = validateCardExpiry(expiry)
                    BR.cvv -> cvvStatus = validateCardCvv(cvv)
                }
            }
        })
    }

    @VisibleForTesting
    internal fun validateCardCvv(cvv: String) =
        when {
            cvv.isEmpty() -> FieldStatus.EMPTY
            cvv.length < CVV_LENGTH -> FieldStatus.INVALID
            else -> FieldStatus.VALID
        }

    @VisibleForTesting
    internal fun validateCardExpiry(expiry: String) =
        when {
            expiry.isEmpty() -> FieldStatus.EMPTY
            expiry.matches(PATTERN_EXP_DATE) -> {
                val split = expiry.split("/")

                val month = split.first().toInt()
                val monthValid = month in 1..LAST_MONTH

                val yearValid =
                    split.last().toInt() >= Calendar
                        .getInstance()
                        .get(Calendar.YEAR)
                        .toString()
                        .takeLast(2)
                        .toInt()
                if (monthValid && yearValid) {
                    FieldStatus.VALID
                } else {
                    FieldStatus.INVALID
                }
            }
            else -> FieldStatus.INVALID
        }

    @VisibleForTesting
    internal fun validateCardNumber(cardNumber: String) =
        when {
            cardNumber.isEmpty() -> FieldStatus.EMPTY
            cardNumber.trimAllWhitespace().length == CARD_NUMBER_LENGTH -> FieldStatus.VALID
            else -> FieldStatus.INVALID
        }

    fun forceValidate() {
        if (numberStatus == FieldStatus.EMPTY) {
            numberStatus = FieldStatus.INVALID
        }

        if (expiryStatus == FieldStatus.EMPTY) {
            expiryStatus = FieldStatus.INVALID
        }

        if (cvvStatus == FieldStatus.EMPTY) {
            cvvStatus = FieldStatus.INVALID
        }
    }

    fun getCardBin() = number.binifyCardNumber()

    fun getPublicKey() = dh.publicKey.y.toByteArray().encodeToString()

    fun generateVerificationCardData(publicKey: String): CardData {
        val cardData = gson.toJson(IvsCardData(number.replace(" ", ""), expiry))
        return generateCardData(cardData, publicKey)
    }

    private fun generateCardData(
        cardData: String,
        publicKey: String
    ): CardData {
        val vector = dh.byteGenerator(GENERATOR_BYTES)
        val chash = dh.encrypt(
            dh.stringGenerator(GENERATOR_OFFSET),
            cardData,
            android.util.Base64.decode(publicKey, android.util.Base64.NO_WRAP),
            vector
        )
        return CardData(chash!!, Base64.toBase64String(vector))
    }

    fun generateProcessingCardData(publicKey: String): CardData {
        val cardData = gson.toJson(PssCardData(number.replace(" ", ""), cvv))
        return generateCardData(cardData, publicKey)
    }

    fun isValid() = numberStatus == FieldStatus.VALID &&
            expiryStatus == FieldStatus.VALID &&
            cvvStatus == FieldStatus.VALID

    companion object {
        private const val GENERATOR_BYTES = 16
        private const val GENERATOR_OFFSET = 5
        private const val CARD_NUMBER_LENGTH = 16
        private const val LAST_MONTH = 12
        private const val CVV_LENGTH = 3

        private val PATTERN_EXP_DATE = Pattern.compile("^(\\d{2}/\\d{2})$").toRegex()
    }
}
