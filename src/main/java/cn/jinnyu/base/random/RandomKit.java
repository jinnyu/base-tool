package cn.jinnyu.base.random;


import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jinyu@jinnyu.cn
 * @date 2022-11-28
 */
public enum RandomKit {

    ;

    private static final SecureRandom RANDOM;
    private static final String       BASE_INT    = "0123456789";
    private static final String       BASE_LETTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" + BASE_INT;

    static {
        try {
            RANDOM = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer getNumber(int len) {
        return getNumber(6, 1, 100);
    }

    public static Integer getNumber(int len, int min, int max) {
        return (int) (RANDOM.nextDouble() * (max - min) + min);
    }

    public static String getIntString(int len) {
        return IntStream.range(0, len).mapToObj(i -> String.valueOf(BASE_INT.charAt(RANDOM.nextInt(10)))).collect(Collectors.joining());
    }

    public static String getString(int len) {
        return IntStream.range(0, len).mapToObj(i -> String.valueOf(BASE_LETTER.charAt(RANDOM.nextInt(62)))).collect(Collectors.joining());
    }

}
