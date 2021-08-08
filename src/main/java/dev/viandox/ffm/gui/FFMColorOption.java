package dev.viandox.ffm.gui;

import com.google.gson.JsonObject;
import dev.viandox.ffm.ColorConverter;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;

public class FFMColorOption extends FFMOption<Integer>{
    private final boolean alpha;
    public FFMColorOption(String key, int color, Text label, boolean alpha) {
        super(key, label);
        this.value = color;
        this.alpha = alpha;
    }

    @Override
    public Integer get() {
        return value;
    }

    public int[] getArray() {
        return ColorConverter.INTtoRGBA(value);
    }

    @Override
    public Text getLabel() {
        return Text.of("");
    }

    @Override
    public void serialize(JsonObject config) {
        int[] rgba = ColorConverter.INTtoRGBA(value);
        float alpha = (float) rgba[3] / 255;
        config.addProperty(key, String.format("rgba(%d, %d, %d, %f)", rgba[0], rgba[1], rgba[2], alpha));
    }

    @Override
    public void deserialize(JsonObject config) {
        // skip if the key isn't here, as the default value is prefered in that case
        if(!config.has(key)) return;
        String v = config.get(key).getAsString();
        String[] tokens = v.replaceAll("^rgba\\((.+)\\)$", "$1").split(",\\s*");
        int r = Integer.parseInt(tokens[0]);
        int g = Integer.parseInt(tokens[1]);
        int b = Integer.parseInt(tokens[2]);
        int a = (int)(Float.parseFloat(tokens[3]) * 255);
        this.value = ColorConverter.RGBAtoINT(new int[] {r, g, b, a});
    }

    @Override
    public AbstractButtonWidget makeWidget(int x, int y, int w, int h) {
        return new FFMColorPickerWidget(x, y, w, h, this.getLabel(), value, alpha, (colorPicker, value) -> {
            this.value = value;
        });
    }
}
