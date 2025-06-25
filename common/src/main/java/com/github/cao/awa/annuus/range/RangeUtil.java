package com.github.cao.awa.annuus.range;

public class RangeUtil {
    public static boolean isIn(int min, int max, int value) {
        return value >= min && value <= max;
    }
}
