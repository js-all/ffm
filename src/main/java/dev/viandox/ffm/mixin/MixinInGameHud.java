package dev.viandox.ffm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.viandox.ffm.*;
import dev.viandox.ffm.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @Shadow
    MinecraftClient client;
    @Shadow
    private Text overlayMessage;
    @Shadow
    abstract TextRenderer getFontRenderer();
    @Shadow
    abstract PlayerEntity getCameraPlayer();
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;

    private static final RenderLayer MAP_BACKGROUND = RenderLayer.getText(new Identifier("textures/map/map_background.png"));
    private static final RenderLayer MAP_BACKGROUND_CHECKERBOARD = RenderLayer.getText(new Identifier("textures/map/map_background_checkerboard.png"));

    @Inject(
            method = "setOverlayMessage",
            at = @At("RETURN")
    )
    public void onActionBar(Text message, boolean tinted, CallbackInfo ci) {
        this.overlayMessage = Text.of(ActionBarScrapper.scrap(message.getString()));
    }

    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"
            )
    )
    // redirect the get armor call to return 0 and hide the armor bar if needed
    public int redirectGetArmor(PlayerEntity player) {
        if(Config.hideArmorBar) return 0;
        return player.getArmor();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/SpectatorHud;render(Lnet/minecraft/client/util/math/MatrixStack;)V",
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    void injectInRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if(!this.client.options.hudHidden) {
            LocalDateTime now = LocalDateTime.now();
            TextRenderer textRenderer = this.getFontRenderer();
            PlayerEntity plyr = this.getCameraPlayer();
            float lineHeight = textRenderer.fontHeight + 2;
            float statusHeight = 9.5f;
            float playerAir = plyr.getAir();
            float leftColumnBasis =  (float) (this.scaledHeight - 28 - (Math.ceil(plyr.getMaxHealth() / 20) + Math.ceil(plyr.getAbsorptionAmount() / 20) + (playerAir < 300 ? 1 : 0)) * statusHeight);
            float rightColumnBasis = (float) (this.scaledHeight - 30 - (1 + (Config.hideArmorBar ? 0 : 1)) * statusHeight);
            // health
            Text txt = Text.of(ActionBarScrapper.health + "/" + ActionBarScrapper.maxHealth);
            FFMGraphicsHelper.drawOutlinedText(textRenderer,
                    matrices,
                    txt,
                    this.scaledWidth / 2 - textRenderer.getWidth(txt.asOrderedText()) / 2 - 52,
                    leftColumnBasis - lineHeight,
                    0xffff0000,
                    0xff000000);
            leftColumnBasis -= lineHeight;
            // mana
            txt = Text.of(ActionBarScrapper.mana + "/" + ActionBarScrapper.maxMana);
            FFMGraphicsHelper.drawOutlinedText(textRenderer,
                    matrices,
                    txt,
                    this.scaledWidth / 2 - textRenderer.getWidth(txt.asOrderedText()) / 2 + 53,
                    rightColumnBasis - textRenderer.fontHeight,
                    0xff00aaff,
                    0xff000000);
            rightColumnBasis -= lineHeight;
            // drill fuel
            if(ActionBarScrapper.holdingDrill) {
                txt = Text.of(ActionBarScrapper.drillFuel + "/" + ActionBarScrapper.maxDrillFuel);
                FFMGraphicsHelper.drawOutlinedText(textRenderer,
                        matrices,
                        txt,
                        this.scaledWidth / 2 - textRenderer.getWidth(txt.asOrderedText()) / 2 + 53,
                        rightColumnBasis - textRenderer.fontHeight,
                        0xff00aa00,
                        0xff000000);
                rightColumnBasis -= lineHeight;
            }
            // defense
            txt = Text.of(Integer.toString(ActionBarScrapper.defense));
            FFMGraphicsHelper.drawOutlinedText(textRenderer,
                    matrices,
                    txt,
                    10,
                    this.scaledHeight - 5 - textRenderer.fontHeight,
                    0xff00ff00,
                    0xff000000);
            // speed
            txt = Text.of(Integer.toString(PlayerListScrapper.speed));
            FFMGraphicsHelper.drawOutlinedText(textRenderer,
                    matrices,
                    txt,
                    10,
                    this.scaledHeight - 7 - textRenderer.fontHeight * 2,
                    0xffffffff,
                    0xff000000);
            // item charges
            if(ActionBarScrapper.holdingChargesItem) {
                float chargesSize = 12;
                float margins = 4;
                for(int i = 0; i < ActionBarScrapper.maxCharges; i++) {
                    int color = i < ActionBarScrapper.charges ? 0xffffffff : 0xaa000000;
                    FFMGraphicsHelper.drawRoundedRect(
                            client,
                            this.scaledWidth / 2 + 94 + i * (chargesSize + margins),
                            this.scaledHeight - margins - chargesSize,
                            this.scaledWidth / 2 + 94 + i * (chargesSize + margins) + chargesSize,
                            this.scaledHeight - margins,
                            0,
                            color,
                            chargesSize / 2
                    );
                }
            }
            // ability uses
            Duration running = Duration.between(ActionBarScrapper.lastAbilityUse, now);
            if(running.compareTo(Config.playerAbilityDisplayDuration) <= 0) {
                // i honestly don't know how to comment this so
                // https://www.desmos.com/calculator/myesx8j0wt
                long t = Config.transitionDuration.toNanos();
                double half_d = (double) (Config.playerAbilityDisplayDuration.toNanos()) * 0.5;
                long r = running.toNanos();
                double x = Math.min((-Math.abs(r-half_d))+half_d, (double) t) / (double) t;
                double fac = Config.interpolationPoint1 * 3 * Math.pow(1-x, 2)*x+ Config.interpolationPoint2*3*(1-x)*x*x+x*x*x;
                float height = 20;

                OrderedText ability = OrderedText.concat(
                        OrderedText.styledString(ActionBarScrapper.abilityUseName, Style.EMPTY.withColor(TextColor.fromRgb(0xff00ddff))),
                        OrderedText.styledString(" -" + ActionBarScrapper.abilityUseManaCost, Style.EMPTY.withColor(TextColor.fromRgb(0xffffaa00)))
                );
                int textWidth = textRenderer.getWidth(ability);
                float half_text_width = (float) (textWidth / 2.0);
                float margins = 5;
                FFMGraphicsHelper.drawRoundedRect(
                        client,
                        (float) (0.75 * this.scaledWidth - half_text_width - margins),
                        (float) (this.scaledHeight + 0 - fac * height),
                        (float) (0.75 * this.scaledWidth + half_text_width + margins),
                        (float) (this.scaledHeight + height - fac * height),
                        0,
                        128, 0, 0, 0,
                        8
                );
                textRenderer.draw(matrices,
                        ability,
                        (float) (0.75 * this.scaledWidth - half_text_width),
                        (float) (this.scaledHeight + 0 - fac * height + 5),
                        0xffffffff);
            }
            // commissions
            if(!PlayerListScrapper.commissions.isEmpty() && PlayerListScrapper.hasCommissions) {
                float cornerSize = 10f;
                float paddingLeft = 20f;
                float marginLeft = -cornerSize;
                float marginTop = 50f;
                float textWidth = textRenderer.getWidth("commissions: ");
                for(Map.Entry<String, String> entry : PlayerListScrapper.commissions.entrySet()) {
                    textWidth = Math.max(textWidth, textRenderer.getWidth(entry.getKey() + ": " + entry.getValue()));
                }
                textWidth += paddingLeft;
                FFMGraphicsHelper.drawRoundedRect(client,
                        marginLeft - cornerSize,
                        marginTop - cornerSize,
                        marginLeft + textWidth + cornerSize,
                        marginTop + lineHeight + lineHeight * PlayerListScrapper.commissions.size() + cornerSize,
                        0,
                        128,
                        0,
                        0,
                        0,
                        cornerSize);

                textRenderer.draw(matrices, Text.of("commissions:"), marginLeft + paddingLeft, marginTop, 0xffffffff);
                float i = marginTop + lineHeight;
                for (Map.Entry<String, String> entry : PlayerListScrapper.commissions.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    textRenderer.draw(matrices, Text.of(String.format("%s: %s", k, v)), marginLeft + paddingLeft, i, 0xffcccccc);
                    i += lineHeight;
                }
            }

            if(FastMenu.isOpen()) FastMenu.render(matrices);
            renderDungeonMap(matrices);
        }
    }

    private float lastPlayerPosX = 0;
    private float lastPlayerPosY = 0;
    private float oldPlayerPosX = 0;
    private float oldPlayerPosY = 0;
    private LocalDateTime lastPlayerChange = LocalDateTime.now();

    public void renderDungeonMap(MatrixStack matrices) {
        // get the itemStack in the last hotbar slot
        ItemStack stack = this.client.player.inventory.main.get(8);

        // skip if not a map (or is the dungeon score map)
        if(stack.isEmpty() || stack.getItem() != Registry.ITEM.get(new Identifier("minecraft:filled_map")) || stack.getName().getString().contains("Score"))
            return;
        // get map state and bufferBuilder
        MapState mapState = FilledMapItem.getOrCreateMapState(stack, this.client.world);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        // begin rendering
        matrices.push();
        matrices.translate(Config.globalMapTranslate.getX(), Config.globalMapTranslate.getY(), Config.globalMapTranslate.getZ());
        matrices.scale(Config.globalMapScale, Config.globalMapScale, 1);

        if (mapState != null) {
            // get the players' mapIcons
            MapIcon playerIcon = null;
            ArrayList<MapIcon> otherPlayersIcon = new ArrayList<>();

            for (MapIcon v : mapState.icons.values()) {
                if (v.getTypeId() == (byte) 3) { // other player (=> BLUE_MARKER type)
                    otherPlayersIcon.add(v);
                } else if (v.getTypeId() == (byte) 1) { // the player (=> FRAME type)
                    playerIcon = v;
                }
            }

            // get the player pos
            float playerIconPosX = playerIcon == null ? lastPlayerPosX : playerIcon.getX() / -2;
            float playerIconPosY = playerIcon == null ? lastPlayerPosY : playerIcon.getZ() / -2;

            // update timestamps (for interpolation)
            if(lastPlayerPosX != playerIconPosX || lastPlayerPosY != playerIconPosY) {
                lastPlayerChange = LocalDateTime.now();
                oldPlayerPosX = lastPlayerPosX;
                oldPlayerPosY = lastPlayerPosY;
            }

            // interpolate
            long elapsedNanos = Duration.between(lastPlayerChange, LocalDateTime.now()).toNanos();
            long maxElapsedNanos = Config.mapChangeInterval.toNanos();
            // when t is 0, the change just happened, when it is 1 it has been at least HudConfig.mapChangeInterval.
            double t = (double) elapsedNanos /  (double) maxElapsedNanos;
            // clamp to one to avoid interpolation going further than it should
            t = t >= 1 ? 1 : t;

            // interpolate
            float interpolatedPlayerPosX = MathHelper.lerp((float) t, oldPlayerPosX, lastPlayerPosX);
            float interpolatedPlayerPosY = MathHelper.lerp((float) t, oldPlayerPosY, lastPlayerPosY);

            float playerRotation = client.getCameraEntity().yaw;

            // draw background
            client.getTextureManager().bindTexture(new Identifier("ffm", "map_background.png"));
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
            Matrix4f matrix = matrices.peek().getModel();
            bufferBuilder.vertex(matrix, 131, -3f, 0).color(255, 255, 255, 255).texture(1, 0).next();
            bufferBuilder.vertex(matrix, -3f, -3f, 0).color(255, 255, 255, 255).texture(0, 0).next();
            bufferBuilder.vertex(matrix, -3f, 131, 0).color(255, 255, 255, 255).texture(0, 1).next();
            bufferBuilder.vertex(matrix, 131, 131, 0).color(255, 255, 255, 255).texture(1, 1).next();
            bufferBuilder.end();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BufferRenderer.draw(bufferBuilder);

            FFMGraphicsHelper.StencilHelper stencilHelper =
                    new FFMGraphicsHelper.StencilHelper(FFMGraphicsHelper.StencilHelper.RESET_OPENGL_STATE_ON_END);
            stencilHelper.beginStencil();

            // draw circle mask
            client.getTextureManager().bindTexture(new Identifier("ffm", "circle.png"));
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);

            bufferBuilder.vertex(matrix, 128,   0, 0).color(255, 255, 255, 255).texture(1, 0).next();
            bufferBuilder.vertex(matrix,   0,   0, 0).color(255, 255, 255, 255).texture(0, 0).next();
            bufferBuilder.vertex(matrix,   0, 128, 0).color(255, 255, 255, 255).texture(0, 1).next();
            bufferBuilder.vertex(matrix, 128, 128, 0).color(255, 255, 255, 255).texture(1, 1).next();
            bufferBuilder.end();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BufferRenderer.draw(bufferBuilder);

            // use stencil (or mask)
            stencilHelper.useStencil();

            matrices.push();
            // translate scale and rotate map
            matrices.translate(64, 64, 0);
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-playerRotation + 180));
            matrices.scale(Config.localMapScale, Config.localMapScale, 1);
            matrices.translate(-64, -64, 0);
            matrices.translate(interpolatedPlayerPosX, interpolatedPlayerPosY, 0);
            // draw map
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(bufferBuilder);
            this.client.gameRenderer.getMapRenderer().draw(matrices, immediate, mapState, true, 15728880);
            immediate.draw();

            // colors are wrong without that
            client.getTextureManager().bindTexture(new Identifier("ffm", "pixel.png"));
            // draw other players
            for(MapIcon marker : otherPlayersIcon) {
                matrices.push();
                matrices.translate(64 + (float)(marker.getX()) / 2, 64 + (float)(marker.getZ()) / 2, 0);
                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float)(marker.getRotation()) / 16 * 360 + 180f));
                // render on 0,0, to rotate with the matrices
                FFMGraphicsHelper.drawOutlinedArrow(matrices, bufferBuilder,
                        0, 0, 10,
                        Config.mapArrowWidth, Config.mapArrowHeight,
                        Config.mapArrowButtHeight,
                        Config.mapArrowOutlineWidth,
                        0xff60a8d1, 0xff38779c, Config.mapArrowOutlineColor);
                matrices.pop();
            }

            matrices.pop();
            // clear stencil
            stencilHelper.endStencil();

            lastPlayerPosX = playerIconPosX;
            lastPlayerPosY = playerIconPosY;
        }
        // draw player arrow (doesn't care about the above because it will always be centered
        Matrix4f matrix = matrices.peek().getModel();

        FFMGraphicsHelper.drawOutlinedArrow(matrices, bufferBuilder,
                64, 64, 10,
                Config.mapArrowWidth, Config.mapArrowHeight,
                Config.mapArrowButtHeight,
                Config.mapArrowOutlineWidth,
                0xffffffff, 0xffaaaaaa, Config.mapArrowOutlineColor);

        matrices.pop();
    }
}
