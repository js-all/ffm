package dev.viandox.ffm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.viandox.ffm.FFMGraphicsHelper;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(AbstractInventoryScreen.class)
public class MixinAbstractInventoryScreen {
    @Redirect(
        method = "drawStatusEffectBackgrounds",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
        )
    )
    void redirectDrawTexture(AbstractInventoryScreen<?> a, MatrixStack matrixStack, int i, int k, int j, int h, int m, int l) {
        FFMGraphicsHelper.drawBlendingTexture(matrixStack, i, k, j, h, m, l, 0);
    }
}
