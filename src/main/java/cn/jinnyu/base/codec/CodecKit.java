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
