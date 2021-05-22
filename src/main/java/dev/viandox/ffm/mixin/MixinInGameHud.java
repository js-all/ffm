package dev.viandox.ffm.mixin;

import dev.viandox.ffm.ActionBarScrapper;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.PlayerListScrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @Shadow
    MinecraftClient client;

    @Shadow
    private Text overlayMessage;

    @Shadow
    abstract TextRenderer getFontRenderer();

    @Inject(
            method = "setOverlayMessage",
            at = @At("RETURN")
    )
    public void onActionBar(Text message, boolean tinted, CallbackInfo ci) {
        String str = message.getString();
        this.overlayMessage = Text.of(ActionBarScrapper.scrap(str));
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/SpectatorHud;render(Lnet/minecraft/client/util/math/MatrixStack;)V",
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    void injectInRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if(!this.client.options.hudHidden) {
            if(!PlayerListScrapper.commissions.isEmpty() && PlayerListScrapper.area.equals("Dwarven Mines")) {
                TextRenderer textRenderer = this.getFontRenderer();
                float m = 10f;
                float tofx = 20f;
                float ofx = -m;
                float ofy = 50f;
                float df = 10f;
                float w = textRenderer.getWidth("commissions: ");
                for(Map.Entry<String, String> entry : PlayerListScrapper.commissions.entrySet()) {
                    w = Math.max(w, textRenderer.getWidth(entry.getKey() + ": " + entry.getValue()));
                }
                w += tofx;
                FFMGraphicsHelper.drawRoundedRect(client,
                        ofx - m,
                        ofy - m,
                        ofx + w + m,
                        ofy + df + df * PlayerListScrapper.commissions.size() + m,
                        0,
                        128,
                        0,
                        0,
                        0,
                        m);

                textRenderer.draw(matrices, Text.of("commissions:"), ofx + tofx, ofy, 0xffffffff);
                float i = ofy + df;
                for (Map.Entry<String, String> entry : PlayerListScrapper.commissions.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    textRenderer.draw(matrices, Text.of(String.format("%s: %s", k, v)), ofx + tofx, i, 0xffcccccc);
                    i += df;
                }
            }
        }
    }
}
