package dev.viandox.ffm;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ActionBarScrapper {
    private static int defense = 0;

    private static int mana = 100;
    private static int maxMana = 100;

    private static int health = 100;
    private static int maxHealth = 100;

    private static int charges = -1;

    private static int abilityUseManaCost = -1;
    private static String abilityUseName = "";

    public static String scrap(String str) {
        String[] infos = str.split(" {3,}");
        ArrayList<String> infosLeft = new ArrayList<>();
        boolean hasCharges = false;
        boolean hasAbility = false;
        for(int i = 0; i < infos.length; i++) {
            String d = infos[i].replaceAll("§[a-z0-9]", "");
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
                String fullCharges = d.replaceAll("[^Ⓞ]", "");
                // we only keep the full charge chars, so the count of them is the number left
                charges = fullCharges.length();
            } else if(d.matches("^-[0-9]+ Mana \\(.+\\)$")) {
                abilityUseName = d.replaceAll("^-[0-9]+ Mana \\((.+)\\)$", "$1");
                abilityUseManaCost = Integer.parseInt(d.replaceAll("[^0-9]", ""));
                hasAbility = true;
            } else {
                System.out.println(d);
                infosLeft.add(infos[i]);
            }
        }

        if(!hasCharges) {
            charges = -1;
        }
        if(!hasAbility) {
            abilityUseManaCost = -1;
            abilityUseName = "";
        }

        return String.join("    ", infosLeft);
    }
}
