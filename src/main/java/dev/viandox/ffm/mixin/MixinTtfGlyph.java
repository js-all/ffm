package dev.viandox.ffm.mixin;

import dev.viandox.ffm.ITtfGlyph;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.stb.STBTTFontinfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(targets = "net/minecraft/client/font/TrueTypeFont$TtfGlyph")
public class MixinTtfGlyph implements ITtfGlyph {
    @Shadow
    @Mutable
    private int width;
    @Shadow
    @Mutable
    private int height;

    private static final int fac = 1;

    @Inject(
            method = "<init>(Lnet/minecraft/client/font/TrueTypeFont;IIIIFFILorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
            at = @At("RETURN")
    )
    public void changeSize(CallbackInfo ci) {
        this.width *= fac;
        this.height *= fac;
    }

//    @Inject(
//            method = "upload",
//            at = @At("HEAD")
//    )
//    public void log(CallbackInfo ci) {
//        System.out.println(this.width);
//    }

    @Redirect(
            method = "upload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;makeGlyphBitmapSubpixel(Lorg/lwjgl/stb/STBTTFontinfo;IIIFFFFII)V"
            )
    )
    public void RedirectMakeGlyphBitmapSubpixel(NativeImage img, STBTTFontinfo fontInfo, int glyphIndex, int width, int height, float scaleX, float scaleY, float shiftX, float shiftY, int startX, int startY) {
        img.makeGlyphBitmapSubpixel(fontInfo, glyphIndex, width, height, scaleX * fac, scaleY * fac, shiftX, shiftY, startX, startY);
    }
}
