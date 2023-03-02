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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-28
 */
public enum RsaKit {

    ;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String encrypt(String data, String key, Object other) throws Exception {
        return encode(data, str2PublicKey(key));
    }

    public static String decrypt(String data, String key, Object other) throws Exception {
        return decode(data, str2PrivateKey(key));
    }

    public static final  String PUBLIC       = "public-key";
    public static final  String PRIVATE      = "private-key";
    public static final  String SIGN_TYPE    = "SHA256withRSA";
    private static final int    KEY_SIZE_MIN = 1024;
    private static final int    KEY_SIZE_MAX = 65536;
    private static final int    DOUBLE_OF_64 = 64;

    /**
     * 生成公匙和私匙
     *
     * @param keySize key大小 1024-65536 & 64的倍数
     * @return { "public-key": RSAPublicKey对象, "private-key": RSAPrivateKey对象 }
     */
    public Map<String, Key> getKeys(int keySize) {
        try {
            // 1024-65536 & 64的倍数
            if (keySize < KEY_SIZE_MIN || keySize > KEY_SIZE_MAX || keySize % DOUBLE_OF_64 != 0) {
                throw new IllegalArgumentException("keySize is between 1024-65536 and a multiple of 64!");
            }
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize, RANDOM);
            KeyPair          keyPair    = keyPairGenerator.generateKeyPair();
            RSAPublicKey     publicKey  = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey    privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, Key> map        = new HashMap<>(4);
            map.put(PUBLIC, publicKey);
            map.put(PRIVATE, privateKey);
            return map;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String key2Str(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static RSAPublicKey str2PublicKey(String key) {
        try {
            byte[]             keyBytes   = Base64.getDecoder().decode(key.getBytes());
            X509EncodedKeySpec keySpec    = new X509EncodedKeySpec(keyBytes);
            KeyFactory         keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPrivateKey str2PrivateKey(String key) {
        try {
            byte[]              keyBytes   = Base64.getDecoder().decode(key.getBytes());
            PKCS8EncodedKeySpec keySpec    = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory          keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加密
     *
     * @param source    待加密内容
     * @param publicKey 公匙
     * @return 加密后的内容
     */
    private static String encode(String source, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] bytes = codec(cipher, Cipher.ENCRYPT_MODE, source.getBytes(StandardCharsets.UTF_8), publicKey.getModulus().bitLength());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     *
     * @param source     待解密内容
     * @param privateKey 私匙
     * @return 解密后的内容
     */
    private static String decode(String source, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bytes = codec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(source.getBytes(StandardCharsets.UTF_8)), privateKey.getModulus().bitLength());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] codec(Cipher cipher, int mode, byte[] data, int keySize) {
        int    maxBlock = mode == Cipher.ENCRYPT_MODE ? keySize / 8 - 11 : keySize / 8;
        int    offset   = 0, i = 0;
        byte[] cache;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while (data.length > offset) {
                if (data.length - offset > maxBlock) {
                    cache = cipher.doFinal(data, offset, maxBlock);
                } else {
                    cache = cipher.doFinal(data, offset, data.length - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * maxBlock;
            }
            return out.toByteArray();
        } catch (BadPaddingException | IllegalBlockSizeException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String createBase64Sign(String source, PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(createSignByte(source, privateKey));
    }

    public byte[] createSignBytes(String source, PrivateKey privateKey) {
        return createSignByte(source, privateKey);
    }

    /**
     * 创建签名
     *
     * @param source     明文信息
     * @param privateKey 私匙
     * @return 签名
     */
    public byte[] createSignByte(String source, PrivateKey privateKey) {
        try {
            Signature signet = Signature.getInstance(SIGN_TYPE);
            signet.initSign(privateKey);
            signet.update(source.getBytes(StandardCharsets.UTF_8));
            return signet.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkBase64Sign(String source, String sign, PublicKey publicKey) {
        return checkSignBytes(source, Base64.getDecoder().decode(sign), publicKey);
    }

    /**
     * 检查签名
     *
     * @param source    明文信息
     * @param sign      根据本地信息生成的签名
     * @param publicKey 公匙
     * @return 是否匹配
     */
    public boolean checkSignBytes(String source, byte[] sign, PublicKey publicKey) {
        try {
            Signature signetCheck = Signature.getInstance(SIGN_TYPE);
            signetCheck.initVerify(publicKey);
            signetCheck.update(source.getBytes(StandardCharsets.UTF_8));
            return signetCheck.verify(sign);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
