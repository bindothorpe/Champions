package com.bindothorpe.champions.util;

import org.bukkit.util.Vector;

public class TextUtil {

    public static String camelCasing(String enumText) {
        String[] words = enumText.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(word.substring(0, 1).toUpperCase());
            sb.append(word.substring(1).toLowerCase());
            sb.append(" ");
        }
        return sb.toString().trim();
    }

}
