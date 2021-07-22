package dev.viandox.ffm.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.FFMUtils;
import dev.viandox.ffm.interpolation.InterpolableColor;
import dev.viandox.ffm.interpolation.InterpolableFloat;
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

import java.util.function.BiConsumer;

public class FFMColorPickerWidget extends AbstractButtonWidget {
    // its really just the big rect dimensions
    private final int colorPickerWidth = 130;
    private final int colorPickerHeight = 111;
    // the actual value, in HSV
    private final float[] value;
    private float alpha;
    private final boolean hasAlpha;
    private boolean open = false;
    // to know when it changes
    private boolean wasHovered = false;
    // can be "saturationValue" "hue" or "alpha" to know when the click started
    // to figure which component to update on mouse drag
    private String lastMouseClickLocation = null;
    // by how much the picker is shifted (for the transition when opened)
    private final InterpolableFloat pickerShift = InterpolableFloat.easeInOut(50f);
    private final InterpolableFloat pickerOpacity = InterpolableFloat.easeInOut(0f);
    private final InterpolableColor borderColor = InterpolableColor.easeInOut(0x00000000);
    private final BiConsumer<FFMColorPickerWidget, Integer> onChange;

    public FFMColorPickerWidget(int x, int y, int width, int height, Text message, int value, boolean alpha, BiConsumer<FFMColorPickerWidget, Integer> onChange) {
        super(x, y, width, height, message);
        int[] rgba = ColorConverter.INTtoRGBA(value);
        this.alpha = (float) (rgba[3]) / 255;
        this.hasAlpha = alpha;
        this.value = ColorConverter.RGBtoHSV(rgba[0], rgba[1], rgba[2]);
        this.onChange = onChange;
    }

    public int getValue() {
        return ColorConverter.RGBAtoINT(
                ColorConverter.RGBtoRGBA(
                        ColorConverter.HSVtoRGB(value[0], value[1], value[2]),
                        (int)(this.alpha * 255)
                )
        );
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(wasHovered != this.isHovered()) {
            if(this.isHovered()) this.borderColor.set(0x55dddddd);
            else this.borderColor.set(0x00000000);
        }

        matrices.push();
        matrices.translate(pickerShift.get(), 0, 0);

        // just to be sure
        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("ffm", "pixel.png"));
        // we check interpolation progress because this.open is set at once, and this hits 0 after when the transition is finished
        if (pickerOpacity.getInterpolationProgress() > 0) {
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425); // shadeModel GL_SMOOTH (for gradients)

            FFMUtils.BoundingBox saturationValue = this.getSVAreaBB();
            FFMUtils.BoundingBox hue = this.getHSliderBB();
            FFMUtils.BoundingBox alpha = this.getASliderBB();

