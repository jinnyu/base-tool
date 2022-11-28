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
package cn.jinnyu.base.lang;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author jinyu@jinnyu.cn
 * @date 2020-07-29
 */
@SuppressWarnings("unused")
@Slf4j
public class LangKit {

    // --------------------------------------------------

    public static <T extends RuntimeException> void throwException(T e) {
        throw e;
    }

    // --------------------------------------------------
    // Object
    // --------------------------------------------------

    /**
     * 判断对象是否为<code>null</code>
     *
     * @param o 对象
     * @return true null / false !null
     */
    public static boolean isNull(Object o) {
        return Objects.isNull(o);
    }

    /**
     * 判断对象数组中任意元素是否为<code>null</code>
     *
     * @param args 对象数组
     * @return 任意元素为 <code>null</code> 返回 <code>true</code>, 否则返回<code>false</code>
     */
    public static boolean isAnyNull(Object... args) {
        if (isNull(args)) {
            return true;
        } else {
            return Arrays.stream(args).anyMatch(LangKit::isNull);
        }
    }

    /**
     * 如果对象为空则抛出<code>RuntimeException</code>异常
     *
     * @param o 对象
     * @param e 异常信息
     */
    public static void throwExceptionIfNull(Object o, String e) {
        if (isNull(o)) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends RuntimeException> void throwExceptionIfNull(Object o, T e) {
        if (isNull(o)) {
            throw e;
        }
    }

    // --------------------------------------------------
    // String
    // --------------------------------------------------

    /**
     * 判断字符串是否为空 null / "" / 长度为0
     *
     * @param s 字符串
     * @return null || "" || length() == 0 返回<code>true</code>, 否则返回<code>false</code>
     */
    public static boolean isEmpty(String s) {
        return isNull(s) || "".equals(s) || s.length() == 0;
    }

    /**
     * 判断字符串数组中任意元素是否为空 null / "" / 长度为0
     *
     * @param args 字符串数组
     * @return 任意元素 null || "" || length() == 0 返回<code>true</code>, 否则返回<code>false</code>
     */
    public static boolean isAnyEmpty(String... args) {
        if (isNull(args)) {
            return true;
        } else {
            return Arrays.stream(args).anyMatch(LangKit::isEmpty);
        }
    }

    /**
     * 如果字符串为空则抛出<code>RuntimeException</code>异常
     *
     * @param s 对象
     * @param e 异常信息
     */
    public static void throwExceptionIfEmpty(String s, String e) {
        if (isEmpty(s)) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends RuntimeException> void throwExceptionIfEmpty(String o, T e) {
        if (isNull(o)) {
            throw e;
        }
    }

    // --------------------------------------------------
    // Map
    // --------------------------------------------------

    /**
     * 线程安全判断Map是否为空
     *
     * @param map map对象
     * @return map对象为 <code>null</code>或者大小为0 返回<code>true</code>, 否则返回<code>false</code>
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 线程安全判断如果Map为空则抛出异常
     *
     * @param map 集合
     * @param e   异常信息
     */
    public static void throwExceptionIfEmpty(Map<?, ?> map, String e) {
        if (isEmpty(map)) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends RuntimeException> void throwExceptionIfEmpty(Map<?, ?> map, T e) {
        if (isEmpty(map)) {
            throw e;
        }
    }

    /**
     * 线程安全判断map是否含有指定key
     *
     * @param map 判断对象
     * @param key 判断key
     * @return true / false
     */
    public static boolean mapHasKey(Map<?, ?> map, Object key) {
        return !isEmpty(map) && map.containsKey(key);
    }

    /**
     * 线程安全判断map是否含有指定value
     *
     * @param map   判断对象
     * @param value 判断value
     * @return true / false
     */
    public static boolean mapHasValue(Map<?, ?> map, Object value) {
        return !isEmpty(map) && map.containsValue(value);
    }

    public static <K, V> Map<K, V> toLinkedHashMap(K key, V value) {
        return toLinkedHashMap(new LinkedHashMap<>(), key, value);
    }

    /**
     * 创建map
     *
     * @param map   map对象
     * @param key   存放key
     * @param value 存放value
     * @param <K>   泛型
     * @param <V>   泛型
     * @return map对象
     */
    public static <K, V> Map<K, V> toLinkedHashMap(Map<K, V> map, K key, V value) {
        if (isEmpty(map)) {
            map = new LinkedHashMap<>();
        }
        map.put(key, value);
        return map;
    }

    @SuppressWarnings("AlibabaCollectionInitShouldAssignCapacity")
    public static <K, V> Map<K, V> toHashMap(K key, V value) {
        return toHashMap(new HashMap<>(), key, value);
    }

    /**
     * 创建map
     *
     * @param map   map对象
     * @param key   存放key
     * @param value 存放value
     * @param <K>   泛型
     * @param <V>   泛型
     * @return map对象
     */
    @SuppressWarnings("AlibabaCollectionInitShouldAssignCapacity")
    public static <K, V> Map<K, V> toHashMap(Map<K, V> map, K key, V value) {
        if (isEmpty(map)) {
            map = new HashMap<>();
        }
        map.put(key, value);
        return map;
    }

    /**
     * 创建HashSet
     *
     * @param v   存放对象
     * @param <V> 泛型
     * @return hashset对象
     */
    @SafeVarargs
    public static <V> HashSet<V> toHashSet(V... v) {
        return toHashSet(new HashSet<>(), v);
    }

    /**
     * 创建HashSet
     *
     * @param set hashset对象
     * @param v   存放对象
     * @param <V> 泛型
     * @return hashset对象
     */
    @SafeVarargs
    public static <V> HashSet<V> toHashSet(HashSet<V> set, V... v) {
        if (isEmpty(set)) {
            set = new HashSet<>();
        }
        Collections.addAll(set, v);
        return set;
    }

    // --------------------------------------------------
    // Collection
    // --------------------------------------------------

    /**
     * 线程安全判断集合是否为空
     *
     * @param collection 集合
     * @return 集合为 <code>null</code>或者长度为0 返回<code>true</code>, 否则返回<code>false</code>
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * 线程安全判断如果集合为空则抛出异常
     *
     * @param collection 集合
     * @param e          异常信息
     */
    public static void throwExceptionIfEmpty(Collection<?> collection, String e) {
        if (isEmpty(collection)) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends RuntimeException> void throwExceptionIfEmpty(Collection<?> collection, T e) {
        if (isEmpty(collection)) {
            throw e;
        }
    }

    // --------------------------------------------------
    // List
    // --------------------------------------------------

    /**
     * 数组转ArrayList
     *
     * @param objs 元素
     * @param <T>  List泛型
     * @return List对象
     */
    @SafeVarargs
    public static <T> ArrayList<T> toArrayList(T... objs) {
        return (ArrayList<T>) toList(ArrayList.class, objs);
    }

    /**
     * 数组转LinkedList
     *
     * @param objs 元素
     * @param <T>  List泛型
     * @return List对象
     */
    @SafeVarargs
    public static <T> LinkedList<T> toLinkedList(T... objs) {
        return (LinkedList<T>) toList(LinkedList.class, objs);
    }

    @SafeVarargs
    private static <T> List<T> toList(Class<?> clazz, T... objs) {
        List<T> list = clazz.equals(ArrayList.class) ? new ArrayList<>() : new LinkedList<>();
        Collections.addAll(list, objs);
        return list;
    }

    // --------------------------------------------------

    /**
     * 猜编码
     *
     * @param value    要测试的字符串
     * @param charsets 被测试的编码
     * @return 猜出来的编码(未命中的时候回返回UTF8)
     */
    public static String guessCharset(String value, String[] charsets) {
        String probe = StandardCharsets.UTF_8.name();
        for (String c : charsets) {
            Charset charset = Charset.forName(c);
            try {
                String utf8  = convert(value, charset.name(), probe);
                String guess = convert(utf8, probe, charset.name());
                if (value.equals(guess)) {
                    return c;
                }
            } catch (UnsupportedEncodingException e) {
                log.error("不支持的编码 [{}]", c);
            }
        }
        return StandardCharsets.UTF_8.name();
    }

    private static String convert(String value, String fromEncoding, String toEncoding) throws UnsupportedEncodingException {
        return new String(value.getBytes(fromEncoding), toEncoding);
    }

    // --------------------------------------------------

}
