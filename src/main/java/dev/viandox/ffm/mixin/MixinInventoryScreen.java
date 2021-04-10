package dev.viandox.ffm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.viandox.ffm.FFMGraphicsHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {

    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
        )
    )
    void redirectDrawBackground(InventoryScreen d, MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        FFMGraphicsHelper.drawBlendingTexture(matrices, x, y, u, v, width, height, 0);
    }
}
