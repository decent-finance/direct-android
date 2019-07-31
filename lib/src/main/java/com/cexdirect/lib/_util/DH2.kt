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

package com.cexdirect.lib._util

import android.util.Base64
import com.mcxiaoke.koi.HASH
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.*
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.DHParameterSpec
import javax.crypto.spec.DHPublicKeySpec
import kotlin.properties.Delegates

class DH2(private val encryptor: Encryptor, val generator: SecureRandom) {

    var p: BigInteger by Delegates.notNull()
    var g: BigInteger by Delegates.notNull()

    val keyAgree by lazy { KeyAgreement.getInstance("DH", "BC") }
    val factory by lazy { KeyFactory.getInstance("DH") }
    val keyPair: KeyPair by lazy {
        Security.addProvider(BouncyCastleProvider())

        val modp2048 = "3231700607131100730033891392642382824881794124114023911284200975140074170663" +
                "43542226196894173635693471179017379097041917546058732091950288537589861856221532121" +
                "75412514901774520270235796078236248884246189477587641105928646099411723245426622522" +
                "19323054091903768052423551912567971587011700105805587765103886184728025797605490356" +
                "97325615261670813393617995413364765591603683178967290731783845896806396719009772021" +
                "94168647225871031411336429319536193471636533209717077448227988588565369208645296636" +
                "07725026895550592836275112117409697299806841055435958486658329164213621823107899099" +
                "9448652468262416972035911852507045361090559"


        g = BigInteger.valueOf(2L)
        p = BigInteger(modp2048)

        val dhParameterSpec = DHParameterSpec(p, g, 0)
        val keyGen = KeyPairGenerator.getInstance("DiffieHellman")
        keyGen.initialize(dhParameterSpec)
        keyGen.generateKeyPair()
    }

    val publicKey by lazy {
        val publicKey = keyPair.public as DHPublicKey
        publicKey.y.toByteArray()
    }

    val privateKey = keyPair.private

    fun prepareForEncryption(public: String) {
        keyAgree.init(privateKey)
//        val bPubKeyBytes = Base64.decode(public.toByteArray(), Base64.DEFAULT)
        val bPubKeyBytes = org.bouncycastle.util.encoders.Base64.decode(public.toByteArray())

        val bPubKeyBI = BigInteger(1, bPubKeyBytes)
        val keyFactory = KeyFactory.getInstance("DiffieHellman")
        val pubKey = keyFactory.generatePublic(DHPublicKeySpec(bPubKeyBI, p, g))
        keyAgree.doPhase(pubKey, true)
    }

    fun encrypt(message: ByteArray, sharedSecretBytes: ByteArray, key: ByteArray): ByteArray = encryptor.encrypt(
        byteGenerator(5) + message,
        HASH.sha256Bytes(sharedSecretBytes),
        key
    )

    fun generateRandomID() = (Math.random() * 10).toString() + System.currentTimeMillis().toString()

    // TODO refactoring needed
    fun byteGenerator(length: Int): ByteArray {
        val array = ArrayList<String>(length)
        for (i in 0..length - 1) {
            array.add(generator.nextInt(9).toString())
        }
        return array
            .reduce(String::plus)
            .toByteArray()
    }
}

fun ByteArray.encodeToString(): String = Base64.encodeToString(this, Base64.NO_WRAP)