            this.drawSaturationLightnessArea(matrices,
                    (int) saturationValue.x0, (int) saturationValue.y0,
                    (int) saturationValue.getWidth(), (int) saturationValue.getHeight(),
                    pickerOpacity.get());
            this.drawHueSlider(matrices,
                    (int) hue.x0, (int) hue.y0,
                    (int) hue.getWidth(), (int) hue.getHeight(),
                    pickerOpacity.get());
            if (this.hasAlpha) {
                this.drawAlphaSlider(matrices,
                        (int) alpha.x0, (int) alpha.y0,
                        (int) alpha.getWidth(), (int) alpha.getHeight(),
                        pickerOpacity.get());
            }
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }
        matrices.pop();
        RenderSystem.enableBlend();

        FFMGraphicsHelper.StencilHelper stencilHelper = new FFMGraphicsHelper.StencilHelper(FFMGraphicsHelper.StencilHelper.RESET_OPENGL_STATE_ON_END);

        stencilHelper.beginStencil();
        // cutout rounded rect same size of the one showing the color
        FFMGraphicsHelper.drawSmallRoundedRect(matrices, x, y, x + width, y + height, 1, 0xffffffff);
        stencilHelper.useStencil();
        // draw checkerboard to have a background when color has alpha
        FFMGraphicsHelper.drawCheckerboardQuad(matrices, 5, x, y, width, height, 1, 0xff757575, 0xff262626);
        // inverse stencil
        RenderSystem.stencilFunc(GL11.GL_GREATER, 0xff, 0xff);
        // draw border
        FFMGraphicsHelper.drawSmallRoundedRect(matrices, x - 1, y - 1, x + width + 1, y + height + 1, 0, borderColor.getInt());

        stencilHelper.endStencil();
        FFMGraphicsHelper.drawSmallRoundedRect(matrices, x, y, x + width, y + height, 1, this.getValue());
        this.wasHovered = this.isHovered();
    }

    public void open() {
        this.open = true;
        this.pickerShift.set(0f);
        this.pickerOpacity.set(1f);
    }

    public void close() {
        this.open = false;
        this.pickerShift.set(50f);
        this.pickerOpacity.set(0f);
    }

    public void toggle() {
        if(this.open) {
            this.close();
        } else {
            this.open();
        }
    }
    // those methods define the sizes and position of the different slider
    // they are defined like that because we need them both when rendering
    // and dealing with mouse event, this removes the duplications
    private FFMUtils.BoundingBox getSVAreaBB() {
        return new FFMUtils.BoundingBox(
                x - this.colorPickerWidth - 16 - (this.hasAlpha ? 16 : 0) - 10,
                y + (float) height / 2 - (float) this.colorPickerHeight / 2,
                x - 16 - (this.hasAlpha ? 16 : 0) - 10,
                y + (float) height / 2 + (float) this.colorPickerHeight / 2
        );
    }

    private FFMUtils.BoundingBox getHSliderBB() {
        return new FFMUtils.BoundingBox(
                x - 15 - 10,
                y + (float) height / 2 - (float) this.colorPickerHeight / 2,
                x - 10,
                y + (float) height / 2 + (float) this.colorPickerHeight / 2
        );
    }

    private FFMUtils.BoundingBox getASliderBB() {
        return new FFMUtils.BoundingBox(
                x - 15 - 16 - 10,
                y + (float) height / 2 - (float) this.colorPickerHeight / 2,
                x - 16 - 10,
                y + (float) height / 2 + (float) this.colorPickerHeight / 2
        );
    }
    // in those method f is opacity
    private void drawHueSlider(MatrixStack matrices, int x, int y, int w, int h, float f) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        // float height
        float fh = (float) h;
        float z = 0;
        // x coord start
        float xs = (float)x;
        // x coord end
        float xe = xs + w;

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        // heeeere we go
        Matrix4f matrix = matrices.peek().getModel();
        // I like it A L I G N E D
        // #f00 000% -> #ff0 017% (red  to yellow)
        bufferBuilder.vertex(matrix, xe, y + 0.00f * fh, z).color(1.0f, 0.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.00f * fh, z).color(1.0f, 0.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, f).next();
        // #ff0 017% -> #0f0 033% (yellow to lime)
        bufferBuilder.vertex(matrix, xe, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.17f * fh, z).color(1.0f, 1.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, f).next();
        // #0f0 033% -> #0ff 050% (lime to cyan)
        bufferBuilder.vertex(matrix, xe, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.33f * fh, z).color(0.0f, 1.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, f).next();
        // #0ff 050% -> #00f 066% (cyan to blue)
        bufferBuilder.vertex(matrix, xe, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.50f * fh, z).color(0.0f, 1.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, f).next();
        // #00f 066% -> #f0f 083% (blue to magenta)
        bufferBuilder.vertex(matrix, xe, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.66f * fh, z).color(0.0f, 0.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, f).next();
        // #f0f 083% -> #f00 100% (magenta to red)
        bufferBuilder.vertex(matrix, xe, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 0.83f * fh, z).color(1.0f, 0.0f, 1.0f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 1.00f * fh, z).color(1.0f, 0.0f, 0.0f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 1.00f * fh, z).color(1.0f, 0.0f, 0.0f, f).next();

        // draw thumb
        float ha = this.value[0];
        bufferBuilder.vertex(matrix, xe, y + 1 + ha * (fh - 2) - 1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 1 + ha * (fh - 2) - 1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 1 + ha * (fh - 2) + 1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 1 + ha * (fh - 2) + 1, z).color(1f, 1f, 1f, f).next();

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    private void drawSaturationLightnessArea(MatrixStack matrices, int x, int y, int w, int h, float f) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        int x0 = x;
        int y0 = y;
        int x1 = x + w;
        int y1 = y + h;
        int z = 0;

        int[] color = ColorConverter.HSVtoRGB(this.value[0], 1f, 1f);
        int c = ColorConverter.RGBtoINT(color);

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getModel();
        // this whole thing is draw by first:
        // - filling a rect of the selected hue (100 saturation 100 value)
        // - then drawing a white to transparent gradient from left to right over it (to make saturation)
        // - then drawing a black to transparent gradient from bottom to top over it (to make value)
        bufferBuilder.vertex(matrix, x1, y0, z).color(color[0], color[1], color[2], (int) (f * 255)).next();
        bufferBuilder.vertex(matrix, x0, y0, z).color(color[0], color[1], color[2], (int) (f * 255)).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(color[0], color[1], color[2], (int) (f * 255)).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(color[0], color[1], color[2], (int) (f * 255)).next();

        bufferBuilder.vertex(matrix, x1, y0, z).color(1f, 1f, 1f, 0f).next();
        bufferBuilder.vertex(matrix, x0, y0, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(1f, 1f, 1f, 0f).next();

        bufferBuilder.vertex(matrix, x1, y0, z).color(0f, 0f, 0f, 0f).next();
        bufferBuilder.vertex(matrix, x0, y0, z).color(0f, 0f, 0f, 0f).next();
        bufferBuilder.vertex(matrix, x0, y1, z).color(0f, 0f, 0f, f).next();
        bufferBuilder.vertex(matrix, x1, y1, z).color(0f, 0f, 0f, f).next();

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        // draw cursor
        float s = this.value[1];
        float v = this.value[2];
        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("ffm", "circle.png"));
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        float px = x0 + s * w;
        float py = y1 - v * h;
        float si = 2f;
        bufferBuilder.vertex(matrix, px + si, py - si, z).color(1f, 1f, 1f, f).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, px - si, py - si, z).color(1f, 1f, 1f, f).texture(0.0f, 0.0f).next();
        bufferBuilder.vertex(matrix, px - si, py + si, z).color(1f, 1f, 1f, f).texture(0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix, px + si, py + si, z).color(1f, 1f, 1f, f).texture(1.0f, 1.0f).next();
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.disableTexture();
        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("ffm", "pixel.png"));
    }

    private void drawAlphaSlider(MatrixStack matrices, int x, int y, int w, int h, float f) {
        // checkerboard colors
        int[] cbc1 = new int[] {117, 117, 117, (int) (f * 255)};
        int[] cbc2 = new int[] {38, 38, 38, (int) (f * 255)};
        // draw background
        FFMGraphicsHelper.drawCheckerboardQuad(matrices, 5, x, y, w, h, 0, ColorConverter.RGBAtoINT(cbc1), ColorConverter.RGBAtoINT(cbc2));

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        int[] color = ColorConverter.HSVtoRGB(this.value[0], this.value[1], this.value[2]);
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getModel();
        // draw gradient
        float fh = (float) h;
        float xs = (float) x;
        float xe = xs + w;
        float z = 1;
        bufferBuilder.vertex(matrix, xe, y + 0, 0).color(color[0], color[1], color[2], (int) (f * 255)).next();
        bufferBuilder.vertex(matrix, xs, y + 0, 0).color(color[0], color[1], color[2], (int) (f * 255)).next();
        bufferBuilder.vertex(matrix, xs, y + h, 0).color(color[0], color[1], color[2], 0).next();
        bufferBuilder.vertex(matrix, xe, y + h, 0).color(color[0], color[1], color[2], 0).next();
        // draw thumb
        float ha = this.alpha;
        bufferBuilder.vertex(matrix, xe, y + 1 + (1 - ha) * (h - 2) - 1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 1 + (1 - ha) * (h - 2) - 1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, xs, y + 1 + (1 - ha) * (h - 2) + 1, z).color(1f, 1f, 1f, f).next();
        bufferBuilder.vertex(matrix, xe, y + 1 + (1 - ha) * (h - 2) + 1, z).color(1f, 1f, 1f, f).next();

        bufferBuilder.end();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        BufferRenderer.draw(bufferBuilder);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.toggle();
        // on click is only called when the mouse is clicked inside the color preview quad, not on the color picker
        this.lastMouseClickLocation = null;
    }


    private void updateHue(double mouseX, double mouseY) {
        FFMUtils.BoundingBox hSliderBB = this.getHSliderBB();
        float[] p = hSliderBB.forcePointInside((float)mouseX, (float)mouseY);
        mouseX = p[0];
        mouseY = p[1];
        float h = (float) ((mouseY - hSliderBB.y0) / this.colorPickerHeight);
        this.value[0] = h;
        this.onChange.accept(this, this.getValue());
    }

    private void updateSaturationValue(double mouseX, double mouseY) {
        FFMUtils.BoundingBox svAreaBB = this.getSVAreaBB();
        float[] p = svAreaBB.forcePointInside((float)mouseX, (float)mouseY);
        mouseX = p[0];
        mouseY = p[1];
        float s = (float) ((mouseX - svAreaBB.x0) / this.colorPickerWidth);
        float v = (float) ((mouseY - svAreaBB.y0) / this.colorPickerHeight);
        this.value[1] = s;
        this.value[2] = 1 - v;
        this.onChange.accept(this, this.getValue());
    }

    private void updateAlpha(double mouseX, double mouseY) {
        FFMUtils.BoundingBox aSliderBB = this.getASliderBB();
        float[] p = aSliderBB.forcePointInside((float)mouseX, (float)mouseY);
        mouseX = p[0];
        mouseY = p[1];
        this.alpha = 1 - (float) ((mouseY - aSliderBB.y0) / this.colorPickerHeight);
        this.onChange.accept(this, this.getValue());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(this.lastMouseClickLocation != null) {
            switch (this.lastMouseClickLocation) {
                case "saturationValue":
                    this.updateSaturationValue(mouseX, mouseY);
                    break;
                case "hue":
                    this.updateHue(mouseX, mouseY);
                    break;
                case "alpha":
                    this.updateAlpha(mouseX, mouseY);
                    break;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean res = super.mouseClicked(mouseX, mouseY, button);
        boolean clickedInBox = false;

        if(button == 0 && open) {
            if(this.getSVAreaBB().isPointWithin((float) mouseX, (float) mouseY)) {
                clickedInBox = true;
                this.updateSaturationValue(mouseX, mouseY);
                this.lastMouseClickLocation = "saturationValue";
            } else if(this.getHSliderBB().isPointWithin((float) mouseX, (float) mouseY)) {
                clickedInBox = true;
                this.updateHue(mouseX, mouseY);
                this.lastMouseClickLocation = "hue";
            } else if(this.hasAlpha && this.getASliderBB().isPointWithin((float) mouseX, (float) mouseY)) {
                clickedInBox = true;
                this.updateAlpha(mouseX, mouseY);
                this.lastMouseClickLocation = "alpha";
            } else {
                this.lastMouseClickLocation = null;
            }
        } else {
            this.lastMouseClickLocation = null;
        }
        if(clickedInBox) {
            // return true to have the click be considered "successful", and say that the click wasn't outside
            // the widget, so that mouseDraw works.
            return true;
        } else if(!res) {
            // if !res then the click wasn't on the color preview quad, and if
            // !clickedInBox then it wasn't on the color picker either, so we close
            this.close();
        }
        return res;
    }
}
