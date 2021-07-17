package dev.viandox.ffm;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class FastMenu extends DrawableHelper {
    private static boolean open;
    private static MinecraftClient client;
    private static int innerCircleSize = 100;

    public static Map<String, String> getItems() {
        return Config.fastMenuEntries;
    }

    public static void open() {
        client = MinecraftClient.getInstance();
        client.mouse.unlockCursor();
        open = true;
    }

    public static boolean isOpen() {
        return open;
    }

    public static void render(MatrixStack matrices) {
        TextRenderer textRenderer = client.textRenderer;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        TextureManager textureManager = client.getTextureManager();
        FFMGraphicsHelper.StencilHelper stencilHelper =
                new FFMGraphicsHelper.StencilHelper(FFMGraphicsHelper.StencilHelper.RESET_OPENGL_STATE_ON_END);

        int scaledHeight = client.getWindow().getScaledHeight();
        int scaledWidth = client.getWindow().getScaledWidth();
        int halfDiagonal = (int) (Math.hypot(scaledHeight, scaledWidth) / 2) + 1;
        int middleX = scaledWidth / 2;
        int middleY = scaledHeight / 2;

        int inactiveColor = 0xaa000000;
        int activeColor = 0xffff214a;
        int inactiveTextColor = 0xffcccccc;
        int activeTextColor = 0xffffffff;
        int margins = 5;
        double angleMargins = Math.PI / 64d;
        int itemSize = 40;
        int totalSize = (int) (0.5 * Math.min(scaledHeight, scaledWidth) / 2);
        int innerCircleRadius = totalSize - itemSize - margins;
        float activeScale = 1.35f;

        innerCircleSize = innerCircleRadius + margins;

        int selectedItem = getSelectedItem();
        int itemCount = getItems().size();
        double angleItem = Math.PI * 2 / itemCount - angleMargins;
        Identifier circleTexture = new Identifier("ffm", "circle.png");
        Identifier pixelTexture = new Identifier("ffm", "pixel.png");
        Matrix4f matrix = matrices.peek().getModel();

        int ica  = inactiveColor     >> 24; int icr  = inactiveColor     >> 16 & 0xff; int icg  = inactiveColor     >> 8 & 0xff; int icb  = inactiveColor     & 0xff;
        int aca  = activeColor       >> 24; int acr  = activeColor       >> 16 & 0xff; int acg  = activeColor       >> 8 & 0xff; int acb  = activeColor       & 0xff;


        stencilHelper.beginStencil();

        textureManager.bindTexture(circleTexture);
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix, middleX + (innerCircleRadius + margins), middleY - (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(1, 0).next();
        bufferBuilder.vertex(matrix, middleX - (innerCircleRadius + margins), middleY - (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(0, 0).next();
        bufferBuilder.vertex(matrix, middleX - (innerCircleRadius + margins), middleY + (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(0, 1).next();
        bufferBuilder.vertex(matrix, middleX + (innerCircleRadius + margins), middleY + (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(1, 1).next();
        bufferBuilder.end();
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        BufferRenderer.draw(bufferBuilder);

        double a = angleItem / 2 + Math.PI / 2;

        textureManager.bindTexture(pixelTexture);
        bufferBuilder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);

        for(int i = 0; i < itemCount; i++) {
            bufferBuilder.vertex(matrix, middleX, middleY, 0).color(1f, 1f, 1f, 1f).next();
            bufferBuilder.vertex(matrix, (float)(middleX + Math.cos(a) * halfDiagonal), (float)(middleY - Math.sin(a) * halfDiagonal), 0).color(1f, 1f, 1f, 1f).next();;
            a += angleMargins;
            bufferBuilder.vertex(matrix, (float)(middleX + Math.cos(a) * halfDiagonal), (float)(middleY - Math.sin(a) * halfDiagonal), 0).color(1f, 1f, 1f, 1f).next();;
            a += angleItem;
        }
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        double selectedItemStartAngle = (Math.PI / 2 - angleItem / 2) + ((angleItem + angleMargins) * selectedItem);
        if (selectedItem != -1) {
            a = selectedItemStartAngle;
            bufferBuilder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, middleX, middleY, 0).color(1f, 1f, 1f, 1f).next();
            bufferBuilder.vertex(matrix, (float) (middleX + Math.cos(a) * halfDiagonal), (float) (middleY - Math.sin(a) * halfDiagonal), 0).color(1f, 1f, 1f, 1f).next();
            ;
            a += angleItem;
            bufferBuilder.vertex(matrix, (float) (middleX + Math.cos(a) * halfDiagonal), (float) (middleY - Math.sin(a) * halfDiagonal), 0).color(1f, 1f, 1f, 1f).next();
            ;
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
        }

        stencilHelper.useStencil();
        // invert stencil
        RenderSystem.stencilFunc(GL11.GL_GREATER, 0xff, 0xff);

        textureManager.bindTexture(circleTexture);
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix, middleX + totalSize, middleY - totalSize, 0).color(icr, icg, icb, ica).texture(1, 0).next();
        bufferBuilder.vertex(matrix, middleX - totalSize, middleY - totalSize, 0).color(icr, icg, icb, ica).texture(0, 0).next();
        bufferBuilder.vertex(matrix, middleX - totalSize, middleY + totalSize, 0).color(icr, icg, icb, ica).texture(0, 1).next();
        bufferBuilder.vertex(matrix, middleX + totalSize, middleY + totalSize, 0).color(icr, icg, icb, ica).texture(1, 1).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        stencilHelper.endStencil();

        if (selectedItem != -1) {
            stencilHelper.beginStencil();

            a = selectedItemStartAngle;
            textureManager.bindTexture(pixelTexture);
            bufferBuilder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, middleX, middleY, 0).color(1f, 1f, 1f, 1f).next();
            bufferBuilder.vertex(matrix, (float) (middleX + Math.cos(a) * halfDiagonal), (float) (middleY - Math.sin(a) * halfDiagonal), 0).color(1f, 1f, 1f, 1f).next();
            ;
            a += angleItem;
            bufferBuilder.vertex(matrix, (float) (middleX + Math.cos(a) * halfDiagonal), (float) (middleY - Math.sin(a) * halfDiagonal), 0).color(1f, 1f, 1f, 1f).next();
            ;
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);

            textureManager.bindTexture(circleTexture);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_ZERO);
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
            bufferBuilder.vertex(matrix, middleX + (innerCircleRadius + margins), middleY - (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(1, 0).next();
            bufferBuilder.vertex(matrix, middleX - (innerCircleRadius + margins), middleY - (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(0, 0).next();
            bufferBuilder.vertex(matrix, middleX - (innerCircleRadius + margins), middleY + (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(0, 1).next();
            bufferBuilder.vertex(matrix, middleX + (innerCircleRadius + margins), middleY + (innerCircleRadius + margins), 0).color(1f, 1f, 1f, 1f).texture(1, 1).next();
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);

            stencilHelper.useStencil();

            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
            float x1 = middleX - (innerCircleRadius + margins + itemSize * activeScale);
            float y1 = middleY - (innerCircleRadius + margins + itemSize * activeScale);
            float x2 = middleX + (innerCircleRadius + margins + itemSize * activeScale);
            float y2 = middleY + (innerCircleRadius + margins + itemSize * activeScale);
            bufferBuilder.vertex(matrix, x2, y1, 0).color(acr, acg, acb, aca).texture(1, 0).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(acr, acg, acb, aca).texture(0, 0).next();
            bufferBuilder.vertex(matrix, x1, y2, 0).color(acr, acg, acb, aca).texture(0, 1).next();
            bufferBuilder.vertex(matrix, x2, y2, 0).color(acr, acg, acb, aca).texture(1, 1).next();
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);

            stencilHelper.endStencil();
        }

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix, middleX + innerCircleRadius, middleY - innerCircleRadius, 0).color(icr, icg, icb, ica).texture(1, 0).next();
        bufferBuilder.vertex(matrix, middleX - innerCircleRadius, middleY - innerCircleRadius, 0).color(icr, icg, icb, ica).texture(0, 0).next();
        bufferBuilder.vertex(matrix, middleX - innerCircleRadius, middleY + innerCircleRadius, 0).color(icr, icg, icb, ica).texture(0, 1).next();
        bufferBuilder.vertex(matrix, middleX + innerCircleRadius, middleY + innerCircleRadius, 0).color(icr, icg, icb, ica).texture(1, 1).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        a = Math.PI / 2;
        for(int i = 0; i < itemCount; i++) {
            float t = innerCircleRadius + margins + (i == selectedItem ? itemSize * activeScale : itemSize) / 2;
            drawCenteredString(matrices,
                    textRenderer,
                    (String) getItems().keySet().toArray()[i],
                    (int)(middleX + Math.cos(a) * t),
                    (int)(middleY - Math.sin(a) * t - textRenderer.fontHeight / 2),
                    i == selectedItem ? activeTextColor : inactiveTextColor);
            a += angleItem + angleMargins;
        }

        drawCenteredString(matrices,
                textRenderer,
                "fast",
                middleX,
                middleY - textRenderer.fontHeight - 2,
                inactiveTextColor);
        drawCenteredString(matrices,
                textRenderer,
                "menu",
                middleX,
                middleY + 2,
                inactiveTextColor);
    }

    private static int getSelectedItem() {
        double x = client.mouse.getX();
        double y = client.mouse.getY();
        double w = client.getWindow().getWidth();
        double h = client.getWindow().getHeight();
        double cx = w / 2;
        double cy = h / 2;
        double l = Math.hypot(x - cx, y - cy);
        double a = -Math.atan((y - cy) / (x - cx));
        if(x < cx) a = Math.PI + a;
        if(x >= cx && y > cy) a = 2 * Math.PI + a;
        int r = -1;
        if(l > client.getWindow().getScaleFactor() * innerCircleSize) {
            a -= Math.PI / 2;
            a += Math.PI * 2 / getItems().size() / 2;
            a %= Math.PI * 2;
            if(a < 0) a += Math.PI * 2;
            r = (int) Math.floor(a / (Math.PI * 2 / getItems().size()));
        }
        return r;
    }

    public static void release() {
        int sel = getSelectedItem();
        if(sel == -1) return;
        String k = (String) getItems().keySet().toArray()[sel];
        client.player.sendChatMessage(getItems().get(k));
    }

    public static void close() {
        release();
        client.mouse.lockCursor();
        open = false;
    }
}
