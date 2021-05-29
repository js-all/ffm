package dev.viandox.ffm;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ActionBarScrapper {
    public static int defense = 0;

    public static int mana = 100;
    public static int maxMana = 100;

    public static int health = 100;
    public static int maxHealth = 100;

    public static int charges = 0;
    public static int maxCharges = 5;
    public static LocalDateTime lastCharges = LocalDateTime.MIN;
    public static boolean holdingChargesItem = false;
    private static boolean lastHadCharges = false;

    public static int abilityUseManaCost = 0;
    public static String abilityUseName = "";
    public static LocalDateTime lastAbilityUse = LocalDateTime.MIN;
    private static boolean lastHadAbilityUse = false;

    public static int drillFuel = 0;
    public static int maxDrillFuel = 0;
    public static boolean holdingDrill = false;

    public static String scrap(String str) {
        String[] infos = str.split(" {3,}");
        ArrayList<String> infosLeft = new ArrayList<>();

        boolean hasCharges = false;
        boolean hasAbilityUse = false;
        boolean hasDrillFuel = false;
        // fucking love regex
        for(int i = 0; i < infos.length; i++) {
            String d = infos[i].replaceAll("§[a-z0-9]", "");
            String org = infos[i];
            if(d.contains("Defense")) {
                defense = Integer.parseInt(d.replaceAll("[^0-9]", ""));
            } else if(d.contains("✎")) {
                String[] clean = d.replaceAll("[^0-9/]", "").split("/");
                mana = Integer.parseInt(clean[0]);
                maxMana = Integer.parseInt(clean[1]);
            } else if(d.contains("❤")) {
                String[] clean = d.replaceAll("[^0-9/]", "").split("/");
                health = Integer.parseInt(clean[0]);
                maxHealth = Integer.parseInt(clean[1]);
            } else if(d.contains("ⓩ") || d.contains("Ⓞ")) {
                hasCharges = true;
                // we only keep the full charges characters
                String fullCharges = org.replaceAll(
                        // the 2 ones in the square brackets should be
                        // ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ
                        "^.*§e§l([ⓩⓄ]*)§.*$",
                        "$1"
                );
                charges = fullCharges.length();
                maxCharges = d.length();

                if(!lastHadCharges) lastCharges = LocalDateTime.now();
            } else if(d.matches("^-[0-9]+ Mana \\(.+\\)$")) {
                hasAbilityUse = true;

                abilityUseName = d.replaceAll("^-[0-9]+ Mana \\((.+)\\)$", "$1");
                abilityUseManaCost = Integer.parseInt(d.replaceAll("[^0-9]", ""));

                if(!lastHadAbilityUse) lastAbilityUse = LocalDateTime.now();
            } else if(d.contains("Drill Fuel")) {
                hasDrillFuel = true;

                String[] clean = d.replaceAll(",", "").replaceAll("^.*?([0-9.,]+/[0-9.,][kMGTPE]?).*$", "$1").split("/");

                drillFuel = (int) FFMUtils.parseSuffixedNumber(clean[0]);
                maxDrillFuel = (int) FFMUtils.parseSuffixedNumber(clean[1]);
            } else {
                // we don't know what it is, just feed it back onto InGameHud
                infosLeft.add(infos[i]);
            }
        }

        lastHadCharges = hasCharges;
        holdingChargesItem = hasCharges;
        lastHadAbilityUse = hasAbilityUse;
        holdingDrill = hasDrillFuel;

        return String.join("    ", infosLeft);
    }
}
