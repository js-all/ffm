package dev.viandox.ffm.gui;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;

public abstract class FFMOption<T> {
    protected T value;
    protected String key;
    protected Text label;

    public FFMOption(String key, Text label) {
        this.key = key;
        this.label = label;
    }

    public abstract T get();
    public abstract Text getLabel();
    public abstract void serialize(JsonObject config);
    public abstract void deserialize(JsonObject config);
    public abstract AbstractButtonWidget makeWidget(int x, int y, int w, int h);
}
