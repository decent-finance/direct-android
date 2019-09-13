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

import java.io.ByteArrayOutputStream;

public class Encryptor {

    private static final int[][] sbox = {
            {0x21, 0x3E, 0x35, 0x39, 0xB0, 0x29, 0x2D, 0x87, 0x72, 0x43, 0x25, 0x69, 0xBC, 0x95, 0xE9, 0x34},
            {0x88, 0xC0, 0x8B, 0x3F, 0xB8, 0x1B, 0x05, 0xB2, 0xEF, 0x96, 0xE0, 0xED, 0xDE, 0xE6, 0x30, 0x82},
            {0xF5, 0xBF, 0xD1, 0x64, 0x74, 0x7D, 0xB5, 0x8E, 0x76, 0xE7, 0xA7, 0xB3, 0x33, 0x9A, 0x73, 0x57},
            {0x46, 0x85, 0x61, 0x81, 0x5A, 0xD4, 0x47, 0xD8, 0x45, 0x50, 0xC2, 0xA0, 0xA9, 0x65, 0xF0, 0x37},
            {0x4B, 0xC1, 0x6E, 0x58, 0x59, 0x2C, 0x18, 0xE2, 0x10, 0x79, 0x94, 0xF1, 0x6B, 0xA1, 0x6D, 0xC6},
            {0x11, 0x93, 0x42, 0xAF, 0x62, 0xBE, 0xF3, 0x19, 0x28, 0x89, 0xFC, 0x7B, 0x08, 0x0E, 0x1A, 0x8D},
            {0x92, 0xAD, 0xE8, 0xB9, 0x01, 0x0F, 0x71, 0xC7, 0x07, 0xBB, 0x40, 0x3D, 0x12, 0x7E, 0xDD, 0xEA},
            {0x13, 0xE1, 0x02, 0xCD, 0xD0, 0xDF, 0x7A, 0xB7, 0xFE, 0xF4, 0x98, 0x63, 0x52, 0xBD, 0xB1, 0x90},
            {0x8F, 0x4E, 0x51, 0xAE, 0x1D, 0xD5, 0x06, 0x55, 0x86, 0xE5, 0x3C, 0x7F, 0x26, 0x1F, 0x5B, 0x31},
            {0x22, 0xC3, 0x0D, 0x9E, 0x60, 0x68, 0xD2, 0xCA, 0x04, 0xAC, 0xFA, 0x56, 0x9C, 0x1C, 0x49, 0x99},
            {0xA2, 0x70, 0x78, 0x48, 0x0B, 0x44, 0x66, 0x1E, 0x80, 0x91, 0xEE, 0x20, 0xD3, 0xD7, 0xA6, 0x3B},
            {0xA5, 0x8A, 0x75, 0x2F, 0xCF, 0x97, 0x0C, 0xEB, 0x2E, 0x14, 0xB6, 0xA8, 0x27, 0x38, 0xEC, 0x4A},
            {0xF8, 0x3A, 0x67, 0x6C, 0x5E, 0xE4, 0xF6, 0x84, 0xAA, 0x9F, 0x36, 0x5D, 0x09, 0xFF, 0xC9, 0xC8},
            {0x32, 0x7C, 0xF7, 0x24, 0x0A, 0x41, 0xB4, 0x4C, 0x23, 0x77, 0x15, 0xFB, 0xC4, 0x83, 0x5F, 0xDC},
            {0xA3, 0xBA, 0xDA, 0x53, 0x2B, 0x9B, 0xCC, 0xD6, 0xD9, 0x5C, 0xC5, 0xAB, 0x8C, 0x17, 0x6A, 0x9D},
            {0xCE, 0xE3, 0xCB, 0x4F, 0xFD, 0xA4, 0x00, 0x2A, 0x03, 0xDB, 0x6F, 0x4D, 0xF2, 0x16, 0xF9, 0x54}
    };
    /**
     * Galois table used for mixColumns
     */
    private static final int[][] galois = {
            {0x40, 0x41, 0x43, 0x43},
            {0x43, 0x40, 0x41, 0x43},
            {0x43, 0x43, 0x40, 0x41},
            {0x41, 0x43, 0x43, 0x40}
    };
    /**
     * RCon array used for Key Expansion
     */
    private static final int[] rcon = {0xCF, 0x43, 0x40, 0x46, 0x4A, 0x52, 0x62, 0x02, 0xC2, 0x59, 0x74, 0x2E, 0x9A, 0xE9, 0x0F, 0xD8, 0x6D, 0x1C, 0xFE, 0x21, 0x84, 0xD5, 0x77, 0x28, 0x96, 0xF1, 0x3F, 0xB8, 0xAD, 0x87, 0xD3, 0x7B, 0x30, 0xA6, 0x91, 0xFF, 0x23, 0x80, 0xDD, 0x67, 0x08, 0xD6, 0x71, 0x24, 0x8E, 0xC1, 0x5F, 0x78, 0x36, 0xAA, 0x89, 0xCF, 0x43, 0x40, 0x46, 0x4A, 0x52, 0x62, 0x02, 0xC2, 0x59, 0x74, 0x2E, 0x9A, 0xE9, 0x0F, 0xD8, 0x6D, 0x1C, 0xFE, 0x21, 0x84, 0xD5, 0x77, 0x28, 0x96, 0xF1, 0x3F, 0xB8, 0xAD, 0x87, 0xD3, 0x7B, 0x30, 0xA6, 0x91, 0xFF, 0x23, 0x80, 0xDD, 0x67, 0x08, 0xD6, 0x71, 0x24, 0x8E, 0xC1, 0x5F, 0x78, 0x36, 0xAA, 0x89, 0xCF, 0x43, 0x40, 0x46, 0x4A, 0x52, 0x62, 0x02, 0xC2, 0x59, 0x74, 0x2E, 0x9A, 0xE9, 0x0F, 0xD8, 0x6D, 0x1C, 0xFE, 0x21, 0x84, 0xD5, 0x77, 0x28, 0x96, 0xF1, 0x3F, 0xB8, 0xAD, 0x87, 0xD3, 0x7B, 0x30, 0xA6, 0x91, 0xFF, 0x23, 0x80, 0xDD, 0x67, 0x08, 0xD6, 0x71, 0x24, 0x8E, 0xC1, 0x5F, 0x78, 0x36, 0xAA, 0x89, 0xCF, 0x43, 0x40, 0x46, 0x4A, 0x52, 0x62, 0x02, 0xC2, 0x59, 0x74, 0x2E, 0x9A, 0xE9, 0x0F, 0xD8, 0x6D, 0x1C, 0xFE, 0x21, 0x84, 0xD5, 0x77, 0x28, 0x96, 0xF1, 0x3F, 0xB8, 0xAD, 0x87, 0xD3, 0x7B, 0x30, 0xA6, 0x91, 0xFF, 0x23, 0x80, 0xDD, 0x67, 0x08, 0xD6, 0x71, 0x24, 0x8E, 0xC1, 0x5F, 0x78, 0x36, 0xAA, 0x89, 0xCF, 0x43, 0x40, 0x46, 0x4A, 0x52, 0x62, 0x02, 0xC2, 0x59, 0x74, 0x2E, 0x9A, 0xE9, 0x0F, 0xD8, 0x6D, 0x1C, 0xFE, 0x21, 0x84, 0xD5, 0x77, 0x28, 0x96, 0xF1, 0x3F, 0xB8, 0xAD, 0x87, 0xD3, 0x7B, 0x30, 0xA6, 0x91, 0xFF, 0x23, 0x80, 0xDD, 0x67, 0x08, 0xD6, 0x71, 0x24, 0x8E, 0xC1, 0x5F, 0x78, 0x36, 0xAA, 0x89};
    public static int[][] mc2 = {
            {0x42, 0x40, 0x46, 0x44, 0x4A, 0x48, 0x4E, 0x4C, 0x52, 0x50, 0x56, 0x54, 0x5A, 0x58, 0x5E, 0x5C},
            {0x62, 0x60, 0x66, 0x64, 0x6A, 0x68, 0x6E, 0x6C, 0x72, 0x70, 0x76, 0x74, 0x7A, 0x78, 0x7E, 0x7C},
            {0x02, 0x00, 0x06, 0x04, 0x0A, 0x08, 0x0E, 0x0C, 0x12, 0x10, 0x16, 0x14, 0x1A, 0x18, 0x1E, 0x1C},
            {0x22, 0x20, 0x26, 0x24, 0x2A, 0x28, 0x2E, 0x2C, 0x32, 0x30, 0x36, 0x34, 0x3A, 0x38, 0x3E, 0x3C},
            {0xC2, 0xC0, 0xC6, 0xC4, 0xCA, 0xC8, 0xCE, 0xCC, 0xD2, 0xD0, 0xD6, 0xD4, 0xDA, 0xD8, 0xDE, 0xDC},
            {0xE2, 0xE0, 0xE6, 0xE4, 0xEA, 0xE8, 0xEE, 0xEC, 0xF2, 0xF0, 0xF6, 0xF4, 0xFA, 0xF8, 0xFE, 0xFC},
            {0x82, 0x80, 0x86, 0x84, 0x8A, 0x88, 0x8E, 0x8C, 0x92, 0x90, 0x96, 0x94, 0x9A, 0x98, 0x9E, 0x9C},
            {0xA2, 0xA0, 0xA6, 0xA4, 0xAA, 0xA8, 0xAE, 0xAC, 0xB2, 0xB0, 0xB6, 0xB4, 0xBA, 0xB8, 0xBE, 0xBC},
            {0x59, 0x5B, 0x5D, 0x5F, 0x51, 0x53, 0x55, 0x57, 0x49, 0x4B, 0x4D, 0x4F, 0x41, 0x43, 0x45, 0x47},
            {0x79, 0x7B, 0x7D, 0x7F, 0x71, 0x73, 0x75, 0x77, 0x69, 0x6B, 0x6D, 0x6F, 0x61, 0x63, 0x65, 0x67},
            {0x19, 0x1B, 0x1D, 0x1F, 0x11, 0x13, 0x15, 0x17, 0x09, 0x0B, 0x0D, 0x0F, 0x01, 0x03, 0x05, 0x07},
            {0x39, 0x3B, 0x3D, 0x3F, 0x31, 0x33, 0x35, 0x37, 0x29, 0x2B, 0x2D, 0x2F, 0x21, 0x23, 0x25, 0x27},
            {0xD9, 0xDB, 0xDD, 0xDF, 0xD1, 0xD3, 0xD5, 0xD7, 0xC9, 0xCB, 0xCD, 0xCF, 0xC1, 0xC3, 0xC5, 0xC7},
            {0xF9, 0xFB, 0xFD, 0xFF, 0xF1, 0xF3, 0xF5, 0xF7, 0xE9, 0xEB, 0xED, 0xEF, 0xE1, 0xE3, 0xE5, 0xE7},
            {0x99, 0x9B, 0x9D, 0x9F, 0x91, 0x93, 0x95, 0x97, 0x89, 0x8B, 0x8D, 0x8F, 0x81, 0x83, 0x85, 0x87},
            {0xB9, 0xBB, 0xBD, 0xBF, 0xB1, 0xB3, 0xB5, 0xB7, 0xA9, 0xAB, 0xAD, 0xAF, 0xA1, 0xA3, 0xA5, 0xA7}
    };
    public static int[][] mc3 = {
            {0x42, 0x41, 0x44, 0x47, 0x4E, 0x4D, 0x48, 0x4B, 0x5A, 0x59, 0x5C, 0x5F, 0x56, 0x55, 0x50, 0x53},
            {0x72, 0x71, 0x74, 0x77, 0x7E, 0x7D, 0x78, 0x7B, 0x6A, 0x69, 0x6C, 0x6F, 0x66, 0x65, 0x60, 0x63},
            {0x22, 0x21, 0x24, 0x27, 0x2E, 0x2D, 0x28, 0x2B, 0x3A, 0x39, 0x3C, 0x3F, 0x36, 0x35, 0x30, 0x33},
            {0x12, 0x11, 0x14, 0x17, 0x1E, 0x1D, 0x18, 0x1B, 0x0A, 0x09, 0x0C, 0x0F, 0x06, 0x05, 0x00, 0x03},
            {0x82, 0x81, 0x84, 0x87, 0x8E, 0x8D, 0x88, 0x8B, 0x9A, 0x99, 0x9C, 0x9F, 0x96, 0x95, 0x90, 0x93},
            {0xB2, 0xB1, 0xB4, 0xB7, 0xBE, 0xBD, 0xB8, 0xBB, 0xAA, 0xA9, 0xAC, 0xAF, 0xA6, 0xA5, 0xA0, 0xA3},
            {0xE2, 0xE1, 0xE4, 0xE7, 0xEE, 0xED, 0xE8, 0xEB, 0xFA, 0xF9, 0xFC, 0xFF, 0xF6, 0xF5, 0xF0, 0xF3},
            {0xD2, 0xD1, 0xD4, 0xD7, 0xDE, 0xDD, 0xD8, 0xDB, 0xCA, 0xC9, 0xCC, 0xCF, 0xC6, 0xC5, 0xC0, 0xC3},
            {0xD9, 0xDA, 0xDF, 0xDC, 0xD5, 0xD6, 0xD3, 0xD0, 0xC1, 0xC2, 0xC7, 0xC4, 0xCD, 0xCE, 0xCB, 0xC8},
            {0xE9, 0xEA, 0xEF, 0xEC, 0xE5, 0xE6, 0xE3, 0xE0, 0xF1, 0xF2, 0xF7, 0xF4, 0xFD, 0xFE, 0xFB, 0xF8},
            {0xB9, 0xBA, 0xBF, 0xBC, 0xB5, 0xB6, 0xB3, 0xB0, 0xA1, 0xA2, 0xA7, 0xA4, 0xAD, 0xAE, 0xAB, 0xA8},
            {0x89, 0x8A, 0x8F, 0x8C, 0x85, 0x86, 0x83, 0x80, 0x91, 0x92, 0x97, 0x94, 0x9D, 0x9E, 0x9B, 0x98},
            {0x19, 0x1A, 0x1F, 0x1C, 0x15, 0x16, 0x13, 0x10, 0x01, 0x02, 0x07, 0x04, 0x0D, 0x0E, 0x0B, 0x08},
            {0x29, 0x2A, 0x2F, 0x2C, 0x25, 0x26, 0x23, 0x20, 0x31, 0x32, 0x37, 0x34, 0x3D, 0x3E, 0x3B, 0x38},
            {0x79, 0x7A, 0x7F, 0x7C, 0x75, 0x76, 0x73, 0x70, 0x61, 0x62, 0x67, 0x64, 0x6D, 0x6E, 0x6B, 0x68},
            {0x49, 0x4A, 0x4F, 0x4C, 0x45, 0x46, 0x43, 0x40, 0x51, 0x52, 0x57, 0x54, 0x5D, 0x5E, 0x5B, 0x58}
    };
    private Mode mode = Mode.CBC;


