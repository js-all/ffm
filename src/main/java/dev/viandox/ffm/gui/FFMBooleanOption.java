package dev.viandox.ffm.gui;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FFMBooleanOption extends FFMOption<Boolean> {
    private Text label;
    private final boolean includeLabel;

    public FFMBooleanOption(String key, Text label, boolean includeLabel) {
        super(key, label);
        this.includeLabel = includeLabel;
    }
    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public void serialize(JsonObject config) {
        config.addProperty(this.key, this.value);
    }

    @Override
    public void deserialize(JsonObject config) {
        this.value = config.get(key).getAsBoolean();
    }
    @Override
    public Text getLabel() {
        if(this.includeLabel) {
            return label.copy().append(Text.of(value ? ": enabled" : ": disabled"));
        } else {
            return Text.of(value ? "enabled" : "disabled");
        }
    }

    @Override
    public AbstractButtonWidget makeWidget(int x, int y, int w, int h) {
        return new FFMButtonWidget(x, y, w, h, this.getLabel(), (button) -> {
           this.value = !this.value;
           button.setMessage(this.getLabel());
        });
    }
}
