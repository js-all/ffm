package dev.viandox.ffm.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.config.Config;
import dev.viandox.ffm.interpolation.InterpolableColor;
import dev.viandox.ffm.interpolation.InterpolableDouble;
import dev.viandox.ffm.interpolation.InterpolableFloat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.util.function.BiConsumer;

public class FFMSliderWidget extends SliderWidget {
    private BiConsumer<FFMSliderWidget, Double> onChange;
    private InterpolableDouble iValue;
    private boolean wasHovered = false;
    private InterpolableFloat thumbSize = InterpolableFloat.easeInOut(2f);

    public FFMSliderWidget(int x, int y, int width, int height, Text text, double value, BiConsumer<FFMSliderWidget, Double> onChange) {
        super(x, y, width, height, text, value);
        this.iValue = InterpolableDouble.easeInOut(value);
        this.onChange = onChange;
        this.updateMessage();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(this.isHovered() != this.wasHovered) {
          if(this.isHovered()) this.thumbSize.set(3f);
          else this.thumbSize.set(2f);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        client.getTextureManager().bindTexture(new Identifier("ffm", "circle.png"));

        FFMGraphicsHelper.StencilHelper stencilHelper = new FFMGraphicsHelper.StencilHelper(FFMGraphicsHelper.StencilHelper.RESET_OPENGL_STATE_ON_END);
        stencilHelper.beginStencil();

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        Matrix4f matrix = matrices.peek().getModel();

        float h = height;
        float w = width;
        double v = iValue.get();

        bufferBuilder.vertex(matrix, (x + h), (  y  ), this.getZOffset()).color(1f, 1f, 1f,1f).texture(1f, 0f).next();
        bufferBuilder.vertex(matrix, (  x  ), (  y  ), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0f, 0f).next();
        bufferBuilder.vertex(matrix, (  x  ), (y + h), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0f, 1f).next();
        bufferBuilder.vertex(matrix, (x + h), (y + h), this.getZOffset()).color(1f, 1f, 1f,1f).texture(1f, 1f).next();

        bufferBuilder.vertex(matrix, (x + w - h / 2), (  y  ), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0.6f, 0.4f).next();
        bufferBuilder.vertex(matrix, (  x + h / 2  ), (  y  ), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0.4f, 0.4f).next();
        bufferBuilder.vertex(matrix, (  x + h / 2  ), (y + h), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0.4f, 0.6f).next();
        bufferBuilder.vertex(matrix, (x + w - h / 2), (y + h), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0.6f, 0.6f).next();

        bufferBuilder.vertex(matrix, (  x + w  ), (  y  ), this.getZOffset()).color(1f, 1f, 1f,1f).texture(1f, 0f).next();
        bufferBuilder.vertex(matrix, (x + w - h), (  y  ), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0f, 0f).next();
        bufferBuilder.vertex(matrix, (x + w - h), (y + h), this.getZOffset()).color(1f, 1f, 1f,1f).texture(0f, 1f).next();
        bufferBuilder.vertex(matrix, (  x + w  ), (y + h), this.getZOffset()).color(1f, 1f, 1f,1f).texture(1f, 1f).next();

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        stencilHelper.useStencil();
        int[] c = ColorConverter.INTtoRGBA(Config.accentColor);
        int[] coff = ColorConverter.INTtoRGBA(0xffdddddd);
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);

        bufferBuilder.vertex(matrix, (x + w), (  y  ), this.getZOffset() - 1).color(coff[0], coff[1], coff[2], coff[3]).texture(0.6f, 0.4f).next();
        bufferBuilder.vertex(matrix, (  x  ), (  y  ), this.getZOffset() - 1).color(coff[0], coff[1], coff[2], coff[3]).texture(0.4f, 0.4f).next();
        bufferBuilder.vertex(matrix, (  x  ), (y + h), this.getZOffset() - 1).color(coff[0], coff[1], coff[2], coff[3]).texture(0.4f, 0.6f).next();
        bufferBuilder.vertex(matrix, (x + w), (y + h), this.getZOffset() - 1).color(coff[0], coff[1], coff[2], coff[3]).texture(0.6f, 0.6f).next();

        bufferBuilder.vertex(matrix, (float) (x + w * v), (  y  ), this.getZOffset()).color(c[0], c[1], c[2], c[3]).texture(0.6f, 0.4f).next();
        bufferBuilder.vertex(matrix,         (    x    ), (  y  ), this.getZOffset()).color(c[0], c[1], c[2], c[3]).texture(0.4f, 0.4f).next();
        bufferBuilder.vertex(matrix,         (    x    ), (y + h), this.getZOffset()).color(c[0], c[1], c[2], c[3]).texture(0.4f, 0.6f).next();
        bufferBuilder.vertex(matrix, (float) (x + w * v), (y + h), this.getZOffset()).color(c[0], c[1], c[2], c[3]).texture(0.6f, 0.6f).next();

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        stencilHelper.endStencil();

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);

        float s = this.thumbSize.get();
        bufferBuilder.vertex(matrix, (float) (x + w * v + s + h / 2), (  y - s  ), this.getZOffset()).color(c[0], c[1], c[2],c[3]).texture(1f, 0f).next();
        bufferBuilder.vertex(matrix, (float) (x + w * v - s - h / 2), (  y - s  ), this.getZOffset()).color(c[0], c[1], c[2],c[3]).texture(0f, 0f).next();
        bufferBuilder.vertex(matrix, (float) (x + w * v - s - h / 2), (y + h + s), this.getZOffset()).color(c[0], c[1], c[2],c[3]).texture(0f, 1f).next();
        bufferBuilder.vertex(matrix, (float) (x + w * v + s + h / 2), (y + h + s), this.getZOffset()).color(c[0], c[1], c[2],c[3]).texture(1f, 1f).next();

        bufferBuilder.end();
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        BufferRenderer.draw(bufferBuilder);

        wasHovered = this.isHovered();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.of(""));
    }

    @Override
    protected void applyValue() {
        this.onChange.accept(this, value);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        iValue.setWithoutInterpolation(value);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        iValue.set(value);
    }
}
