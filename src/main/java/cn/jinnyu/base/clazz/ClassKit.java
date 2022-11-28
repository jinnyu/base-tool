package cn.jinnyu.base.clazz;

/**
 * @author jinyu@jinnyu.cn
 * @date 2021-11-26
 */
public class ClassKit {

    public static boolean isClassPresent(String name) {
        try {
            Thread.currentThread().getContextClassLoader().loadClass(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
