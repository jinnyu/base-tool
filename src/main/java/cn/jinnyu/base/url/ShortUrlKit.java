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

    public static String toHash(String raw) {
        return to62Bit(hashRawData(raw));
    }

    public static String toShort(String baseUrl, String raw) {
        return toShort(true, baseUrl, raw);
    }

    public static String toShort(boolean validUrl, String baseUrl, String raw) {
        if (validUrl) {
            if (!(isValidUrl(baseUrl) || isValidUrl(raw))) {
                throw new RuntimeException("Base url or input url incorrect!");
            }
        }
        int    hash    = hashRawData(raw);
        String shorted = to62Bit(hash);
        return formatShortUrl(baseUrl, shorted);
    }

    private static boolean isValidUrl(String s) {
        try {
            new URL(s);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static int hashRawData(String data) {
        return Hashing.murmur3_128().hashBytes(data.getBytes(StandardCharsets.UTF_8)).asInt();
    }

    private static String to62Bit(int uniqueId) {
        return CodecKit.toOtherSystemNumber(uniqueId, 62);
    }

    private static String formatShortUrl(String baseUrl, String shorted) {
        return baseUrl.endsWith("/") ? baseUrl + shorted : baseUrl + "/" + shorted;
    }

}
