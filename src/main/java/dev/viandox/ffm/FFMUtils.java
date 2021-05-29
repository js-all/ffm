package dev.viandox.ffm;

import com.google.common.collect.Maps;

import java.util.Map;

public class FFMUtils {
    public static Map<String, Long> suffixes = Maps.newHashMap();
    static {
        suffixes.put("k", 1_000L);
        suffixes.put("M", 1_000_000L);
        suffixes.put("G", 1_000_000_000L);
        suffixes.put("T", 1_000_000_000_000L);
        suffixes.put("P", 1_000_000_000_000_000L);
        suffixes.put("E", 1_000_000_000_000_000_000L);
    }
    public static long parseSuffixedNumber(String str) {
        double numberPart = Double.parseDouble(str.replaceAll("[^0-9-.]", ""));
        Long fac = suffixes.get(str.replaceAll("[0-9-.,]", ""));
        System.out.println(fac + "   =>  '" + str.replaceAll("[0-9-.,]", "") + "'");
        fac = fac == null ? 1 : fac;
        return (long) (numberPart * fac);
    }
}
