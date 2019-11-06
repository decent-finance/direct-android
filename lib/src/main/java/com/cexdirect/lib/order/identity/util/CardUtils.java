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

package com.cexdirect.lib.order.identity.util;

/**
 * Created on 2/22/18.
 *
 * @author Mykola Tychyna (iMykolaPro)
 */

public class CardUtils {

    public static final String ONLY_NUMBERS_REGEX = "[0-9]+";
    public static final String DATE_SLASH_REGEX = "[0-9/]+";

    public static boolean onlyNumbers(String s) {
        return s.matches(ONLY_NUMBERS_REGEX);
    }

    public static boolean dateWithSlash(String s) {
        return s.matches(DATE_SLASH_REGEX);
    }

    public static String getWithLeadingZeros(int number, int leadZeros) {
        StringBuilder result = new StringBuilder(String.valueOf(number));
        for (int i = 0; i < leadZeros; i++) result.insert(0, "0");
        return String.valueOf(result);
    }

    public static boolean luhnAlgorithmVerification(long cardNumber) {
        String temp = Long.toString(cardNumber);
        int[] numbers = new int[temp.length()];
        for (int i = 0; i < temp.length(); i++) {
            numbers[i] = temp.charAt(i) - '0';
        }
        return luhnAlgorithmVerification(numbers);
    }

    public static boolean luhnAlgorithmVerification(String cardNumber) {
        int[] numbers = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
            numbers[i] = cardNumber.charAt(i) - '0';
        }
        return luhnAlgorithmVerification(numbers);
    }

    public static boolean luhnAlgorithmVerification(int[] digits) {
        int sum = 0;
        int length = digits.length;
        for (int i = 0; i < length; i++) {

            // get digits in reverse order
            int digit = digits[length - i - 1];

            // every 2nd number multiply with 2
            if (i % 2 == 1) {
                digit *= 2;
            }
            sum += digit > 9 ? digit - 9 : digit;
        }
        return sum % 10 == 0;
    }
}
