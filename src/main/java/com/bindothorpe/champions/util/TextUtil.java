package com.bindothorpe.champions.util;

public class TextUtil {

    public static String enumToCamelCase(String enumName) {
        String[] parts = enumName.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(part.substring(0, 1).toUpperCase());
            builder.append(part.substring(1).toLowerCase());
        }
        return builder.toString();
    }

}
