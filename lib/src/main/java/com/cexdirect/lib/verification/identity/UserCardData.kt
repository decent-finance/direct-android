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

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.cexdirect.lib.BR
import com.cexdirect.lib._util.DH
import com.cexdirect.lib._util.binifyCardNumber
import com.cexdirect.lib._util.encodeToString
import com.cexdirect.lib.network.models.CardData
import com.cexdirect.lib.network.models.IvsCardData
import com.cexdirect.lib.network.models.PssCardData
import com.google.gson.Gson
import org.bouncycastle.util.encoders.Base64

class UserCardData(private val dh: DH) : BaseObservable() {

    private val gson = Gson()

    @get:Bindable
    var number = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.number)
        }

    @get:Bindable
    var expiry = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.expiry)
        }

    @get:Bindable
    var cvv = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.cvv)
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

    // TODO implement data validation
    fun isCardDataPresent() = number.isNotBlank() && expiry.isNotBlank() && cvv.isNotBlank()

    companion object {
        private const val GENERATOR_BYTES = 16
        private const val GENERATOR_OFFSET = 5
    }
}
