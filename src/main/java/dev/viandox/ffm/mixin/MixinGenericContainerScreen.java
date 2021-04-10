package dev.viandox.ffm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import dev.viandox.ffm.FFMGraphicsHelper;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GenericContainerScreen.class)
public class MixinGenericContainerScreen {
    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
        )
    )
    void redirectDrawBackground(GenericContainerScreen d, MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        FFMGraphicsHelper.drawBlendingTexture(matrices, x, y, u, v, width, height, 0);
    }
}
