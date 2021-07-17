package dev.viandox.ffm.mixin;

import com.google.common.collect.Maps;
import dev.viandox.ffm.config.Config;
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
    public void onPlayerListUpdate(PlayerListS2CPacket packet, CallbackInfo ci) {
        LocalDateTime currentRead = LocalDateTime.now();

        // skip if too short after last
        if(Duration.between(lastRead, currentRead).compareTo(Config.minDelayBetweenPlayerListScrapping) <= 0) return;
        // reset delay
        lastRead = currentRead;

        PlayerListScrapper.scrap(this.playerListEntries.values());
    }
}
