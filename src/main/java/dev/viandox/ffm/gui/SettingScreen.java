package dev.viandox.ffm.gui;

import dev.viandox.ffm.config.Config;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
//TODO: this will be reworked heavily
public class SettingScreen extends Screen {
    public SettingScreen() {
        super(Text.of("FFM Options"));
    }

    @Override
    protected void init() {
        super.init();
        int x = 10;
        int y = 40;
        for(FFMOption<?> opt : Config.options) {
            this.addChild(new FFMLabelWidget(x, y + 5, opt.label, false, 0xffffffff));
            int w = opt instanceof FFMColorOption ? 20 : 100;
            int h = opt instanceof FFMSliderOption ? 5 : 20;
            int x1 = opt instanceof FFMColorOption ? x + 190 : x + 150;
            int y1 = opt instanceof FFMSliderOption ? y + 7 : y;
            this.addButton(opt.makeWidget(x1, y1, w, h));
            y += 30;
            if(y + 20 >= height) {
                y = 40;
                x += 260;
            }
        }
        this.addButton(new FFMButtonWidget(width / 2 - 50, height - 30, 100, 20, Text.of("done"), button -> {
            assert client != null;
            try {
                Config.serialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.openScreen(null);
        }));
    }

    @Override
    public void onClose() {
        try {
            Config.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onClose();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
        this.children()
                .stream()
                .map(e -> e instanceof Drawable ? (Drawable)e : null)
                .filter(Objects::nonNull)
                .forEach(e -> e.render(matrices, mouseX, mouseY, delta));
    }
}
