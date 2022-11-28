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
package cn.jinnyu.base.totp;

import java.util.Arrays;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-29
 */
public class Base32 {

    protected static final char[] chars32 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9' };

    private static int char2int(char in) {
        in = Character.toLowerCase(in);
        switch (in) {
            case 'a':
                return 0;
            case 'b':
                return 1;
            case 'c':
                return 2;
            case 'd':
                return 3;
            case 'e':
                return 4;
            case 'f':
                return 5;
            case 'g':
                return 6;
            case 'h':
                return 7;
            case 'j':
                return 8;
            case 'k':
                return 9;
            case 'l':
                return 10;
            case 'm':
                return 11;
            case 'n':
                return 12;
            case 'p':
                return 13;
            case 'q':
                return 14;
            case 'r':
                return 15;
            case 's':
                return 16;
            case 't':
                return 17;
            case 'u':
                return 18;
            case 'v':
                return 19;
            case 'w':
                return 20;
            case 'x':
                return 21;
            case 'y':
                return 22;
            case 'z':
                return 23;
            case '2':
                return 24;
            case '3':
                return 25;
            case '4':
                return 26;
            case '5':
                return 27;
            case '6':
                return 28;
            case '7':
                return 29;
            case '8':
                return 30;
            case '9':
                return 31;
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            case 'i':
            case 'o':
        }
        return -1;
    }

    /**
     * @param input 明文内容
     * @return 编码后内容
     */
    public static String encode(byte[] input) {
        byte[] binary = input.length % 5 == 0 ? input : Arrays.copyOf(input, input.length + 5 - input.length % 5);
        int    cursor = 0;

        char[] result = new char[binary.length / 5 * 8];
        while ((cursor + 1) * 5 <= binary.length) {
            long pack = (((long) binary[cursor * 5] << 32) & 0x000000ff00000000L) | (((long) binary[cursor * 5 + 1] << 24) & 0x00000000ff000000L) | (((long) binary[cursor * 5 + 2] << 16) & 0x0000000000ff0000L) | (((long) binary[cursor * 5 + 3] << 8) & 0x000000000000ff00l) | ((long) binary[cursor * 5 + 4] & 0x00000000000000ffl);
            result[cursor * 8] = chars32[(int) (pack >>> 35) & 0x0000001f];
            result[cursor * 8 + 1] = chars32[(int) (pack >>> 30) & 0x0000001f];
            result[cursor * 8 + 2] = chars32[(int) (pack >>> 25) & 0x0000001f];
            result[cursor * 8 + 3] = chars32[(int) (pack >>> 20) & 0x0000001f];
            result[cursor * 8 + 4] = chars32[(int) (pack >>> 15) & 0x0000001f];
            result[cursor * 8 + 5] = chars32[(int) (pack >>> 10) & 0x0000001f];
            result[cursor * 8 + 6] = chars32[(int) (pack >>> 5) & 0x0000001f];
            result[cursor * 8 + 7] = chars32[(int) pack & 0x0000001f];
            cursor++;
        }

        switch (input.length % 5) {
            case 0:
                return new String(result);
            case 1:
                return new String(Arrays.copyOf(result, result.length - 6));
            case 2:
                return new String(Arrays.copyOf(result, result.length - 5));
            case 3:
                return new String(Arrays.copyOf(result, result.length - 3));
            case 4:
                return new String(Arrays.copyOf(result, result.length - 1));
        }
        return null;
    }

    /**
     * @param encoded 编码后内容
     * @return 明文内容
     */
    public static byte[] decode(String encoded) {
        String input  = encoded.length() % 8 == 0 ? encoded : encoded + "0000000".substring(0, 8 - encoded.length() % 8);
        int    cursor = 0;
        byte[] result = new byte[input.length() / 8 * 5];
        while (input.length() > cursor * 8) {
            result[cursor * 5] = (byte) ((char2int(input.charAt(cursor * 8)) << 3 & 0x000000f8) | (char2int(input.charAt(cursor * 8 + 1)) >>> 2 & 0x00000007));
            result[cursor * 5 + 1] = (byte) ((char2int(input.charAt(cursor * 8 + 1)) << 6 & 0x000000c0) | (char2int(input.charAt(cursor * 8 + 2)) << 1 & 0x00000003e) | (char2int(input.charAt(cursor * 8 + 3)) >>> 4 & 0x00000001));
            result[cursor * 5 + 2] = (byte) ((char2int(input.charAt(cursor * 8 + 3)) << 4 & 0x000000f0) | (char2int(input.charAt(cursor * 8 + 4)) >>> 1 & 0x0000000f));
            result[cursor * 5 + 3] = (byte) ((char2int(input.charAt(cursor * 8 + 4)) << 7 & 0x00000080) | (char2int(input.charAt(cursor * 8 + 5)) << 2 & 0x00000007c) | (char2int(input.charAt(cursor * 8 + 6)) >>> 3 & 0x00000003));
            result[cursor * 5 + 4] = (byte) ((char2int(input.charAt(cursor * 8 + 6)) << 5 & 0x000000e0) | (char2int(input.charAt(cursor * 8 + 7)) & 0x0000001f));
            cursor++;
        }
        switch (encoded.length() % 8) {
            case 0:
                return result;
            case 1:
            case 4:
            case 6:
                return null;
            case 2:
                return Arrays.copyOf(result, result.length - 4);
            case 3:
                return Arrays.copyOf(result, result.length - 3);
            case 5:
                return Arrays.copyOf(result, result.length - 2);
            case 7:
                return Arrays.copyOf(result, result.length - 1);
        }
        return null;

    }

}
