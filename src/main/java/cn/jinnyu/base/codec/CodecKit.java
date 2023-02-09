/*
 * Copyright (c) 2022, Jinnyu (jinyu@jinnyu.cn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.jinnyu.base.codec;

import java.util.stream.IntStream;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-28
 */
public enum CodecKit {

    ;


    /**
     * 在进制表示中的字符集合，0-Z分别用于表示最大为62进制的符号表示
     */
    // @formatter:off
    private static final char[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    // @formatter:on

    /**
     * 将十进制的数字转换为指定进制的字符串
     *
     * @param number 十进制数字
     * @param bit    进制数
     * @return 指定进制的字符串
     */
    public static String toOtherSystemNumber(long number, int bit) {
        if (number < 0) {
            number = ((long) 2 * 0x7fffffff) + number + 2;
        }
        char[] buf     = new char[32];
        int    charPos = 32;
        while ((number / bit) > 0) {
            buf[--charPos] = digits[(int) (number % bit)];
            number /= bit;
        }
        buf[--charPos] = digits[(int) (number % bit)];
        return new String(buf, charPos, (32 - charPos));
    }

    /**
     * 将其它进制的数字（字符串形式）转换为十进制的数字
     *
     * @param number 其它进制的数字（字符串形式）
     * @param bit    进制数
     * @return 十进制的数字
     */
    public static long toDecimalNumber(String number, int bit) {
        char[] charBuf = number.toCharArray();
        if (bit == 10) {
            return Long.parseLong(number);
        }
        long result = 0, base = 1;
        for (int i = charBuf.length - 1; i >= 0; i--) {
            int index = 0;
            for (int j = 0, length = digits.length; j < length; j++) {
                // 找到对应字符的下标, 对应的下标才是具体的数值
                if (digits[j] == charBuf[i]) {
                    index = j;
                }
            }
            result += index * base;
            base *= bit;
        }
        return result;
    }

    /**
     * 将2进制转换成16进制
     *
     * @param bytes byte数组
     * @return 16进制数据
     */
    public static String byte2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为2进制
     *
     * @param hex 16进制数据
     * @return 2进制数据
     */
    public static byte[] hex2byte(String hex) {
        if (hex.length() < 1) {
            return null;
        }
        byte[] result = new byte[hex.length() / 2];
        IntStream.range(0, hex.length() / 2).forEachOrdered(i -> {
            int high = Integer.parseInt(hex.substring(i * 2, i * 2 + 1), 16);
            int low  = Integer.parseInt(hex.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        });
        return result;
    }

}
