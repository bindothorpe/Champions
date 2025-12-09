package com.bindothorpe.champions.domain.build;

import java.util.HashMap;
import java.util.Map;

public class ClassTypeHealthValues {

    private static final Map<ClassType, Double> maxHealthMap = new HashMap<>();

    public static double getMaxHealthOfClass(ClassType classType) {
        if(maxHealthMap.isEmpty()) populateMap();

        if(classType == null) {
            return 20.0D;
        }

        return maxHealthMap.get(classType);
    }


    private static void populateMap() {
        maxHealthMap.put(ClassType.ASSASSIN, 31.0D);
        maxHealthMap.put(ClassType.KNIGHT, 50.0D);
        maxHealthMap.put(ClassType.BRUTE, 50.0D);
        maxHealthMap.put(ClassType.MAGE, 40.0D);
        maxHealthMap.put(ClassType.RANGER, 37.0D);
    }
}
