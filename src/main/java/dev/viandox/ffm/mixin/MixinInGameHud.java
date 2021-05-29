package dev.viandox.ffm.mixin;

import dev.viandox.ffm.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
            if(running.compareTo(HudConfig.playerAbilityDisplayDuration) <= 0) {
                // i honestly don't know how to comment this so
                // https://www.desmos.com/calculator/myesx8j0wt
                long t = HudConfig.playerAbilityTransitionDuration.toNanos();
                double half_d = (double) (HudConfig.playerAbilityDisplayDuration.toNanos()) * 0.5;
                long r = running.toNanos();
                double x = Math.min((-Math.abs(r-half_d))+half_d, (double) t) / (double) t;
                double fac = HudConfig.interpolationPoint1 * 3 * Math.pow(1-x, 2)*x+HudConfig.interpolationPoint2*3*(1-x)*x*x+x*x*x;
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
            if(!PlayerListScrapper.commissions.isEmpty() && PlayerListScrapper.area.equals("Dwarven Mines")) {
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

            renderDungeonMap(matrices);
        }
    }

    public void renderDungeonMap(MatrixStack matrices) {
        VertexConsumerProvider vertexConsumers = this.client.getBufferBuilders().getEntityVertexConsumers();

        ItemStack mapSlot = this.client.player.inventory.main.get(8);
        Item.Settings settings = new Item.Settings();

        if(mapSlot.isEmpty() || mapSlot.getItem().equals(new Item()))

        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
        matrices.scale(0.38F, 0.38F, 0.38F);
        matrices.translate(-0.5D, -0.5D, 0.0D);
        matrices.scale(0.0078125F, 0.0078125F, 0.0078125F);
        MapState mapState = FilledMapItem.getOrCreateMapState(stack, this.client.world);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(mapState == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = matrices.peek().getModel();
        vertexConsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).next();
        vertexConsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).next();
        vertexConsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).next();
        vertexConsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).next();
        if (mapState != null) {
            this.client.gameRenderer.getMapRenderer().draw(matrices, vertexConsumers, mapState, false, 255);
        }
    }
}
