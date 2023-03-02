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
package cn.jinnyu.base.crypto;

import cn.jinnyu.base.codec.CodecKit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-25
 */
@SuppressWarnings("unchecked")
public enum AesKit {

    ;

    public enum ActionMode {
        ENCRYPT, DECRYPT
    }

    public enum IvMode {
        BASE64, HEX
    }

    public static final String IV      = "iv";
    public static final String IV_MODE = "iv-mode";

    /**
     * 生成偏移向量
     *
     * @param seed   随机数种子
     * @param length 数组长度
     * @return 生成的向量
     */
    public static String iv(String seed, int length, IvMode mode) {
        SecureRandom random = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
        byte[]       bytes  = new byte[length];
        random.nextBytes(bytes);
        return IvMode.BASE64.equals(mode) ? Base64.getEncoder().encodeToString(bytes) : CodecKit.byte2hex(bytes);
    }

    public static String encrypt(String data, String key, Map<String, Object> iv) throws Exception {
        if (null == data || null == key) {
            throw new IllegalArgumentException("data or key can not be null!");
        }
        return doAes(data, ActionMode.ENCRYPT, key, (String) iv.get(IV), (IvMode) iv.get(IV_MODE));
    }

    public static String decrypt(String data, String key, Object other) throws Exception {
        if (null == data || null == key) {
            throw new IllegalArgumentException("data or key can not be null!");
        }
        Map<String, Object> map = (Map<String, Object>) other;
        return doAes(data, ActionMode.DECRYPT, key, (String) map.get(IV), (IvMode) map.get(IV_MODE));
    }

    private static byte[] decodeIv(String iv, IvMode mode) {
        return IvMode.BASE64.equals(mode) ? Base64.getDecoder().decode(iv) : CodecKit.hex2byte(iv);
    }

    private static SecretKey initSecretKey(String key) throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(key.getBytes(StandardCharsets.UTF_8));
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        instance.init(256, random);
        return instance.generateKey();
    }

    private static Cipher initAesCipher(ActionMode mode, SecretKey secretKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        // 固定为 AES/GCM/NoPadding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        // @formatter:off
        cipher.init(
                ActionMode.ENCRYPT.equals(mode) ? 1 : 2,
                // 密匙
                new SecretKeySpec(secretKey.getEncoded(), "AES"),
                // GCM模式参数
                new GCMParameterSpec(128, iv)
        );
        // @formatter:on
        return cipher;
    }

    private static String doAes(String data, ActionMode mode, String key, String iv, IvMode ivMode) throws Exception {
        SecretKey secretKey = initSecretKey(key);
        Cipher    cipher    = initAesCipher(mode, secretKey, decodeIv(iv, ivMode));
        byte[]    result;
        if (ActionMode.ENCRYPT.equals(mode)) {
            // 加密
            result = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return CodecKit.byte2hex(result);
        } else {
            // 解密
            result = cipher.doFinal(CodecKit.hex2byte(data));
            return new String(result, StandardCharsets.UTF_8);
        }
    }

}
