package dev.viandox.ffm.gui;

import com.ibm.icu.impl.coll.Collation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.time.Instant;
import java.time.LocalDateTime;

public class FFMButtonWidget extends ButtonWidget {
    private boolean wasHovered = false;
    private Instant lastHoveredStateChange = Instant.now();

    public FFMButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    public FFMButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(wasHovered != this.isHovered()) {
            this.lastHoveredStateChange = Instant.now();
        }
        double now = Instant.now().toEpochMilli();
        double diff = now - lastHoveredStateChange.toEpochMilli();
        // how much of the transition are we at (0 -> 1)
        double f = Math.min(diff / Config.transitionDuration.toMillis(), 1);

        int color;
        if(this.isHovered()) {
            color = ColorConverter.RGBtoINT(ColorConverter.lerpRGB(
                    ColorConverter.INTtoRGB(Config.accentColor),
                    ColorConverter.INTtoRGB(Config.accentColorBright),
                    (float) f));
        } else {
            color = ColorConverter.RGBtoINT(ColorConverter.lerpRGB(
                    ColorConverter.INTtoRGB(Config.accentColorBright),
                    ColorConverter.INTtoRGB(Config.accentColor),
                    (float) f));
        }

        FFMGraphicsHelper.drawRoundedRect(matrices, x, y, x + width, y+ height, 0, color, 9);

        drawCenteredText(matrices,
                MinecraftClient.getInstance().textRenderer,
                this.getMessage(),
                this.x + this.width / 2,
                this.y + (this.height - 8) / 2,
                0xffffffff);

        wasHovered = this.isHovered();
    }
}
