package dev.viandox.ffm.gui;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class FFMCyclingOption<T> extends FFMOption<Integer> {
    private List<T> variants;
    private final boolean includeLabel;

    public FFMCyclingOption(String key, Text label, List<T> variants, boolean includeLabel) {
        super(key, label);
        this.variants = variants;
        this.includeLabel = includeLabel;
    }

    @Override
    public Integer get() {
        return value;
    }

    public T getConcrete() {
        return variants.get(value);
    }

    @Override
    public Text getLabel() {
        if(this.includeLabel) {
            return label.copy().append(this.getConcrete().toString());
        } else {
            return Text.of(this.getConcrete().toString());
        }
    }

    @Override
    public void serialize(JsonObject config) {
        config.addProperty(key, value);
    }

    @Override
    public void deserialize(JsonObject config) {
        value = config.get(key).getAsInt();
    }

    @Override
    public AbstractButtonWidget makeWidget(int x, int y, int w, int h) {
        return new FFMButtonWidget(x, y, w, h, this.getLabel(), button -> {
            value++;
            if (value >= variants.size()) {
                value = 0;
            }
            button.setMessage(this.getLabel());
        });
    }
}
