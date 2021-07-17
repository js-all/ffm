package dev.viandox.ffm.mixin;

import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import dev.viandox.ffm.config.Config;
import dev.viandox.ffm.IGetTextHandler;

@Mixin(TextRenderer.class)
public class MixinTextRenderer implements IGetTextHandler {
	private static Boolean shadow = Config.TextShadow;

	@Shadow
	TextHandler handler;

	@ModifyVariable(
		method = "drawInternal(Ljava/lang/String;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZIIZ)I",
		at = @At("HEAD"),
		ordinal = 0
	)
	private boolean setTextShadow(boolean s) { 
		return shadow == null ? s : shadow;
	}
	@ModifyVariable(
		method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I",
		at = @At("HEAD"),
		ordinal = 0
	)
	private boolean setOrderedTextShadow(boolean s) {
		return shadow == null ? s : shadow;
	}
	@Override
	public TextHandler getTextHandler() {
		return this.handler;
	}
}
