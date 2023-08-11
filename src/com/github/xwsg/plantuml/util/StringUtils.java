package com.github.xwsg.plantuml.util;

/**
 * String utils.
 *
 * @author xwsg
 */
public class StringUtils {

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }
}