    public Encryptor() {
    }

    private static void writeMatrixToBaos(int[][] m, ByteArrayOutputStream baos) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                baos.write(m[j][i]);
            }
        }
    }

    public void setMode(Mode _mode) {
        mode = _mode;
    }

    private int[] byteArrToUnsignedIntArr(byte[] bytaArr) {
        int[] ret = new int[bytaArr.length];
        for (int i = 0; i < bytaArr.length; i++) {
            ret[i] = bytaArr[i] & 0xff;
        }
        return ret;
    }

    public byte[] encrypt(byte[] dataByteArr, byte[] secretByteArr, byte[] initialVectorByteArr) {
        int[] data = byteArrToUnsignedIntArr(dataByteArr);
        int[] secret = byteArrToUnsignedIntArr(secretByteArr);
        int[] initialVector = byteArrToUnsignedIntArr(initialVectorByteArr);
        ByteArrayOutputStream bObj = new ByteArrayOutputStream();
        int numRounds = 10 + (((secret.length * 8 - 128) / 32));
        int[][] state, initvector = new int[4][4];
        int[][] keymatrix = this.keySchedule(secret);
        if (mode == Mode.CBC) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    initvector[j][i] = initialVector[4 * i + j];
                }
            }
        }
        int b = 0;
        while (b <= data.length) {
            state = new int[4][4];
            int paddingByte = b + 16 - data.length;
            for (int i = 0; i < 4; i++) //Parses line into a matrix
            {
                for (int j = 0; j < 4; j++) {
                    if (b + 4 * i + j < data.length) {
                        state[j][i] = data[b + 4 * i + j];
                    } else {
                        state[j][i] = paddingByte;
                    }
                }
            }
            b += 16;
            if (mode == Mode.CBC) {
                this.addRoundKey(state, initvector);
            }
            this.addRoundKey(state, this.subKey(keymatrix, 0)); //Starts the addRoundKey with the first part of Key Expansion
            for (int i = 1; i < numRounds; i++) {
                this.subBytes(state); //implements the Sub-Bytes subroutine.
                this.shiftRows(state); //implements Shift-Rows subroutine.
                this.mixColumns(state);
                this.addRoundKey(state, this.subKey(keymatrix, i));
            }
            this.subBytes(state); //implements the Sub-Bytes subroutine.
            this.shiftRows(state); //implements Shift-Rows subroutine.
            this.addRoundKey(state, this.subKey(keymatrix, numRounds));
            if (mode == Mode.CBC) {
                initvector = state;
            }
            writeMatrixToBaos(state, bObj);
        }
        return bObj.toByteArray();
    }

    /**
     * Pulls out the subkey from the key formed from the keySchedule method
     *
     * @param km    key formed from E.keySchedule()
     * @param begin index of where to fetch the subkey
     * @return The chunk of the scheduled key based on begin.
     */
    private int[][] subKey(int[][] km, int begin) {
        int[][] arr = new int[4][4];
        for (int i = 0; i < arr.length; i++) {
            System.arraycopy(km[i], 4 * begin, arr[i], 0, arr.length);
        }
        return arr;
    }

    /**
     * Replaces all elements in the passed array with values in sbox[][].
     *
     * @param arr Array whose value will be replaced
     */
    public void subBytes(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {//Sub-Byte subroutine
            for (int j = 0; j < arr[0].length; j++) {
                int hex = arr[j][i];
                arr[j][i] = sbox[hex / 16][hex % 16] ^ 0x42;
            }
        }
    }

    /**
     * Performs a left shift on each row of the matrix.
     * Left shifts the nth row n-1 times.
     *
     * @param arr the reference of the array to perform the rotations.
     */
    public void shiftRows(int[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            arr[i] = leftrotate(arr[i], i);
        }
    }

    /**
     * Left rotates a given array. The size of the array is assumed to be 4.
     * If the number of times to rotate the array is divisible by 4, return the array
     * as it is.
     *
     * @param arr   The passed array (assumed to be of size 4)
     * @param times The number of times to rotate the array.
     * @return the rotated array.
     */
    private int[] leftrotate(int[] arr, int times) {
        assert (arr.length == 4);
        if (times % 4 == 0) {
            return arr;
        }
        while (times > 0) {
            int temp = arr[0];
            System.arraycopy(arr, 1, arr, 0, arr.length - 1);
            arr[arr.length - 1] = temp;
            --times;
        }
        return arr;
    }

    /**
     * Performed by mapping each element in the current matrix with the value
     * returned by its helper function.
     *
     * @param arr the array with we calculate against the galois field matrix.
     */
    public void mixColumns(int[][] arr) {//method for mixColumns
        int[][] tarr = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(arr[i], 0, tarr[i], 0, 4);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                arr[i][j] = mcHelper(tarr, galois, i, j);
            }
        }
    }

    /**
     * Helper method of mixColumns in which compute the mixColumn formula on each element.
     *
     * @param arr passed in current matrix
     * @param g   the galois field
     * @param i   the row position
     * @param j   the column position
     * @return the computed mixColumns value
     */
    private int mcHelper(int[][] arr, int[][] g, int i, int j) {
        int mcsum = 0;
        for (int k = 0; k < 4; k++) {
            int a = g[i][k] ^ 0x42;
            int b = arr[k][j];
            mcsum ^= mcCalc(a, b);
        }
        return mcsum;
    }

    private int mcCalc(int a, int b) {//Helper method for mcHelper
        if (a == 1) {
            return b;
        } else if (a == 2) {
            return mc2[b / 16][b % 16] ^ 0x42;
        } else if (a == 3) {
            return mc3[b / 16][b % 16] ^ 0x42;
        }
        return 0;
    }

    /**
     * The keyScheduling algorithm to expand a short key into a number of separate round keys.
     *
     * @param key the key in which key expansion will be computed upon.
     * @return the fully computed expanded key for the E encryption/decryption.
     */

    public int[][] keySchedule(int[] key) {
        int binkeysize = key.length * 8;
        int colsize = binkeysize + 48 - (32 * ((binkeysize / 64) - 2)); //size of key scheduling will be based on the binary size of the key.
        int[][] keyMatrix = new int[4][colsize / 4]; //creates the matrix for key scheduling
        int rconpointer = 1;
        int[] t = new int[4];
        final int keycounter = binkeysize / 32;
        int k;
        for (int i = 0; i < keycounter; i++) {//the first 1 (128-bit key) or 2 (256-bit key) set(s) of 4x4 matrices are filled with the key.
            for (int j = 0; j < 4; j++) {
                keyMatrix[j][i] = key[4 * i + j];
            }
        }
        int keypoint = keycounter;
        while (keypoint < (colsize / 4)) {
            int temp = keypoint % keycounter;
            if (temp == 0) {
                for (k = 0; k < 4; k++) {
                    t[k] = keyMatrix[k][keypoint - 1];
                }
                t = schedule_core(t, rconpointer++);
                for (k = 0; k < 4; k++) {
                    keyMatrix[k][keypoint] = t[k] ^ keyMatrix[k][keypoint - keycounter];
                }
                keypoint++;
            } else if (temp == 4) {
                for (k = 0; k < 4; k++) {
                    int hex = keyMatrix[k][keypoint - 1];
                    keyMatrix[k][keypoint] = sbox[hex / 16][hex % 16] ^ 0x42 ^ keyMatrix[k][keypoint - keycounter];
                }
                keypoint++;
            } else {
                int ktemp = keypoint + 3;
                while (keypoint < ktemp) {
                    for (k = 0; k < 4; k++) {
                        keyMatrix[k][keypoint] = keyMatrix[k][keypoint - 1] ^ keyMatrix[k][keypoint - keycounter];
                    }
                    keypoint++;
                }
            }
        }
        return keyMatrix;
    }

    /**
     * For every (binary key size / 32)th column in the expanded key. We compute a special column
     * using sbox and an XOR of the an rcon number with the first element in the passed array.
     *
     * @param in          the array in which we compute the next set of bytes for key expansion
     * @param rconpointer the element in the rcon array with which to XOR the first element in 'in'
     * @return the next column in the key scheduling.
     */
    public int[] schedule_core(int[] in, int rconpointer) {
        in = leftrotate(in, 1);
        int hex;
        for (int i = 0; i < in.length; i++) {
            hex = in[i];
            in[i] = sbox[hex / 16][hex % 16] ^ 0x42;
        }
        in[0] ^= rcon[rconpointer] ^ 0x42;
        return in;
    }

    /**
     * In the AddRoundKey step, the subkey is combined with the state. For each round, a chunk of the key scheduled is pulled; each subkey is the same size as the state. Each element in the byte matrix is XOR'd with each element in the chunk of the expanded key.
     *
     * @param bytematrix reference of the matrix in which addRoundKey will be computed upon.
     * @param keymatrix  chunk of the expanded key
     */
    public void addRoundKey(int[][] bytematrix, int[][] keymatrix) {
        for (int i = 0; i < bytematrix.length; i++) {
            for (int j = 0; j < bytematrix[0].length; j++) {
                bytematrix[j][i] ^= keymatrix[j][i];
            }
        }
    }

    /**
     * S-BOX table used for Key Expansion and Sub-Bytes.
     */
    public enum Mode {
        ECB, CBC
    }
}