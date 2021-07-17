package dev.viandox.ffm.gui;

import com.ibm.icu.impl.coll.Collation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.ColorConverter;
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

        int ca = color >> 24;
        int cr = color >> 16 & 0xff;
        int cg = color >> 8 & 0xff;
        int cb = color & 0xff;

        // buttons will be rendered like so (each square is a different quad)
        //    ╭──┬───────────────────────┬──╮
        //    ├──┤                       ├──┤
        //    ├──┤                       ├──┤
        //    ╰──┴───────────────────────┴──╯

        MinecraftClient client = MinecraftClient.getInstance();
        client.getTextureManager().bindTexture(new Identifier("ffm", "corner.png"));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        // cs = cornerSize
        float cs = 8;
        float z = 0;
        int w = x + width;
        int h = y + height;
        Matrix4f matrix = matrices.peek().getModel();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        // top left corner
        bufferBuilder.vertex(matrix, x + cs, y +  0, 0).color(cr, cg, cb, ca).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, x +  0, y +  0, 0).color(cr, cg, cb, ca).texture(0.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, x +  0, y + cs, 0).color(cr, cg, cb, ca).texture(0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, x + cs, y + cs, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();
        // top right corner
        bufferBuilder.vertex(matrix, w +  0, y +  0, 0).color(cr, cg, cb, ca).texture(0.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, w - cs, y +  0, 0).color(cr, cg, cb, ca).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, w - cs, y + cs, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, w +  0, y + cs, 0).color(cr, cg, cb, ca).texture(0.0f, 1.0f).next();
        // bottom left corner
        bufferBuilder.vertex(matrix, x + cs, h - cs, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, x +  0, h - cs, 0).color(cr, cg, cb, ca).texture(0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, x +  0, h +  0, 0).color(cr, cg, cb, ca).texture(0.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, x + cs, h +  0, 0).color(cr, cg, cb, ca).texture(1.0f, 0.0f).next();
        // bottom right corner
        bufferBuilder.vertex(matrix, w +  0, h - cs, 0).color(cr, cg, cb, ca).texture(0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, w - cs, h - cs, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, w - cs, h +  0, 0).color(cr, cg, cb, ca).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, w +  0, h +  0, 0).color(cr, cg, cb, ca).texture(0.0f, 0.0f).next();
        // left middle piece                                                              0.9 here to get a solid color
        bufferBuilder.vertex(matrix, x + cs, y + cs, 0).color(cr, cg, cb, ca).texture(1.0f, 0.9f).next();
        bufferBuilder.vertex(matrix, x +  0, y + cs, 0).color(cr, cg, cb, ca).texture(0.9f, 0.9f).next();
        bufferBuilder.vertex(matrix, x +  0, h - cs, 0).color(cr, cg, cb, ca).texture(0.9f, 1.0f).next();
        bufferBuilder.vertex(matrix, x + cs, h - cs, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();
        // right middle piece
        bufferBuilder.vertex(matrix, w +  0, y + cs, 0).color(cr, cg, cb, ca).texture(1.0f, 0.9f).next();
        bufferBuilder.vertex(matrix, w - cs, y + cs, 0).color(cr, cg, cb, ca).texture(0.9f, 0.9f).next();
        bufferBuilder.vertex(matrix, w - cs, h - cs, 0).color(cr, cg, cb, ca).texture(0.9f, 1.0f).next();
        bufferBuilder.vertex(matrix, w +  0, h - cs, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();
        // body
        bufferBuilder.vertex(matrix, w - cs, y +  0, 0).color(cr, cg, cb, ca).texture(1.0f, 0.9f).next();
        bufferBuilder.vertex(matrix, x + cs, y +  0, 0).color(cr, cg, cb, ca).texture(0.9f, 0.9f).next();
        bufferBuilder.vertex(matrix, x + cs, h +  0, 0).color(cr, cg, cb, ca).texture(0.9f, 1.0f).next();
        bufferBuilder.vertex(matrix, w - cs, h +  0, 0).color(cr, cg, cb, ca).texture(1.0f, 1.0f).next();

        bufferBuilder.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        BufferRenderer.draw(bufferBuilder);

        drawCenteredText(matrices,
                client.textRenderer,
                this.getMessage(),
                this.x + this.width / 2,
                this.y + (this.height - 8) / 2,
                0xffffffff);

        wasHovered = this.isHovered();
    }
}
