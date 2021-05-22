package dev.viandox.ffm;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameMode;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

public class PlayerListScrapper {
    // taken straight from PlayerListHud
    public static Ordering<PlayerListEntry> ENTRY_ORDERING = Ordering.from((Comparator)(new PlayerListScrapper.EntryOrderComparator()));

    // more to come, just need to define them and add to setFields
    public static Map<String, String> commissions = Maps.newHashMap();

    public static void setFields(Map<String, Map<String, String>> data) {
        if(data.containsKey("Commissions")) commissions = data.get("Commissions");
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
}
