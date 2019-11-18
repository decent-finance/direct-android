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

package com.cexdirect.lib.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mcxiaoke.koi.HASH;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DH {

    public KeyAgreement keyAgree;

    private BigInteger g;
    private BigInteger p;

    private PrivateKey privateKey;
    private DHPublicKey publicKey;

    private SecureRandom random;

    public DH() {
        try {
            random = new SecureRandom();

            Security.addProvider(new BouncyCastleProvider());
            keyAgree = KeyAgreement.getInstance("DH", "BC");

            String modp2048 = "3231700607131100730033891392642382824881794124114023911284200975140074170663" +
                    "43542226196894173635693471179017379097041917546058732091950288537589861856221532121" +
                    "75412514901774520270235796078236248884246189477587641105928646099411723245426622522" +
                    "19323054091903768052423551912567971587011700105805587765103886184728025797605490356" +
                    "97325615261670813393617995413364765591603683178967290731783845896806396719009772021" +
                    "94168647225871031411336429319536193471636533209717077448227988588565369208645296636" +
                    "07725026895550592836275112117409697299806841055435958486658329164213621823107899099" +
                    "9448652468262416972035911852507045361090559";

            g = BigInteger.valueOf(2L);
            p = new BigInteger(modp2048);

            DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g, 0);
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DiffieHellman");
            keyGen.initialize(dhParameterSpec);
            KeyPair keyPair = keyGen.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = (DHPublicKey) keyPair.getPublic();
            publicKey.getY().toByteArray();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void prepareToEncryption(byte[] publicKey) {
        try {
            keyAgree.init(privateKey);

            BigInteger bPubKeyBI = new BigInteger(1, publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
            PublicKey pubKey = keyFactory.generatePublic(new DHPublicKeySpec(bPubKeyBI, p, g));
            keyAgree.doPhase(pubKey, true);

        } catch (@NonNull InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
//			Logger.e(this, e);
        }

    }

    @Nullable
    public String encrypt(String random, String message, byte[] key, byte[] salt) {
        prepareToEncryption(key);

        byte[] secret = getKeyAgree().generateSecret();
        String secret64 = encode(secret);
        Log.w("SECRET", secret64);

        String c = random + message;

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(HASH.INSTANCE.sha256Bytes(secret64.getBytes()), "AES"), new IvParameterSpec(salt));
            byte[] cipherText = cipher.doFinal(c.getBytes(Charset.forName("UTF-8")));
            return encode(cipherText);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public String encrypt(byte[] message, byte[] key, byte[] salt) {
        prepareToEncryption(key);

        byte[] secret = getKeyAgree().generateSecret();
        String secret64 = encode(secret);
        Log.w("SECRET", secret64);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(HASH.INSTANCE.sha256Bytes(secret64.getBytes()), "AES"), new IvParameterSpec(salt));
            byte[] cipherText = cipher.doFinal(message);
            return encode(cipherText);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public String decrypt(String message, byte[] key, byte[] salt) {
        prepareToEncryption(key);

        byte[] secret = getKeyAgree().generateSecret();
        String secret64 = encode(secret);

        try {
            byte[] ivAndCipherText = decode(message);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(HASH.INSTANCE.sha256Bytes(secret64.getBytes()), "AES"), new IvParameterSpec(salt));
            String decrypted = new String(cipher.doFinal(ivAndCipherText), Charset.forName("UTF-8"));
            return decrypted.substring(5);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @NonNull
    public String generateRandomID() {
        return Integer.toString(new Random().nextInt() * 10) +
                Long.toString(System.currentTimeMillis());
    }

    public byte[] byteGenerator(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(String.format(Locale.UK, "%1d", random.nextInt(9)));
        }
        return builder.toString().getBytes();
    }

    @NonNull
    public String stringGenerator(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(String.format(Locale.UK, "%1d", random.nextInt(9)));
        }
        return builder.toString();
    }


    public DHPublicKey getPublicKey() {
        return publicKey;
    }

    private KeyAgreement getKeyAgree() {
        return keyAgree;
    }

    private static String encode(@NonNull byte[] bytes) {
        return Base64.toBase64String(bytes);
    }

    @Nullable
    private static byte[] decode(@Nullable String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.decode(base64);
    }

}
