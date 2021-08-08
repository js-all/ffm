package dev.viandox.ffm.gui;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;

public class FFMSliderOption extends FFMOption<Double> {
    double rangeStart;
    double rangeEnd;
    public FFMSliderOption(String key, double value, Text label, double rangeStart, double rangeEnd) {
        super(key, label);
        this.value = (value - rangeStart) / (rangeEnd - rangeStart);
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    @Override
    public Double get() {
        return this.value;
    }

    public double getRange() {
        return rangeStart + value * (rangeEnd - rangeStart);
    }

    @Override
    public Text getLabel() {
        return this.label;
    }

    @Override
    public void serialize(JsonObject config) {
        config.addProperty(this.key, this.value);
    }

    @Override
    public void deserialize(JsonObject config) {
        if(config.has(key)) value = config.get(key).getAsDouble();
    }

    @Override
    public AbstractButtonWidget makeWidget(int x, int y, int w, int h) {
        return new FFMSliderWidget(x, y, w, h, this.label, this.value, (slider, value) -> {
            this.value = value;
            slider.setMessage(Text.of(String.format("%.2f", getRange())));
        });
    }
}
