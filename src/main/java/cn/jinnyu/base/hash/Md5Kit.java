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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-28
 */
public enum Md5Kit {

    ;

    private static final MessageDigest INSTANCE;

    static {
        try {
            INSTANCE = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(String data) {
        if (null == data) {
            return null;
        }
        byte[] bytes = INSTANCE.digest(data.getBytes());
        return CodecKit.byte2hex(bytes);
    }

}
