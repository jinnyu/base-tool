package cn.jinnyu.base.url;

import cn.jinnyu.base.codec.CodecKit;
import com.google.common.hash.Hashing;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author jinyu@jinnyu.cn
 * @date 2023-02-09
 */
public enum ShortUrlKit {

    ;

    public static String parse(String baseUrl, String raw) {
        if (isValidUrl(baseUrl) && isValidUrl(raw)) {
            long   hash    = hash(raw);
            String shorted = to62Bit(hash);
            return formatShortUrl(baseUrl, shorted);
        }
        throw new RuntimeException("Base url or input url incorrect!");
    }

    private static boolean isValidUrl(String s) {
        try {
            new URL(s);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static long hash(String data) {
        return Hashing.murmur3_128().hashBytes(data.getBytes(StandardCharsets.UTF_8)).asLong();
    }

    private static String to62Bit(long uniqueId) {
        return CodecKit.toOtherSystemNumber(uniqueId, 62);
    }

    private static String formatShortUrl(String baseUrl, String shorted) {
        return baseUrl.endsWith("/") ? baseUrl + shorted : baseUrl + "/" + shorted;
    }

}
