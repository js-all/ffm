package dev.viandox.ffm.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class FFMLabelWidget extends DrawableHelper implements Drawable, Element {
    private final boolean centered;
    private final Text label;
    private final int x;
    private final int y;
    private final int color;
    public FFMLabelWidget(int x, int y, Text label, boolean centered, int color) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.color = color;
        this.centered = centered;
    }
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(centered) {
            drawCenteredText(matrices, client.textRenderer, label, x, y, color);
        } else {
            client.textRenderer.draw(matrices, label, x, y, color);
        }
    }
}
