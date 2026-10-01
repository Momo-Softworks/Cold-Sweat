package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.client.renderer.SoulLampPose;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Poses entities' arms while holding a soulspring lamp. The pose data is attached to the render state by {@link SoulLampPose}
 */
@Mixin(HumanoidModel.class)
public class MixinSoulLampRendering
{
    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    public void poseRightArm(HumanoidRenderState state, CallbackInfo ci)
    {
        SoulLampPose pose = SoulLampPose.of(state);
        if (pose == null) return;

        boolean holdingLamp = pose.rightLamp();
        float rightArmRot = pose.rightArmRot();

        if (!CSMath.betweenInclusive(rightArmRot, -0.01, 0.01))
        {
            switch (state.rightArmPose)
            {
                case EMPTY ->
                {
                    this.rightArm.xRot = this.rightArm.xRot - rightArmRot;
                    this.rightArm.zRot = this.rightArm.zRot - (holdingLamp ? 0.05F : 0f);
                    this.rightArm.yRot = 0;
                }
                case ITEM ->
                {
                    this.rightArm.xRot = (holdingLamp ? this.rightArm.xRot * 0.15f - 0.35f : this.rightArm.xRot) - rightArmRot;
                    this.rightArm.zRot = this.rightArm.zRot - (holdingLamp ? 0.05F : 0f);
                    this.rightArm.yRot = 0;
                }
            }
        }
        RenderLampHand.transformArm(pose, this.rightArm, HumanoidArm.RIGHT);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    public void poseLeftArm(HumanoidRenderState state, CallbackInfo ci)
    {
        SoulLampPose pose = SoulLampPose.of(state);
        if (pose == null) return;

        boolean holdingLamp = pose.leftLamp();
        float leftArmRot = pose.leftArmRot();

        if (!CSMath.betweenInclusive(leftArmRot, -0.01, 0.01))
        {
            switch (state.leftArmPose)
            {
                case EMPTY ->
                {
                    this.leftArm.xRot = this.leftArm.xRot - leftArmRot;
                    this.leftArm.zRot = this.leftArm.zRot + (holdingLamp ? 0.05F : 0f);
                    this.leftArm.yRot = 0.0F;
                }
                case ITEM ->
                {
                    this.leftArm.xRot = (holdingLamp ? this.leftArm.xRot * 0.15f - 0.35f : this.leftArm.xRot) - leftArmRot;
                    this.leftArm.zRot = this.leftArm.zRot + (holdingLamp ? 0.05F : 0f);
                    this.leftArm.yRot = 0.0F;
                }
            }
        }
        RenderLampHand.transformArm(pose, this.leftArm, HumanoidArm.LEFT);
    }

    @Mixin(ItemInHandLayer.class)
    public static class HeldItem
    {
        /**
         * Slim arms hold the lamp slightly closer to the body
         */
        @Inject(method = "submitArmWithItem",
                at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
        public void shiftLampForSlimArms(ArmedEntityRenderState state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm,
                                         PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci)
        {
            if (!ConfigSettings.POSE_SOULSPRING_LAMP.get()) return;

            ItemInHandLayer<?, ?> self = (ItemInHandLayer<?, ?>) (Object) this;
            if (itemStack.is(ModItems.SOULSPRING_LAMP) && ClientOnlyHelper.isPlayerModelSlim(self))
            {   poseStack.translate((arm == HumanoidArm.RIGHT ? -0.5 : 0.5) / 16f, 0, 0);
            }
        }
    }

    @Mixin(HumanoidModel.class)
    public static class ShiftWidePlayerArm
    {
        @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
        public void shiftWidePlayerArm(HumanoidRenderState state, CallbackInfo ci)
        {
            SoulLampPose pose = SoulLampPose.of(state);
            if (pose == null) return;

            HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
            if (self instanceof PlayerModel playerModel && !ClientOnlyHelper.isPlayerModelSlim(self))
            {
                if (pose.rightLamp())
                {
                    playerModel.rightArm.y += 1;
                    if (pose.attackAnim() > 0 && pose.swingingArm() == HumanoidArm.RIGHT)
                    {   playerModel.rightArm.x -= 1;
                    }
                }
                if (pose.leftLamp())
                {
                    playerModel.leftArm.y += 1;
                    if (pose.attackAnim() > 0 && pose.swingingArm() == HumanoidArm.LEFT)
                    {   playerModel.leftArm.x += 1;
                    }
                }
            }
        }
    }

    @Mixin(Player.class)
    public static class EquipAnimation
    {
        @Inject(method = "getCurrentItemAttackStrengthDelay", at = @At("HEAD"), cancellable = true)
        public void reduceEquipDelay(CallbackInfoReturnable<Float> cir)
        {
            if (!ConfigSettings.POSE_SOULSPRING_LAMP.get()) return;

            if (((LivingEntity) (Object) this).getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.SOULSPRING_LAMP))
            {   cir.setReturnValue(0f);
            }
        }
    }
}
