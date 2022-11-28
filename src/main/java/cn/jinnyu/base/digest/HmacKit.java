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
package cn.jinnyu.base.digest;

import cn.jinnyu.base.codec.CodecKit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-28
 */
public class HmacKit {

    public static final String HMAC_MD5     = "HmacMD5";
    public static final String HMAC_SHA_1   = "HmacSHA1";
    public static final String HMAC_SHA_224 = "HmacSHA224";
    public static final String HMAC_SHA_256 = "HmacSHA256";
    public static final String HMAC_SHA_384 = "HmacSHA384";
    public static final String HMAC_SHA_512 = "HmacSHA512";

    /**
     * 默认HmacSHA512
     *
     * @param data 待加密数据
     * @param key  加密密匙
     * @return 加密后的串
     */
    public String encode(String data, String key) throws Exception {
        return encode(HMAC_SHA_512, data, key);
    }

    public String encode(String method, String data, String key) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), method);
        Mac           mac        = Mac.getInstance(method);
        mac.init(signingKey);
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return CodecKit.byte2hex(bytes);
    }

}
