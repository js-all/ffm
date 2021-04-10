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
    void afterOldDrawingCallback(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y, CallbackInfo ci, int i, int k, int l, int m, int n, int o, int p, int q, int r, Tessellator tessellator, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        int color = 0xffffffff;
        try {
            color = this.getTextHandler().getStyleAt(lines.get(0), 0).getColor().getRgb();
        } catch (Exception e) {}

        if((color & 0x00ffffff) == 0x00ffffff) {
            color = Config.ToolTipDefaultColor;
        }
        float alp = Config.ToolTipBodyOpactity;
        int cr = ((color >> 16) & 0xff);
        int cg = ((color >>  8) & 0xff);
        int cb = ((color >>  0) & 0xff);
        

        float bg = 0.0588f;
        float b = Config.ToolTipMarginSize;
        int h = (int) (b * 1.25);
        bufferBuilder.end();
        bufferBuilder.reset();
        this.client.getTextureManager().bindTexture(new Identifier("ffm", "corner.png"));
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        // i know this looks like a lot, but keep in mind that the original minecraft tooltip
        // uses 36 of those bufferBuilder.vertex calls.
        // (there are 32 here)
        // top left corner
        bufferBuilder.vertex((  k + b  ), (      l - b      ), 400).texture(1.0F, 0.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k - b  ), (      l - b      ), 400).texture(0.0F, 0.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k - b  ), (      l + b      ), 400).texture(0.0F, 1.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k + b  ), (      l + b      ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, 255).next();
        // top right corner
        bufferBuilder.vertex((k + i + b), (      l - b      ), 400).texture(0.0F, 0.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((k + i - b), (      l - b      ), 400).texture(1.0F, 0.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((k + i - b), (      l + b      ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((k + i + b), (      l + b      ), 400).texture(0.0F, 1.0F).color(cr, cg, cb, 255).next();
        // bottom left corner
        bufferBuilder.vertex((  k + b  ), (l + n + 6 - b + h), 400).texture(1.0F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k - b  ), (l + n + 6 - b + h), 400).texture(0.0F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k - b  ), (l + n + 6 + b + h), 400).texture(0.0F, 0.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k + b  ), (l + n + 6 + b + h), 400).texture(1.0F, 0.0F).color(bg, bg, bg, alp).next();
        // bottom right corner
        bufferBuilder.vertex((k + i + b), (l + n + 6 - b + h), 400).texture(0.0F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((k + i - b), (l + n + 6 - b + h), 400).texture(1.0F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((k + i - b), (l + n + 6 + b + h), 400).texture(1.0F, 0.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((k + i + b), (l + n + 6 + b + h), 400).texture(0.0F, 0.0F).color(bg, bg, bg, alp).next();
        // mid piece between corner
        bufferBuilder.vertex((k + i - b), (      l - b      ), 400).texture(0.9F, 0.9F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k + b  ), (      l - b      ), 400).texture(1.0F, 0.9F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k + b  ), (      l + b      ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((k + i - b), (      l + b      ), 400).texture(0.9F, 1.0F).color(cr, cg, cb, 255).next();
        // bottom part of header, under corner, full width
        bufferBuilder.vertex((k + i + b), (      l + b      ), 400).texture(0.9F, 0.9F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k - b  ), (      l + b      ), 400).texture(1.0F, 0.9F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((  k - b  ), (   l + 1.7 * b   ), 400).texture(1.0F, 1.0F).color(cr, cg, cb, 255).next();
        bufferBuilder.vertex((k + i + b), (   l + 1.7 * b   ), 400).texture(0.9F, 1.0F).color(cr, cg, cb, 255).next();
        // most of body
        bufferBuilder.vertex((k + i + b), (   l + 1.7 * b   ), 400).texture(0.9F, 0.9F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k - b  ), (   l + 1.7 * b   ), 400).texture(1.0F, 0.9F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k - b  ), (l + n + 6 - b + h), 400).texture(1.0F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((k + i + b), (l + n + 6 - b + h), 400).texture(0.9F, 1.0F).color(bg, bg, bg, alp).next();
        // mid bottom piece between corners
        bufferBuilder.vertex((k + i - b), (l + n + 6 - b + h), 400).texture(0.9F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k + b  ), (l + n + 6 - b + h), 400).texture(1.0F, 1.0F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((  k + b  ), (l + n + 6 + b + h), 400).texture(1.0F, 0.9F).color(bg, bg, bg, alp).next();
        bufferBuilder.vertex((k + i - b), (l + n + 6 + b + h), 400).texture(0.9F, 0.9F).color(bg, bg, bg, alp).next();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();

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
    List<? extends OrderedText> updateFirstMemberOfLines(List<? extends OrderedText> list) {
        List<OrderedText> lines = new ArrayList<>(list);

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
    int addPaddingtoFirstLine(int l) {
        if(this.renderOrderedTooltipLastSValue == 0) {
            return l + (int) (Config.ToolTipMarginSize * 1.25);
        }
        return l;
    }
    @Inject(
        method = "renderOrderedTooltip",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;get(I)Ljava/lang/Object;"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    void getSValueAndSetPrivateField(MatrixStack matrices, List<?> lines, int x, int y, CallbackInfo ci, int i, int k, int l, int m, int n, int o, int p, int q, int r, Tessellator tessellator, BufferBuilder bufferBuilder, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate, int s) {
        this.renderOrderedTooltipLastSValue = s;
    }
}

