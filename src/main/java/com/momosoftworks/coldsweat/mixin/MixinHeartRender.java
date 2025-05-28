package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.api.event.vanilla.RenderHeartEvent;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * A needlessly complex mixin to render frozen hearts when the player's temp falls below -50
 */
@Mixin(ForgeIngameGui.class)
public abstract class MixinHeartRender
{
    private static int HEART_INDEX = 0;
    private static boolean IS_CONTAINER = false;

    @Inject(method = "renderHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/gui/ForgeIngameGui;blit(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIIII)V", shift = At.Shift.AFTER),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I", ordinal = 3),
                           to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V")),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, remap = false)
    private void renderHeart(int width, int height, MatrixStack ps, CallbackInfo ci,
                             // Locals
                             PlayerEntity player, int health, boolean blink, int healthLast,
                             ModifiableAttributeInstance attrMaxHealth, float healthMax,
                             float absorb, int healthRows, int rowHeight, int left,
                             int top, int regen, int v, int BACKGROUND, int u,
                             float absorbRemaining, int i, int row, int x, int y)
    {
        int hearts = CSMath.ceil(health / 2d);
        int lastHeartIndex = (int) (healthMax / 2 - hearts);
        boolean halfHeart = HEART_INDEX == lastHeartIndex + 1 && health % 2 == 1;
        RenderHeartEvent.HeartType heartType;
        if (IS_CONTAINER) heartType = RenderHeartEvent.HeartType.CONTAINER;
        else if (absorbRemaining > 0) heartType = RenderHeartEvent.HeartType.ABSORBING;
        else if (player.hasEffect(Effects.POISON)) heartType = RenderHeartEvent.HeartType.POISONED;
        else if (player.hasEffect(Effects.WITHER)) heartType = RenderHeartEvent.HeartType.WITHERED;
        else heartType = RenderHeartEvent.HeartType.NORMAL;
        MinecraftForge.EVENT_BUS.post(new RenderHeartEvent(ps, heartType, x, y, blink, halfHeart));
        IS_CONTAINER = false;
    }

    @Inject(method = "renderHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I", ordinal = 4),
            remap = false)
    private void incrementHeartIndex(int width, int height, MatrixStack mStack, CallbackInfo ci)
    {   HEART_INDEX++;
        IS_CONTAINER = true;
    }

    @Inject(method = "renderHealth", at = @At(value = "HEAD"), remap = false)
    private void resetHeartIndex(int width, int height, MatrixStack mStack, CallbackInfo ci)
    {   HEART_INDEX = 0;
    }
}
