package dev.viandox.ffm.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.config.Config;
import dev.viandox.ffm.interpolation.InterpolableColor;
import dev.viandox.ffm.interpolation.InterpolableDouble;
import dev.viandox.ffm.interpolation.InterpolableFloat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.opengl.GL11;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.function.BiConsumer;

public class FFMSliderWidget extends SliderWidget {
    private final BiConsumer<FFMSliderWidget, Double> onChange;
    private final InterpolableDouble iValue;
    private boolean wasHovered = false;
    private final InterpolableFloat thumbSize = InterpolableFloat.easeInOut(2f);
    private final InterpolableFloat toolTipOpacity = InterpolableFloat.easeInOut(0f);
    private final InterpolableColor toolTipTextColor = InterpolableColor.easeInOut(0x00ffffff);
    private boolean isActive = false;

    public FFMSliderWidget(int x, int y, int width, int height, Text text, double value, BiConsumer<FFMSliderWidget, Double> onChange) {
        super(x, y, width, height, text, value);
        this.iValue = InterpolableDouble.easeInOut(value);
        this.onChange = onChange;
        this.applyValue();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(this.isHovered() != this.wasHovered) {
          if(this.isHovered()) {
              this.thumbSize.set(3f);
              this.toolTipOpacity.set(0.7f);
              this.toolTipTextColor.set(0xffffffff);
          }
          else {
              this.thumbSize.set(2f);
              this.toolTipOpacity.set(0f);
              this.toolTipTextColor.set(0x00ffffff);
          }
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
        int[] c = Config.accentColor.getArray();
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

        TextRenderer textRenderer = client.textRenderer;
        float tw = (float) textRenderer.getWidth(this.getMessage()) / 2;
        float th = 7;
        float p = 5;

        FFMGraphicsHelper.drawSmallRoundedRect(matrices,
                x + w * (float)v - tw - p,
                y - th - textRenderer.fontHeight - p - p,
                x + w * (float) v + tw + p,
                y - th,
                0,
                (int)(toolTipOpacity.get() * 255),
                0, 0, 0
        );
        client.getTextureManager().bindTexture(new Identifier("ffm", "pixel.png"));
        bufferBuilder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x + w * (float)v + 4, y - th, 0).color(0, 0, 0, toolTipOpacity.get()).next();
        bufferBuilder.vertex(matrix, x + w * (float)v - 4, y - th, 0).color(0, 0, 0, toolTipOpacity.get()).next();
        bufferBuilder.vertex(matrix, x + w * (float)v, y - th + 3, 0).color(0, 0, 0, toolTipOpacity.get()).next();

        bufferBuilder.end();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();

        // textRenderer interpret 0xAARRGGBB colors with 0 alpha as 0x00RRGGBB so this stops it from rendering the text when alpha is 0
        if(toolTipTextColor.get()[3] > 8) {
            drawCenteredText(matrices, textRenderer, this.getMessage(), (int)(x + w * v), (int)(y - th - p - textRenderer.fontHeight), toolTipTextColor.getInt());
        }

        wasHovered = this.isHovered();
    }

    @Override
    public boolean isHovered() {
        return super.isHovered() || this.isActive;
    }

    @Override
    protected void updateMessage() {

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
        isActive = true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isActive = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || isActive;
    }
}
