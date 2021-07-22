package dev.viandox.ffm;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PlayerListScrapper {
    // taken straight from PlayerListHud
    public static Ordering<PlayerListEntry> ENTRY_ORDERING = Ordering.from((Comparator)(new PlayerListScrapper.EntryOrderComparator()));

    // more to come, just need to define them and add to setFields
    public static Map<String, String> commissions = Maps.newHashMap();
    public static String area = null;
    public static int speed = 100;
    public static boolean hasCommissions = false;
    // not accurate value, parsed from suffixed number
    public static long bank = 0;

    public static void setFields(Map<String, Map<String, String>> data) {
//        try {
//            PrintWriter writer = null;
//            writer = new PrintWriter("player_list_scrapper_"+data.get("Area").get("__value")+"_log", "UTF-8");
//
//            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
//                writer.println(entry.getKey() + ":");
//                for(Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
//                    writer.println("    " + entry1.getKey() + ": " + entry1.getValue());
//                }
//            }
//
//            writer.close();
//        } catch (FileNotFoundException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        if(data.containsKey("Profile")) bank = FFMUtils.parseSuffixedNumber(data.get("Profile").get("Bank"));
        if(data.containsKey("Skills")) speed = Integer.parseInt(data.get("Skills").get("Speed").replaceAll("[^0-9]", ""));
        if(data.containsKey("Commissions")) commissions = data.get("Commissions");
        hasCommissions = data.containsKey("Commissions");
        if(data.containsKey("Area")) area = data.get("Area").get("__value");
    }

    // also taken from PlayerListHud
    public static class EntryOrderComparator implements Comparator<PlayerListEntry> {
        private EntryOrderComparator() {
        }

        public int compare(PlayerListEntry playerListEntry, PlayerListEntry playerListEntry2) {
            Team team = playerListEntry.getScoreboardTeam();
            Team team2 = playerListEntry2.getScoreboardTeam();
            return ComparisonChain.start().compareTrueFirst(playerListEntry.getGameMode() != GameMode.SPECTATOR, playerListEntry2.getGameMode() != GameMode.SPECTATOR).compare((Comparable)(team != null ? team.getName() : ""), (Comparable)(team2 != null ? team2.getName() : "")).compare(playerListEntry.getProfile().getName(), playerListEntry2.getProfile().getName(), String::compareToIgnoreCase).result();
        }
    }

    private static Text getPlayerName(PlayerListEntry entry) {
        return entry.getDisplayName() != null ? entry.getDisplayName().shallowCopy() : Team.modifyText(entry.getScoreboardTeam(), new LiteralText(entry.getProfile().getName()));
    }

    public static void scrap(Collection<PlayerListEntry> entries) {
        // sort the entries the same way they are in the GUI
        List<PlayerListEntry> playerList = PlayerListScrapper.ENTRY_ORDERING.sortedCopy(entries);

        List<String> nameList = new ArrayList<>(new LinkedHashSet<>( // remove duplicates, keep the same order
                playerList.stream().map((v) -> getPlayerName(v).getString()).collect(Collectors.toList()) // extract just the displayed names
        ));
        int serverInfoIndex = nameList.indexOf("       Server Info");

        // if no server info, there isn't anything to scrap (or at least we won't bother checking)
        if(serverInfoIndex == -1) return;

        // remove anything before the "Server Info" entry, as that is just players names and garbage
        nameList = nameList.subList(serverInfoIndex + 1, nameList.size() - 1);


        AtomicReference<String> lastKey = new AtomicReference<>("");
        // actual data stored in format example:
        // Map: {
        //      key: "Commissions": Map: {
        //          key: "a commission": "50%"
        //      }
        // }
        Map<String, Map<String, String>> data = Maps.newHashMap();

        nameList.forEach(v -> {
            // skip a useless entry
            if(v.startsWith("   ")) return;

            if(!v.startsWith(" ")) { // then this will be a key (+ some data maybe)
                int colonIndex = v.indexOf(":");
                String key = colonIndex == -1 ? v.trim() : v.substring(0, colonIndex).trim();
                data.put(key, Maps.newHashMap());
                lastKey.set(key);
                if(colonIndex != -1) { // if there is a colon
                    data.get(key).put("__value", v.substring(colonIndex + 1, v.length()).trim());
                }
            } else {
                int separatorIndex = !v.contains(":") ? v.indexOf(")") : v.indexOf(":"); // get the first index of : or )
                if(separatorIndex != -1) {
                    String key = v.substring(0, separatorIndex).trim();
                    data.get(lastKey.get()).put(key, v.substring(separatorIndex + 1, v.length()).trim());
                } else {  // no separator (almost never)
                    String key = v.trim();
                    data.get(lastKey.get()).put(key, null);
                }
            }
        });

        PlayerListScrapper.setFields(data);
    }
}
