package com.lazuream.ai.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

/**
 * @author lazuream
 * @Description
 * @create 2026/3/4
 */
public class StringTools {

    private static final String DECIMAL_FORMAT = "0.00";

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static String getFileSuffix(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return suffix;
    }

    public static boolean pathIsOk(String path) {
        if (StringTools.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }

    public static BigDecimal convertYuan2fen4BigDecimal(BigDecimal amount) {
        BigDecimal fen = amount.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN);
        return fen;
    }

    public static Integer getRandomNumberRange(Integer min, Integer max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}
