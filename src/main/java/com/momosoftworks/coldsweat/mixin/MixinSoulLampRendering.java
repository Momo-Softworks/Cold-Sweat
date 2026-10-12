package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The lamp arm's pose is applied by {@link RenderLampHand#getLampArmPose()}
 */
@Mixin(HumanoidModel.class)
public class MixinSoulLampRendering
{
    HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

    PartPose lampArmPose;

    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    @Shadow
    private void poseRightArm(LivingEntity entity) {}

    @Shadow
    private void poseLeftArm(LivingEntity entity) {}

    @Shadow
    private HumanoidArm getAttackArm(LivingEntity entity) { return null; }

    /**
     * Two-handed poses take the lamp arm. Must run before vanilla poses the arms
     */
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    public void updateLampArmPoses(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, CallbackInfo ci)
    {
        RenderLampHand.updateArmPose(model, entity, HumanoidArm.RIGHT);
        RenderLampHand.updateArmPose(model, entity, HumanoidArm.LEFT);
    }

    /**
     * Vanilla doesn't pose the lamp arm while the other arm is using an item, and a loaded crossbow's pose overwrites it
     */
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAttackAnimation(Lnet/minecraft/world/entity/LivingEntity;F)V"))
    public void poseIdleLampArm(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, CallbackInfo ci)
    {
        if (RenderLampHand.shouldReposeArm(model, entity, HumanoidArm.RIGHT))
        {   this.poseRightArm(entity);
        }
        if (RenderLampHand.shouldReposeArm(model, entity, HumanoidArm.LEFT))
        {   this.poseLeftArm(entity);
        }
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    public void lowerRightArm(LivingEntity entity, CallbackInfo ci)
    {   RenderLampHand.lowerArm(model, entity, HumanoidArm.RIGHT);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    public void lowerLeftArm(LivingEntity entity, CallbackInfo ci)
    {   RenderLampHand.lowerArm(model, entity, HumanoidArm.LEFT);
    }

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"))
    public void storeLampArmPose(LivingEntity entity, float age, CallbackInfo ci)
    {
        lampArmPose = null;
        if (model.attackTime <= 0) return;

        HumanoidArm side = this.getAttackArm(entity);
        if (RenderLampHand.isLampArmPosed(model, entity, side))
        {   lampArmPose = (side == HumanoidArm.RIGHT ? this.rightArm : this.leftArm).storePose();
        }
    }

    @Inject(method = "setupAttackAnimation", at = @At("TAIL"))
    public void swingLampArm(LivingEntity entity, float age, CallbackInfo ci)
    {
        if (lampArmPose == null) return;

        HumanoidArm side = this.getAttackArm(entity);
        // Discard vanilla's swing, but keep the arm's position
        (side == HumanoidArm.RIGHT ? this.rightArm : this.leftArm).setRotation(lampArmPose.xRot, lampArmPose.yRot, lampArmPose.zRot);
        RenderLampHand.swingArm(model, side, model.attackTime);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void offsetLampArms(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, CallbackInfo ci)
    {
        if (!ConfigSettings.POSE_SOULSPRING_LAMP.get()) return;

        RenderLampHand.offsetArm(model, entity, HumanoidArm.RIGHT);
        RenderLampHand.offsetArm(model, entity, HumanoidArm.LEFT);
    }

    @Mixin(ItemInHandLayer.class)
    public static class HeldItem
    {
        ItemInHandLayer<?, ?> self = (ItemInHandLayer<?, ?>) (Object) this;

        /**
         * Hides the lamp while the other arm is in a two-handed pose
         */
        @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
        public void hideLampForTwoHandedPose(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, HumanoidArm arm, PoseStack ms,
                                             MultiBufferSource bufferSource, int light, CallbackInfo ci)
        {
            if (stack.is(ModItems.SOULSPRING_LAMP) && self.getParentModel() instanceof HumanoidModel<?> model
            && RenderLampHand.isLampHidden(model, entity, arm))
            {   ci.cancel();
            }
        }

        @Inject(method = "renderArmWithItem",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
        public void shiftSlimArmLamp(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, HumanoidArm arm, PoseStack ms,
                                     MultiBufferSource bufferSource, int light, CallbackInfo ci)
        {
            if (!ConfigSettings.POSE_SOULSPRING_LAMP.get()) return;

            if (stack.is(ModItems.SOULSPRING_LAMP) && ClientOnlyHelper.isPlayerModelSlim(self))
            {   ms.translate((arm == HumanoidArm.RIGHT ? -0.5 : 0.5) / 16f, 0, 0);
            }
        }
    }
}
