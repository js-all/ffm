package dev.viandox.ffm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.viandox.ffm.FFMGraphicsHelper;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(TexturedButtonWidget.class)
public class MixinTexturedButtonWidget {
        @Redirect(
            method = "renderButton",
            at = @At(
                value = "INVOKE",
                target= "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIFFIIII)V"
            )
        )
        public void redirectDrawTexture(MatrixStack matrices, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
            FFMGraphicsHelper.drawBlendingTexture(matrices, x, y, u, v, width, height, textureWidth, textureHeight);
        }
}
