package dev.viandox.ffm.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SettingScreen extends Screen {
    public SettingScreen() {
        super(Text.of("FFM Options"));
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new FFMButtonWidget(20, 20, 100, 20, Text.of("button"), (button) -> {

        }));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
