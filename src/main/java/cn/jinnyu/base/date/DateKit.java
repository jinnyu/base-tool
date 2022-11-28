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
package cn.jinnyu.base.date;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author jinyu@jinnyu.cn
 * @date 2020-07-01
 */
@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public enum DateKit {

    ;

    public static final DateTimeFormatter YYMMDD                  = DateTimeFormatter.ofPattern("yyMMdd");
    public static final DateTimeFormatter YY_MM_DD                = DateTimeFormatter.ofPattern("yy-MM-dd");
    public static final DateTimeFormatter YYYYMMDD                = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter YYYY_MM_DD              = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter HHMMSS                  = DateTimeFormatter.ofPattern("HHmmss");
    public static final DateTimeFormatter HHMMSSSSS               = DateTimeFormatter.ofPattern("HHmmssSSS");
    public static final DateTimeFormatter HH_MM_SS                = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter HH_MM_SS_SSS            = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public static final DateTimeFormatter YYYYMMDDHHMMSS          = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter YYYYMMDDHHMMSSSSS       = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 日期

    public static String getYesterday() {
        return getDate(LocalDate.now().minusDays(1), YYYY_MM_DD);
    }

    public static String getYesterday(DateTimeFormatter formatter) {
        return getDate(LocalDate.now().minusDays(1), formatter);
    }

    public static String getToday() {
        return getDate(LocalDate.now(), YYYY_MM_DD);
    }

    public static String getToday(DateTimeFormatter formatter) {
        return getDate(LocalDate.now(), formatter);
    }

    public static String getDate(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    // 时间

    public static String getNowTime() {
        return getTime(LocalTime.now(), HH_MM_SS);
    }

    public static String getTime(DateTimeFormatter formatter) {
        return getTime(LocalTime.now(), formatter);
    }

    public static String getTime(LocalTime time, DateTimeFormatter formatter) {
        return time.format(formatter);
    }

    // 日期时间

    public static String getNowDateTime() {
        return getDateTime(LocalDateTime.now(), YYYY_MM_DD_HH_MM_SS);
    }

    public static String getNowDateTime(DateTimeFormatter formatter) {
        return getDateTime(LocalDateTime.now(), formatter);
    }

    public static String getDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    public static LocalDateTime fromString(String s, DateTimeFormatter formatter) {
        return LocalDateTime.parse(s, formatter);
    }

    public static String fromUnixEpoch(long ts) {
        return fromUnixEpoch(ts, YYYY_MM_DD_HH_MM_SS_SSS);
    }

    public static String fromUnixEpoch(long ts, DateTimeFormatter formatter) {
        Instant       instant = Instant.ofEpochMilli(ts);
        LocalDateTime ldt     = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ldt.format(formatter);
    }

}
