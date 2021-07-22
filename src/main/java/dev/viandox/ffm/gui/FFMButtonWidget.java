package dev.viandox.ffm.gui;

import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.FFMGraphicsHelper;
import dev.viandox.ffm.config.Config;
import dev.viandox.ffm.interpolation.InterpolableColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.time.Instant;

public class FFMButtonWidget extends ButtonWidget {
    private boolean wasHovered = false;
    private final InterpolableColor backgroundColor = InterpolableColor.easeInOut(Config.accentColor);

    public FFMButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    public FFMButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(wasHovered != this.isHovered()) {
            if(this.isHovered()) this.backgroundColor.set(Config.accentColorBright);
            else this.backgroundColor.set(Config.accentColor);
        }

        FFMGraphicsHelper.drawRoundedRect(matrices, x, y, x + width, y+ height, 0, backgroundColor.getInt(), 9);

        drawCenteredText(matrices,
                MinecraftClient.getInstance().textRenderer,
                this.getMessage(),
                this.x + this.width / 2,
                this.y + (this.height - 8) / 2,
                0xffffffff);

        wasHovered = this.isHovered();
    }
}
