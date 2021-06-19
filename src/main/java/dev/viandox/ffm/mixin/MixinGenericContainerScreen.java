package dev.viandox.ffm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.FFMUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import dev.viandox.ffm.FFMGraphicsHelper;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(GenericContainerScreen.class)
public class MixinGenericContainerScreen {
    @Redirect(
        method = "drawBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"
        )
    )
    // enable blending on chest background
    void redirectDrawBackground(GenericContainerScreen d, MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        // soon: background stained glass panes don't render.
        /*
        TODO: gonna have to implement this like so:
        - look through container and find slots that we don't want to render (and remember them)
        - convert the slots into concrete coordinates
        - stencil out the concerned slots
        - render the regular texture
        - render one by one the non-slots
        */

        List<Slot> removedSlots = d.getScreenHandler().slots.stream().filter(s -> FFMUtils.isItemStackBackgroundGlassPane(s.getStack())).collect(Collectors.toList());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        FFMGraphicsHelper.StencilHelper stencilHelper =
                new FFMGraphicsHelper.StencilHelper(FFMGraphicsHelper.StencilHelper.RESET_OPENGL_STATE_ON_END);
        stencilHelper.beginStencil();

        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("ffm", "pixel.png"));
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);

        for(Slot s : removedSlots) {
            int x1 = x + s.x - 1;
            int y1 = y + s.y - v - 1;
            int x2 = x + s.x + 17;
            int y2 = y + s.y + 17 - v;

            // skip if not in texture boundaries (because minecraft renders chest weird)
            if(y1 < y) continue;
            if(y2 > y + height) continue;

            bufferBuilder.vertex(matrix, x2, y1, 0).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix, x1, y2, 0).color(255, 255, 255, 255).next();
            bufferBuilder.vertex(matrix, x2, y2, 0).color(255, 255, 255, 255).next();
        }

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        stencilHelper.useStencil();
        // inverse stencil
        RenderSystem.stencilFunc(GL11.GL_GREATER, 0xff, 0xff);

        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/gui/container/generic_54.png"));
        FFMGraphicsHelper.drawBlendingTexture(matrices, x, y, u, v, width, height, 0);
        stencilHelper.endStencil();


        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);

        for(Slot s : removedSlots) {
            int x1 = x + s.x - 1;
            int y1 = y + s.y - v - 1;
            int x2 = x + s.x + 17;
            int y2 = y + s.y + 17 - v;

            // skip if not in texture boundaries (because minecraft renders chest weird)
            if(y2 < y) continue;
            if(y1 > y + height) continue;

            bufferBuilder.vertex(matrix, x2, y1, 0).color(255, 255, 255, 255).texture(0.5625f, 0.015625f).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(255, 255, 255, 255).texture(0.5000f, 0.015625f).next();
            bufferBuilder.vertex(matrix, x1, y2, 0).color(255, 255, 255, 255).texture(0.0500f, 0.0625f).next();
            bufferBuilder.vertex(matrix, x2, y2, 0).color(255, 255, 255, 255).texture(0.5625f, 0.0625f).next();
        }

        bufferBuilder.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.draw(bufferBuilder);
        bufferBuilder.reset();
    }
}
