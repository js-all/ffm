package dev.viandox.ffm.mixin;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.Config;
import dev.viandox.ffm.IGetTextHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow
    TextRenderer textRenderer;
    @Shadow
    MinecraftClient client;
    private int renderOrderedTooltipLastSValue;
    TextHandler getTextHandler() {
        if(this.textRenderer instanceof IGetTextHandler) {
            return ((IGetTextHandler) this.textRenderer).getTextHandler();
        }
        return null;
    }
    
    @Inject(
        method = "renderOrderedTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    /**
     * mixin into renderOrderedTooltip:
     * 
     * init buffers
     * draw original tooltip in buffers
     * MIXIN afterOldDrawingCallback <-- right here
     * draw buffers
     */
    void afterOldDrawingCallback(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y, CallbackInfo ci, int i, int k, int l, int m, int n, int o, int p, int q, int r, Tessellator tessellator, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        // get the first line's color
        int color = 0xffffffff;
        try {
            color = this.getTextHandler().getStyleAt(lines.get(0), 0).getColor().getRgb();
        } catch (Exception e) {}

        // if the color is solid white (either because of the text, or because of an error, default to the config)
        if((color & 0x00ffffff) == 0x00ffffff) {
            color = Config.ToolTipDefaultColor;
        }
        // get the alpha, will be used in the background color
        int alp = (int)(Config.ToolTipBodyOpactity * 255);
        
        // c[rgba] each component of the header color
        int cr = ((color >> 16) & 0xff);
        int cg = ((color >>  8) & 0xff);
        int cb = ((color >>  0) & 0xff);
        int ca = 255;

        // limit the brightness to a level, to avoid unreadable tooltips
        float[] hslc = ColorConverter.RGBtoHSL(cr, cg, cb);
        hslc[2] = Math.min(hslc[2], Config.ToolTipColorMaxBrightness / 100);
        int[] rgbc = ColorConverter.HSLtoRGB(hslc[0], hslc[1], hslc[2]);

        cr = rgbc[0];
        cg = rgbc[1];
        cb = rgbc[2];

        // the background greyscale color
        int BG = (int) (0.0588f * 255);
        // b[rgba] each component of the background color
        int br = BG;
        int bg = BG;
        int bb = BG; 
        int ba = alp;

        // a sort of offset that shrinks the tooltip by an ammount, used in single line tooltip
        float v = 0;
        // how much to move the entire tooltip vertically, used in single line tooltip
        int f = 0;

        // the margin
        float b = Config.ToolTipMarginSize;
        // random value to make things work, just don't touch it
        int h = (int) (b * 1.25);

        // change some value if the tooltip is single lined
        if(lines.size() == 1) {
            // change the colors, to either only background color or only header color
            if(Config.invertedSingleLineToolTip) {
                cr = br;
                cg = bg;
                cb = bb;
                ca = ba;
            } else {
                br = cr;
                bg = cg;
                bb = cb;
                ba = ca;
            }
            // set v and f to render the tooltip at the right place, the right size
            v = b;
            f = 2;
        }

        // reset buffers to clear the original tooltip and prevent it from rendering
        bufferBuilder.end();
        bufferBuilder.reset();
        // bind rounded corner texture
        this.client.getTextureManager().bindTexture(new Identifier("ffm", "corner.png"));
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);

        // if you don't understand this code (why l + n + 6 - b + h - v + f or the UV coordinates for example) don't worry
        // i don't either, just don't think about it, it mostly works, and thats all that matters.

        // the tooltip is rendered with 8 quads (font liguature recomended, eg. FiraCode):
        //
        //                  mid piece between corners
        // top left corner ─┐           │         ┌─ top right corner
        //                 ╭──┬─────────────────┬──╮ ─┐
        //   bottom part __├──┴─────────────────┴──┤  │ <- header
        //     of header   ├───────────────────────┤ ─┤
        //                 │                       │  │
        //                 │                       │  │
        //  most of body ──│                       │  │ <- body
        //                 │                       │  │
        //                 │                       │  │
        //   bottom left __├──┬─────────────────┬──┤  │
        //        corner   ╰──┴─────────────────┴──╯ ─┘
        //           mid bottom piece ─┘          └─ bottom right corner

        // i know this looks like a lot, but keep in mind that the original minecraft tooltip
        // uses 36 of those bufferBuilder.vertex calls.                             │ 0.9F values here are used 
        // (there are 32 here)                                                      │ to render solid color while
        // top left corner                                                          ↓ still having the texture bound
        bufferBuilder.vertex((  k + b  ), (      l - b + v + f      ), 400).texture(1.0F, 0.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k - b  ), (      l - b + v + f      ), 400).texture(0.0F, 0.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k - b  ), (      l + b + v + f      ), 400).texture(0.0F, 1.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k + b  ), (      l + b + v + f      ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, ca).next();
        // top right corner
        bufferBuilder.vertex((k + i + b), (      l - b + v + f      ), 400).texture(0.0F, 0.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((k + i - b), (      l - b + v + f      ), 400).texture(1.0F, 0.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((k + i - b), (      l + b + v + f      ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((k + i + b), (      l + b + v + f      ), 400).texture(0.0F, 1.0F).color(cr, cg, cb, ca).next();
        // bottom left corner
        bufferBuilder.vertex((  k + b  ), (l + n + 6 - b + h - v + f), 400).texture(1.0F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k - b  ), (l + n + 6 - b + h - v + f), 400).texture(0.0F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k - b  ), (l + n + 6 + b + h - v + f), 400).texture(0.0F, 0.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k + b  ), (l + n + 6 + b + h - v + f), 400).texture(1.0F, 0.0F).color(br, bg, bb, ba).next();
        // bottom right corner
        bufferBuilder.vertex((k + i + b), (l + n + 6 - b + h - v + f), 400).texture(0.0F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((k + i - b), (l + n + 6 - b + h - v + f), 400).texture(1.0F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((k + i - b), (l + n + 6 + b + h - v + f), 400).texture(1.0F, 0.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((k + i + b), (l + n + 6 + b + h - v + f), 400).texture(0.0F, 0.0F).color(br, bg, bb, ba).next();
        // mid piece between corner
        bufferBuilder.vertex((k + i - b), (      l - b + v + f      ), 400).texture(0.9F, 0.9F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k + b  ), (      l - b + v + f      ), 400).texture(1.0F, 0.9F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k + b  ), (      l + b + v + f      ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((k + i - b), (      l + b + v + f      ), 400).texture(0.9F, 1.0F).color(cr, cg, cb, ca).next();
        // bottom part of header, under corner, full width
        bufferBuilder.vertex((k + i + b), (      l + b + v + f      ), 400).texture(0.9F, 0.9F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k - b  ), (      l + b + v + f      ), 400).texture(1.0F, 0.9F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((  k - b  ), (     l + 1.7 * b + f     ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, ca).next();
        bufferBuilder.vertex((k + i + b), (     l + 1.7 * b + f     ), 400).texture(0.9F, 1.0F).color(cr, cg, cb, ca).next();
        // most of body
        bufferBuilder.vertex((k + i + b), (     l + 1.7 * b + f     ), 400).texture(0.9F, 0.9F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k - b  ), (     l + 1.7 * b + f     ), 400).texture(1.0F, 0.9F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k - b  ), (l + n + 6 - b + h - v + f), 400).texture(1.0F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((k + i + b), (l + n + 6 - b + h - v + f), 400).texture(0.9F, 1.0F).color(br, bg, bb, ba).next();
        // mid bottom piece between corners
        bufferBuilder.vertex((k + i - b), (l + n + 6 - b + h - v + f), 400).texture(0.9F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k + b  ), (l + n + 6 - b + h - v + f), 400).texture(1.0F, 1.0F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((  k + b  ), (l + n + 6 + b + h - v + f), 400).texture(1.0F, 0.9F).color(br, bg, bb, ba).next();
        bufferBuilder.vertex((k + i - b), (l + n + 6 + b + h - v + f), 400).texture(0.9F, 0.9F).color(br, bg, bb, ba).next();

        // draw the tooltip
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();

        // reset for the draw later in the original method, to avoid errors
        bufferBuilder.reset();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
    } 
    @ModifyVariable(
        method = "renderOrderedTooltip",
        ordinal = 0,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V"
        )
    )
    // complicated af, but simple set the color of the first line of the tooltip to solid white
    // its this long because minecraft sucks.
    List<? extends OrderedText> updateFirstMemberOfLines(List<? extends OrderedText> list) {
        List<OrderedText> lines = new ArrayList<>(list);

        // if we only have one line, and invertedSingleLineToolTip is true, ignore
        if(lines.size() == 1 && Config.invertedSingleLineToolTip) return list;

        // get the first line's style
        Style firstLineStyle;
        try {
            firstLineStyle = this.getTextHandler().getStyleAt(lines.get(0), 0);
        } catch (Exception e) {
            firstLineStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xffffffff));
        }

        // decompose ordered text in list of strings with their corresponding style
        OrderedText firstLine = lines.get(0);
        ArrayList<String> decStrings = new ArrayList<>();
        ArrayList<Style> decStyle = new ArrayList<>();

        // visit the full string
        firstLine.accept((index,  style,  codePoint) -> {
            String c = String.valueOf(Character.toChars(codePoint));
            // if the style is the same, just append to the last style's string
            if(index != 0 && style.equals(decStyle.get(decStrings.size() - 1))) {
                decStrings.set(decStrings.size() - 1, decStrings.get(decStrings.size() - 1).concat(c));
            } else {
                // otherwise just add a new string and style
                decStrings.add(c);
                decStyle.add(style);
            }
            return true;
        });
        // set the first style to solid white
        decStyle.set(0, firstLineStyle.withColor(TextColor.fromRgb(0xffffffff)));
        // reassemble the strings with style into a single orderedText
        List<OrderedText> decOrdered = new ArrayList<>();
        for(int t = 0; t < decStrings.size(); t++) {
            decOrdered.add(OrderedText.styledString(decStrings.get(t), decStyle.get(t)));
        }
        // update first line
        lines.set(0, OrderedText.concat(decOrdered));

        return lines;
    }
    @ModifyVariable(
        method = "renderOrderedTooltip",
        index = 7,
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;get(I)Ljava/lang/Object;"
        )
    )
    int addBottomPaddingtoFirstLine(int l) {
        // i have the use this attrocity because a modifyVariable can't capture locals
        if(this.renderOrderedTooltipLastSValue == 0) {
            return l + (int) (Config.ToolTipMarginSize * 1.25);
        }
        return l;
    }
    // so i capture them here instead and set renderOrderedTooltipLastSValue to keep their value.
    @Inject(
        method = "renderOrderedTooltip",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;get(I)Ljava/lang/Object;"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    // s being at the verry end, i need every other argument before it
    void getSValueAndSetPrivateField(MatrixStack matrices, List<?> lines, int x, int y, CallbackInfo ci, int i, int k, int l, int m, int n, int o, int p, int q, int r, Tessellator tessellator, BufferBuilder bufferBuilder, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate, int s) {
        this.renderOrderedTooltipLastSValue = s;
    }
}

