package dev.viandox.ffm.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.BiConsumer;

public class FFMSliderWidget extends SliderWidget {
    private BiConsumer<FFMSliderWidget, Double> onChange;
    private boolean drawLabel;
    public FFMSliderWidget(int x, int y, int width, int height, Text text, double value, BiConsumer<FFMSliderWidget, Double> onChange, boolean drawLabel) {
        super(x, y, width, height, text, value);
        this.onChange = onChange;
        this.drawLabel = drawLabel;
        this.updateMessage();
    }

    @Override
    protected void renderBg(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
        client.getTextureManager().bindTexture(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHovered() ? 2 : 1) * 20;
        this.drawTexture(matrices, this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
        this.drawTexture(matrices, this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
    }

    @Override
    protected void updateMessage() {
        if(this.drawLabel) return;
        this.setMessage(Text.of(""));
    }

    @Override
    protected void applyValue() {
        this.onChange.accept(this, value);
    }
}
