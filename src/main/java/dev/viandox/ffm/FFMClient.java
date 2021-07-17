package dev.viandox.ffm;

import dev.viandox.ffm.gui.SettingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class FFMClient implements ClientModInitializer {
    private boolean wasOpenFastMenuPressed = false;

    @Override
    public  void onInitializeClient() {
        KeyBinding openfastmenu = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.ffm.open_fast_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.ffm.category"));
        KeyBinding openOptions = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.ffm.open_options",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "key.ffm.category"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openfastmenu.isPressed() && !wasOpenFastMenuPressed) {
                FastMenu.open();
            } else if(!openfastmenu.isPressed() && wasOpenFastMenuPressed) {
                FastMenu.close();
            }
            wasOpenFastMenuPressed = openfastmenu.isPressed();
            if(openOptions.wasPressed()) {
                MinecraftClient.getInstance().openScreen(new SettingScreen());
            }
        });

    }
}
