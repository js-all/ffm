package dev.viandox.ffm;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
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
    * <p>The Z coordinate of the rectangle is {@link #zOffset}.
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
}
