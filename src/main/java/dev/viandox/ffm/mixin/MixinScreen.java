package dev.viandox.ffm.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.config.Config;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow
    TextRenderer textRenderer;
    @Shadow
    MinecraftClient client;
    // those are used to keep data in between different injections in the same method
    private int renderOrderedTooltipLastSValue;
    private int toolTipHeaderColor;

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
        int color = toolTipHeaderColor;

        // if the color is solid white (either because of the text, or because of an error, default to the config)
        if((color & 0x00ffffff) == 0x00ffffff) {
            color = Config.ToolTipDefaultColor;
        }
        // get the alpha, will be used in the background color
        int alp = (int)(Config.ToolTipBodyOpacity * 255);
        
        // c[rgba] each component of the header color
        int cr = ((color >> 16) & 0xff);
        int cg = ((color >>  8) & 0xff);
        int cb = ((color >>  0) & 0xff);
        int ca = 255;

        if (Config.ColorCorrectToolTip) {
            // limit the lightness to a level, to avoid unreadable tooltips
            float[] lch = ColorConverter.RGBtoLCH(cr, cg, cb, ColorConverter.CIE2_D65);
            lch[0] += (Config.ColorCorrectToolTipLightness - lch[0]) / 4;
            lch[1] += 20;
            int[] rgb = ColorConverter.clampRGB(ColorConverter.LCHtoRGB(lch[0], lch[1], lch[2], ColorConverter.CIE2_D65));

            cr = Math.abs(rgb[0]);
            cg = Math.abs(rgb[1]);
            cb = Math.abs(rgb[2]);
        }

        // the background greyscale color
        int BG = (int) (0.0588f * 255);
        // b[rgba] each component of the background color
        int br = BG;
        int bg = BG;
        int bb = BG;
        int ba = alp;

        // a sort of offset that shrinks the tooltip by an amount, used in single line tooltip
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

        // done now to not have the old tooltip, but still have the bufferBuilder
        // began because the original minecraft code ends it

        // get the string value of the name of the item
        AtomicReference<String> name = new AtomicReference<>("");
        lines.get(0).accept((index,  style,  codePoint) -> {
            name.set(name.get().concat(String.valueOf(Character.toChars(codePoint))));
            return true;
        });
        // don't render if empty and single lined
        if(lines.size() == 1 && name.get().equals(" ")) return;

        // if you don't understand this code (why l + n + 6 - b + h - v + f or the UV coordinates for example) don't worry
        // i don't either, just don't think about it, it mostly works, and that's all that matters.

        // the tooltip is rendered with 8 quads (font ligature recommended, eg. FiraCode):
        //
        //                  mid piece between corners
        // top left corner ─┐           │         ┌─ top right corner
        //                 ╭──┬─────────────────┬──╮ ─┐
        //   bottom part __├──┴─────────────────┴──┤  │ <- header
        //     of header   ├───────────────────────┤ ─┤
        //                 │                       │  │                   ( the rounded corners are a texture )
        //                 │                       │  │
        //  most of body ──│                       │  │ <- body
        //                 │                       │  │
        //                 │                       │  │
        //   bottom left __├──┬─────────────────┬──┤  │
        //        corner   ╰──┴─────────────────┴──╯ ─┘
        //           mid bottom piece ─┘          └─ bottom right corner

        // i know this looks like a lot, but keep in mind that the original minecraft tooltip
        // uses 36 of those bufferBuilder.vertex calls.                                 ┌ 0.9F values here are used
        // (there are 32 here)                                                          │ to render solid color while
        // top left corner                                                              ↓ still having the texture bound
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
        at = @At("HEAD")
    )
    // complicated af, but just chooses which color the header will be and make any text that color solid white
    // its this long because minecraft sucks.
    List<? extends OrderedText> updateFirstMemberOfLines(List<? extends OrderedText> list) {
        List<OrderedText> lines = new ArrayList<>(list);
        // if we only have one line, and invertedSingleLineToolTip is true, ignore
        if(lines.size() == 1 && Config.invertedSingleLineToolTip) {
            toolTipHeaderColor = 0xffffffff;
            return list;
        }
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
        // find style attached to the longest string (that is not delimited)
        int longestStyleIndex = -1;
        for(int i = 0; i < decStrings.size(); i++) {
            String currentString = decStrings.get(i);
            // if the string is delimited by [], (), {}, "", or '' ignore, this is to avoid coloring the header the color or secondary text
            // for example coloring "[Lvl 87] rat" the color of "[lvl 87]" when the correct color is obviously the one of "rat"
            if(currentString.matches("^\\s*(?:\\[.+?\\]|\\(.+?\\)|\\{.+?\\}|\".+?\"|'.+?')\\s*$")) continue;
            int lastLongestStyleStringLength = longestStyleIndex != -1 ? decStrings.get(longestStyleIndex).length() : 0;
            int currentStringLength = currentString.length();
            if(currentStringLength > lastLongestStyleStringLength) {
                longestStyleIndex = i;
            }
        }
        longestStyleIndex = longestStyleIndex == -1 ? 0 : longestStyleIndex;

        // set the header color
        try {
            toolTipHeaderColor = decStyle.get(longestStyleIndex).getColor().getRgb();
        } catch (Exception e) {
            toolTipHeaderColor = 0xffffffff;
        }
        // change every occurrence of the color of that string to solid white
        for(int i = 0; i < decStyle.size(); i++) {
            int currentColor;
            try {
                currentColor = decStyle.get(i).getColor().getRgb();
            } catch (Exception e) {
                currentColor = 0xffffffff;
            }
            // also replace gray because gray looks like shit. (on colored backgrounds)
            if(currentColor == toolTipHeaderColor) {
                decStyle.set(i, firstLineStyle.withColor(TextColor.fromRgb(0xffffffff)));
            } else if(currentColor == TextColor.fromFormatting(Formatting.GRAY).getRgb()) {
                decStyle.set(i, firstLineStyle.withColor(TextColor.fromRgb(0xffaaaaaa)));
            }
        }
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
    int addBottomPaddingToFirstLine(int l) {
        // i have the use this atrocity because a modifyVariable can't capture locals
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
    // s being at the very end, i need every other argument before it
    void getSValueAndSetPrivateField(MatrixStack matrices, List<?> lines, int x, int y, CallbackInfo ci, int i, int k, int l, int m, int n, int o, int p, int q, int r, Tessellator tessellator, BufferBuilder bufferBuilder, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate, int s) {
        this.renderOrderedTooltipLastSValue = s;
    }
}

