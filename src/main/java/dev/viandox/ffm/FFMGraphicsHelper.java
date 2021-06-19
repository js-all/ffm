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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public static void makeBufferBuilderRegularPolygon(BufferBuilder bufferBuilder, float radius, int sides, float cx, float cy, int z, int a, int r, int g, int b) {
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        double sideAngle = 2*Math.PI / sides;
        for(int i = sides - 1; i >= 0; i--) {
            double angle = i * sideAngle;
            double x = cx + Math.cos(angle) * radius;
            double y = cy + Math.sin(angle) * radius;

            System.out.println(x + ", " + y);

            bufferBuilder.vertex(x, y, z).color(r, g, b, a).next();
        }
        bufferBuilder.end();
    }

    public static void drawOutlinedText(TextRenderer textRenderer, MatrixStack matrices, Text txt, float x, float y, int color, int outlineColor) {
        textRenderer.draw(matrices, txt, x + 0, y - 1, outlineColor);
        textRenderer.draw(matrices, txt, x - 1, y + 0, outlineColor);
        textRenderer.draw(matrices, txt, x + 1, y + 0, outlineColor);
        textRenderer.draw(matrices, txt, x + 0, y + 1, outlineColor);

        textRenderer.draw(matrices, txt, x, y, color);
    }

    public static void drawArrow(MatrixStack matrices, BufferBuilder bufferBuilder, float cx, float cy, float z, float width, float height, float buttHeight, int color1, int color2) {
        Matrix4f matrix = matrices.peek().getModel();
        float headHeight = height - buttHeight;

        int A1 = color1 >> 24 & 0xff;
        int R1 = color1 >> 16 & 0xff;
        int G1 = color1 >>  8 & 0xff;
        int B1 = color1 >>  0 & 0xff;
        int A2 = color2 >> 24 & 0xff;
        int R2 = color2 >> 16 & 0xff;
        int G2 = color2 >>  8 & 0xff;
        int B2 = color2 >>  0 & 0xff;

        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, cx + 0        , cy - headHeight, z).color(R1, G1, B1, A1).next();
        bufferBuilder.vertex(matrix, cx - width / 2, cy + buttHeight, z).color(R1, G1, B1, A1).next();
        bufferBuilder.vertex(matrix, cx + 0        , cy + 0         , z).color(R1, G1, B1, A1).next();
        bufferBuilder.vertex(matrix, cx + 0        , cy - headHeight, z).color(R2, G2, B2, A2).next();
        bufferBuilder.vertex(matrix, cx + width / 2, cy + buttHeight, z).color(R2, G2, B2, A2).next();
        bufferBuilder.vertex(matrix, cx + 0        , cy + 0         , z).color(R2, G2, B2, A2).next();
        bufferBuilder.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.draw(bufferBuilder);
    }

    public static void drawOutlinedArrow(MatrixStack matrices, BufferBuilder bufferBuilder, float cx, float cy, float z, float width, float height, float buttHeight, float outlineWidth, int color1, int color2, int outlineColor) {
        FFMGraphicsHelper.drawArrow(matrices, bufferBuilder, cx, cy + outlineWidth, z, width + 2 * outlineWidth, height + 2 * outlineWidth, buttHeight, outlineColor, outlineColor);
        FFMGraphicsHelper.drawArrow(matrices, bufferBuilder, cx, cy, z, width, height, buttHeight, color1, color2);
    }

    public static class StencilHelper {
        public static byte RESET_OPENGL_STATE_ON_USE = 0;
        public static byte RESET_OPENGL_STATE_ON_END = 1;

        private int stencilBit;
        private boolean glAlphaTestOld;
        private int glAlphaFuncOld;
        private float glAlphaFuncRefOld;
        private final boolean[] glColorMaskOld = new boolean[4];
        private boolean glDepthMaskOld;
        private final byte resetOpenglStatePoint;
        public StencilHelper(byte resetOpenglStatePoint) {
            this.stencilBit = 0xff;
            this.resetOpenglStatePoint = resetOpenglStatePoint;
        }

        public int getStencilBit() {
            return stencilBit;
        }
        public void setStencilBit(int stencilBit) {
            this.stencilBit = stencilBit;
        }
        public void beginStencil() {
            this.glAlphaTestOld = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            int[] tmp = new int[4];
            // i really couldn't get glGetBooleanv to work
            GL11.glGetIntegerv(GL11.GL_COLOR_WRITEMASK, tmp);

            this.glColorMaskOld[1] = tmp[0] == GL11.GL_TRUE;
            this.glColorMaskOld[0] = tmp[1] == GL11.GL_TRUE;
            this.glColorMaskOld[2] = tmp[2] == GL11.GL_TRUE;
            this.glColorMaskOld[3] = tmp[3] == GL11.GL_TRUE;
            this.glDepthMaskOld = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            this.glAlphaFuncOld = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
            this.glAlphaFuncRefOld = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            RenderSystem.colorMask (false, false, false, false);
            RenderSystem.depthMask(false);

            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

            RenderSystem.stencilFunc(GL11.GL_ALWAYS, stencilBit, stencilBit);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
            RenderSystem.stencilMask(stencilBit);

            RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        }

        public void useStencil() {
            RenderSystem.colorMask (true, true, true, true);
            RenderSystem.depthMask(true);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            RenderSystem.stencilMask(0x00);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, stencilBit, stencilBit);
            RenderSystem.stencilOp (GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            if(this.resetOpenglStatePoint == RESET_OPENGL_STATE_ON_USE) this.resetOpenglState();
        }

        public void endStencil() {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            if(this.resetOpenglStatePoint == RESET_OPENGL_STATE_ON_END) this.resetOpenglState();
        }

        private void resetOpenglState() {
            RenderSystem.colorMask(this.glColorMaskOld[0], this.glColorMaskOld[1], this.glColorMaskOld[2], this.glColorMaskOld[3]);
            RenderSystem.depthMask(this.glDepthMaskOld);
            if(this.glAlphaTestOld) GL11.glEnable(GL11.GL_ALPHA_TEST);
            else GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(this.glAlphaFuncOld, this.glAlphaFuncRefOld);
        }
    }
}
