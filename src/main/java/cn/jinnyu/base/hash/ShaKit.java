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
package cn.jinnyu.base.hash;

import cn.jinnyu.base.codec.CodecKit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-28
 */
public enum ShaKit {

    ;

    public static final String SHA256   = "SHA-256";
    public static final String SHA512   = "SHA-512";
    public static final String SHA3_256 = "SHA3-256";
    public static final String SHA3_512 = "SHA3-512";

    public static String encode(String data, String methodOrKey) throws Exception {
        try {
            MessageDigest digest = MessageDigest.getInstance(methodOrKey);
            byte[]        bytes  = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return CodecKit.byte2hex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
