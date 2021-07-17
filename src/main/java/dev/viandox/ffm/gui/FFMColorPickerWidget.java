package dev.viandox.ffm.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.ColorConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

public class FFMColorPickerWidget extends AbstractButtonWidget {
    private int colorPickerWidth = 130;
    private int colorPickerHeight = 111;
    private float[] value;
    private float alpha;
    private boolean hasAlpha;
    public FFMColorPickerWidget(int x, int y, int width, int height, Text message, int value, boolean alpha) {
        super(x, y, width, height, message);
        int[] rgb = ColorConverter.INTtoRGB(value);
        this.alpha = (float) (value >> 24) / 255;
        this.hasAlpha = alpha;
        this.value = ColorConverter.RGBtoHSL(rgb[0], rgb[1], rgb[2]);
    }

    public int getValue() {
        int res = ColorConverter.RGBtoINT(ColorConverter.HSLtoRGB(value[0], value[1], value[2]));
        // set both last bits to 0
        res = res & ~(0xff << 24);
        // set alpha
        res = res | ((int)this.alpha * 255) << 24;
        return res;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // just to be sure
        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("ffm", "pixel.png"));
        int oldVal = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        this.drawSaturationLightnessArea(matrices,
                x - this.colorPickerWidth - 25 - (this.hasAlpha ? 25 : 0) - 10,
                y + height / 2 - this.colorPickerHeight / 2,
                this.colorPickerWidth,
                this.colorPickerHeight);
        this.drawHueSlider(matrices,
                 x - 20 - 10,
                y + height / 2 - this.colorPickerHeight / 2,
                20,
                this.colorPickerHeight);
        if(this.hasAlpha) {
            this.drawAlphaSlider(matrices,
                    x - 20 - 25 - 10,
                    y + height / 2 - this.colorPickerHeight / 2,
                    20,
                    this.colorPickerHeight);
        }
        GL11.glShadeModel(oldVal);
    }

    private void drawHueSlider(MatrixStack matrices, int x, int y, int w, int h) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        float fh = (float) h;
        float z = 0;
        float xs = (float)x;
        float xe = xs + w;
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        // heeeere we go
        Matrix4f matrix = matrices.peek().getModel();

        // #f00 000% -> #ff0 017% (red  to yellow)
        bufferBuilder.vertex(matrix, xe, y + 0.00f * fh, z).color(1.0f, 0.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.00f * fh, z).color(1.0f, 0.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, 1.0f).next();
        // #ff0 017% -> #0f0 033% (yellow to lime)
        bufferBuilder.vertex(matrix, xe, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, 1.0f).next();
        // #0f0 033% -> #0ff 050% (lime to cyan)
        bufferBuilder.vertex(matrix, xe, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, 1.0f).next();
        // #0ff 050% -> #00f 066% (cyan to blue)
        bufferBuilder.vertex(matrix, xe, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
        // #00f 066% -> #f0f 083% (blue to magenta)
        bufferBuilder.vertex(matrix, xe, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, 1.0f).next();
        // #f0f 083% -> #f00 100% (magenta to red)
        bufferBuilder.vertex(matrix, xe, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xs, y + 1.00f * fh, z).color(1.0f, 0.0f, 0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, xe, y + 1.00f * fh, z).color(1.0f, 0.0f, 0.0f, 1.0f).next();

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    private void drawSaturationLightnessArea(MatrixStack matrices, int x, int y, int w, int h) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        int x0 = x;
        int y0 = y;
        int x1 = x + w;
        int y1 = y + h;
        int z = 0;

        int[] color = ColorConverter.HSLtoRGB(this.value[0], this.value[1], this.value[2]);

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getModel();

        bufferBuilder.vertex(matrix, x1, y0, z).color(color[0], color[1], color[2], 255).next();
        bufferBuilder.vertex(matrix, x0, y0, z).color(color[0], color[1], color[2], 255).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(color[0], color[1], color[2], 255).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(color[0], color[1], color[2], 255).next();
        // now, you might ask, what is this magic number, what is 0.9 here ?
        // and to that ill answer, it scales the gradient overlay to make
        // them actually cover the surface of the quad above.
        // And i know i shouldn't need this, but for IDFK what reason, it needs
        // to be like that, so, so be it.
        x1 = (int) (x + (float) w / 0.9);

        bufferBuilder.vertex(matrix, x1, y0, z).color(1f, 1f, 1f, 0f).next();
        bufferBuilder.vertex(matrix, x0, y0, z).color(1f, 1f, 1f, 1f).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(1f, 1f, 1f, 1f).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(1f, 1f, 1f, 0f).next();

        x1 = x + w;
        y0 = (int) (y1 - (float) h / 0.9);

        bufferBuilder.vertex(matrix, x1, y0, z).color(0f, 0f, 0f, 0f).next();
        bufferBuilder.vertex(matrix, x0, y0, z).color(0f, 0f, 0f, 0f).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(0f, 0f, 0f, 1f).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(0f, 0f, 0f, 1f).next();

        bufferBuilder.end();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.draw(bufferBuilder);
    }

    private void drawAlphaSlider(MatrixStack matrices, int x, int y, int w, int h) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        int[] color = ColorConverter.HSLtoRGB(this.value[0], this.value[1], this.value[2]);

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getModel();
        // see drawSaturationLightnessArea comment
        h /= 0.9;

        bufferBuilder.vertex(matrix, x + w, y, 0).color(color[0], color[1], color[2], 255).next();
        bufferBuilder.vertex(matrix, x, y, 0).color(color[0], color[1], color[2], 255).next();
        bufferBuilder.vertex(matrix, x, y + h, 0).color(color[0], color[1], color[2], 0).next();
        bufferBuilder.vertex(matrix, x + w, y + h, 0).color(color[0], color[1], color[2], 0).next();

        bufferBuilder.end();
        RenderSystem.enableBlend();
        BufferRenderer.draw(bufferBuilder);
    }
}
