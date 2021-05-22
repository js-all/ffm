package dev.viandox.ffm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.Config;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.PlayerListScrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @Shadow
    MinecraftClient client;

    @Shadow
    abstract TextRenderer getFontRenderer();

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
            if(!PlayerListScrapper.commissions.isEmpty()) {
                TextRenderer textRenderer = this.getFontRenderer();
                //OrderedText.styledString("test", Style.EMPTY.withFont())
                textRenderer.draw(matrices, Text.of("commissions:"), 10.0f, 10.0f, 0xffff0000);
                float i = 10f;
                for (Map.Entry<String, String> entry : PlayerListScrapper.commissions.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    textRenderer.draw(matrices, Text.of(String.format("%s: %s", k, v)), 20.0f, 10.0f + i, 0xffffaa00);
                    i += 10f;
                }
            }
        }
    }
}
