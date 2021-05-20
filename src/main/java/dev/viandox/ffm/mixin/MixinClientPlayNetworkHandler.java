package dev.viandox.ffm.mixin;

import com.google.common.collect.Maps;
import dev.viandox.ffm.Config;
import dev.viandox.ffm.PlayerListScrapper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Shadow
    Map<UUID, PlayerListEntry> playerListEntries = Maps.newHashMap();

    private static LocalDateTime lastRead = LocalDateTime.MIN;

    public Text getPlayerName(PlayerListEntry entry) {
        return entry.getDisplayName() != null ? entry.getDisplayName().shallowCopy() : Team.modifyText(entry.getScoreboardTeam(), new LiteralText(entry.getProfile().getName()));
    }

    @Inject(
            method = "onPlayerList",
            at = @At("RETURN")
    )
    public void tickCallback(PlayerListS2CPacket packet, CallbackInfo ci) {
        LocalDateTime currentRead = LocalDateTime.now();

        // skip if too short after last
        if(Duration.between(lastRead, currentRead).compareTo(Config.minDelayBetweenPlayerListScrapping) <= 0) return;
        // reset delay
        lastRead = currentRead;

        List<PlayerListEntry> playerList = PlayerListScrapper.ENTRY_ORDERING.sortedCopy(this.playerListEntries.values());
        // extract only the displayed name and remove duplicated while keeping the order
        List<String> nameList = new ArrayList<>(new LinkedHashSet<>(
                playerList.stream().map((v) -> getPlayerName(v).getString()).collect(Collectors.toList())
        ));
        int serverInfoIndex = nameList.indexOf("       Server Info");

        // if no server info, there isn't anything to scrap (or at least we won't bother checking)
        if(serverInfoIndex == -1) return;

        // remove player names
        nameList = nameList.subList(serverInfoIndex + 1, nameList.size() - 1);

        AtomicReference<String> lastKey = new AtomicReference<>("");
        Map<String, Map<String, String>> data = Maps.newHashMap();

        nameList.forEach(v -> {
            // only applies for the first Account Info thing that we don't care about
            if(v.startsWith("   ")) return;
            if(!v.startsWith(" ")) { // then this will be a key (+ some data maybe)
                int colonIndex = v.indexOf(":");
                String key = colonIndex == -1 ? v.trim() : v.substring(0, colonIndex).trim();
                data.put(key, Maps.newHashMap());
                lastKey.set(key);
                if(colonIndex != -1) {
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
