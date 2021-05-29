package dev.viandox.ffm;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public class FFMGraphicsHelper {
    static void drawBlendingTexturedQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
        bufferBuilder.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.draw(bufferBuilder);
    }
    /**
    * Draws a textured rectangle from a region in a 256x256 texture.
    * 
    * <p>The width and height of the region are the same as
    * the dimensions of the rectangle.
    * 
    * @param matrices the matrix stack used for rendering
    * @param x the X coordinate of the rectangle
    * @param y the Y coordinate of the rectangle
    * @param u the left-most coordinate of the texture region
    * @param v the top-most coordinate of the texture region
    * @param width the width
    * @param height the height
    */
    public static void drawBlendingTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height, int zOffset) {
        drawBlendingTexture(matrices, x, y, zOffset, (float)u, (float)v, width, height, 256, 256);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
    * 
    * <p>The width and height of the region are the same as
    * the dimensions of the rectangle.
    * 
    * @param matrices the matrix stack used for rendering
    * @param x the X coordinate of the rectangle
    * @param y the Y coordinate of the rectangle
    * @param z the Z coordinate of the rectangle
    * @param u the left-most coordinate of the texture region
    * @param v the top-most coordinate of the texture region
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param textureHeight the height of the entire texture
    * @param textureWidth the width of the entire texture
    */
    public static void drawBlendingTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height, int textureHeight, int textureWidth) {
        drawBlendingTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
    * 
    * @param matrices the matrix stack used for rendering
    * @param x the X coordinate of the rectangle
    * @param y the Y coordinate of the rectangle
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param u the left-most coordinate of the texture region
    * @param v the top-most coordinate of the texture region
    * @param regionWidth the width of the texture region
    * @param regionHeight the height of the texture region
    * @param textureWidth the width of the entire texture
    * @param textureHeight the height of the entire texture
    */
    public static void drawBlendingTexture(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        drawBlendingTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    /**
     * Draws a textured rectangle from a region in a texture.
    * 
    * <p>The width and height of the region are the same as
    * the dimensions of the rectangle.
    * 
    * @param matrices the matrix stack used for rendering
    * @param x the X coordinate of the rectangle
    * @param y the Y coordinate of the rectangle
    * @param u the left-most coordinate of the texture region
    * @param v the top-most coordinate of the texture region
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param textureWidth the width of the entire texture
    * @param textureHeight the height of the entire texture
    */
    public static void drawBlendingTexture(MatrixStack matrices, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        drawBlendingTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    public static void drawBlendingTexture(MatrixStack matrices, int x0, int y0, int x1, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawBlendingTexturedQuad(matrices.peek().getModel(), x0, y0, x1, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }


    public static void drawRoundedRect(MinecraftClient client, float x0, float y0, float x1, float y1, float z, int color, float cs) {
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8  & 0xff;
        int b = color >> 0  & 0xff;
        drawRoundedRect(client, x0, y0, x1, y1, z, a, r, g, b, cs);
    }

    public static void drawRoundedRect(MinecraftClient client, float x0, float y0, float x1, float y1, float z, int a, int r, int g, int b, float cs) {
        // the rectangle is rendered this way, from top to bottom, from left to right
        //   ╭─────┬─────────────────┬─────╮
        //   │  1  │        2        │  3  │
        //   ├─────┴─────────────────┴─────┤
        //   │                             │
        //   │                             │
        //   │                             │
        //   │              4              │
        //   │                             │
        //   │                             │
        //   │                             │
        //   ├─────┬─────────────────┬─────┤
        //   │  5  │        6        │  7  │
        //   ╰─────┴─────────────────┴─────╯

        // bind rounded corner texture
        client.getTextureManager().bindTexture(new Identifier("ffm", "corner.png"));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        float c = cs;
        // top left corner
        bufferBuilder.vertex(x0 + c, y0 + 0, z).texture(1.0F, 0.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + 0, y0 + 0, z).texture(0.0F, 0.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + 0, y0 + c, z).texture(0.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + c, y0 + c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        // top middle bar
        bufferBuilder.vertex(x1 - c, y0 + 0, z).texture(0.9F, 0.9F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + c, y0 + 0, z).texture(1.0F, 0.9F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + c, y0 + c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - c, y0 + c, z).texture(0.9F, 1.0F).color(r, g, b, a).next();
        // top right corner
        bufferBuilder.vertex(x1 + 0, y0 + 0, z).texture(0.0F, 0.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - c, y0 + 0, z).texture(1.0F, 0.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - c, y0 + c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 + 0, y0 + c, z).texture(0.0F, 1.0F).color(r, g, b, a).next();
        /// most of body
        bufferBuilder.vertex(x1 + 0, y0 + c, z).texture(0.9F, 0.9F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + 0, y0 + c, z).texture(1.0F, 0.9F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + 0, y1 - c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - 0, y1 - c, z).texture(0.9F, 1.0F).color(r, g, b, a).next();
        // bottom left corner
        bufferBuilder.vertex(x0 + c, y1 - c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + 0, y1 - c, z).texture(0.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + 0, y1 + 0, z).texture(0.0F, 0.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + c, y1 + 0, z).texture(1.0F, 0.0F).color(r, g, b, a).next();
        // bottom middle bar
        bufferBuilder.vertex(x1 - c, y1 - c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + c, y1 - c, z).texture(0.9F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x0 + c, y1 + 0, z).texture(0.9F, 0.9F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - c, y1 + 0, z).texture(1.0F, 0.9F).color(r, g, b, a).next();
        // bottom right corner
        bufferBuilder.vertex(x1 + 0, y1 - c, z).texture(0.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - c, y1 - c, z).texture(1.0F, 1.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 - c, y1 + 0, z).texture(1.0F, 0.0F).color(r, g, b, a).next();
        bufferBuilder.vertex(x1 + 0, y1 + 0, z).texture(0.0F, 0.0F).color(r, g, b, a).next();


        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
    }

    public static void drawOutlinedText(TextRenderer textRenderer, MatrixStack matrices, Text txt, float x, float y, int color, int outlineColor) {
        textRenderer.draw(matrices, txt, x + 0, y - 1, outlineColor);
        textRenderer.draw(matrices, txt, x - 1, y + 0, outlineColor);
        textRenderer.draw(matrices, txt, x + 1, y + 0, outlineColor);
        textRenderer.draw(matrices, txt, x + 0, y + 1, outlineColor);

        textRenderer.draw(matrices, txt, x, y, color);
    }
}
