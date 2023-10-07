package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.event.TempEffectsCommon;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * A needlessly complex mixin to render frozen hearts when the player's temp falls below -50
 */
@Mixin(Gui.class)
public class MixinHeartRender
{
    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/hearts_frozen.png");
    private static final ResourceLocation ICONS_TEXTURE = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static int HEART_COUNTER = 0;

    @Inject(method = "renderHeart", at = @At("TAIL"), cancellable = true, remap = ColdSweat.REMAP_MIXINS)
    private void renderHeart(GuiGraphics guiGraphics, Gui.HeartType heartType, int x, int y, int yOffset, boolean blink, boolean halfHeart, CallbackInfo ci)
    {
        Player player = Minecraft.getInstance().player;

        if (player != null)
        {
            if (player.hasEffect(ModEffects.ICE_RESISTANCE)) return;
            double temp = Overlays.BODY_TEMP;

            // Get protection from armor underwear
            float coldLiningFactor = CSMath.blend(0.5f, 1, TempEffectsCommon.getArmorUnderProt(player, true), 0, 4);
            if (coldLiningFactor == 1) return;

            int frozenHealth = (int) (player.getMaxHealth() - player.getMaxHealth() * CSMath.blend(coldLiningFactor, 1, temp, -100, -50));
            int frozenHearts = frozenHealth / 2;
            int u = blink || heartType == Gui.HeartType.CONTAINER ? 14 : halfHeart ? 7 : 0;

            // Render frozen hearts
            RenderSystem.setShaderTexture(0, HEART_TEXTURE);
            if (HEART_COUNTER > 0 && HEART_COUNTER < frozenHearts + 1)
            {
                guiGraphics.blit(HEART_TEXTURE, x, y, 21, 0, 9, 9, 30, 14);
                guiGraphics.blit(HEART_TEXTURE, x + 1, y + 1, u, 0, 7, 7, 30, 14);
                ci.cancel();
            }
            // Render half-frozen heart if needed
            else if (HEART_COUNTER == frozenHearts + 1 && frozenHealth % 2 == 1)
            {
                guiGraphics.blit(HEART_TEXTURE, x + 1, y + 1, u, 7, 7, 7, 30, 14);
            }
        }
        RenderSystem.setShaderTexture(0, ICONS_TEXTURE);
    }

    @Inject(method = "renderHearts",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tickHeartCounter(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight,
                                  // local variables
                                  CallbackInfo ci, Gui.HeartType gui$hearttype, int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2)
    {
        if (player != null)
        {
            int playerHearts = j + k;
            HEART_COUNTER = (HEART_COUNTER + 1) % playerHearts;
        }
    }
}
