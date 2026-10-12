package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedModel.class)
public class MixinSoulLampRendering
{
    BipedModel<?> model = (BipedModel<?>) (Object) this;

    // xRot, yRot, zRot of the lamp arm before vanilla's swing
    float[] lampArmRot;

    @Final
    @Shadow
    public ModelRenderer rightArm;

    @Final
    @Shadow
    public ModelRenderer leftArm;

    @Shadow
    private void poseRightArm(LivingEntity entity) {}

    @Shadow
    private void poseLeftArm(LivingEntity entity) {}

    @Shadow
    private HandSide getAttackArm(LivingEntity entity) { return null; }

    /**
     * Vanilla only poses the arm that is using an item, so the lamp arm would drop while eating/drinking with the other hand
     */
    @Inject(method = "setupAnim(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/model/BipedModel;setupAttackAnimation(Lnet/minecraft/entity/LivingEntity;F)V"))
    public void poseIdleLampArm(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, CallbackInfo ci)
    {
        if (!ConfigSettings.POSE_SOULSPRING_LAMP.get() || !entity.isUsingItem()) return;

        HandSide idleArm = entity.getUsedItemHand() == Hand.MAIN_HAND ? entity.getMainArm().getOpposite() : entity.getMainArm();
        if (!EntityHelper.holdingLamp(entity, idleArm)) return;

        if (idleArm == HandSide.RIGHT)
        {   this.poseRightArm(entity);
        }
        else
        {   this.poseLeftArm(entity);
        }
    }

    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    public void poseRightLampArm(LivingEntity entity, CallbackInfo ci)
    {
        if (shouldPoseLampArm(entity, HandSide.RIGHT, model.rightArmPose))
        {   RenderLampHand.poseArm(model, entity, HandSide.RIGHT);
            ci.cancel();
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    public void poseLeftLampArm(LivingEntity entity, CallbackInfo ci)
    {
        if (shouldPoseLampArm(entity, HandSide.LEFT, model.leftArmPose))
        {   RenderLampHand.poseArm(model, entity, HandSide.LEFT);
            ci.cancel();
        }
    }

    private static boolean shouldPoseLampArm(LivingEntity entity, HandSide side, BipedModel.ArmPose pose)
    {
        return ConfigSettings.POSE_SOULSPRING_LAMP.get() && entity instanceof PlayerEntity
            && pose == BipedModel.ArmPose.ITEM && EntityHelper.holdingLamp(entity, side);
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    public void lowerRightArm(LivingEntity entity, CallbackInfo ci)
    {   RenderLampHand.lowerArm(model, entity, HandSide.RIGHT);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    public void lowerLeftArm(LivingEntity entity, CallbackInfo ci)
    {   RenderLampHand.lowerArm(model, entity, HandSide.LEFT);
    }

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"))
    public void storeLampArmPose(LivingEntity entity, float age, CallbackInfo ci)
    {
        lampArmRot = null;
        if (!ConfigSettings.POSE_SOULSPRING_LAMP.get() || model.attackTime <= 0 || !(entity instanceof PlayerEntity)) return;

        HandSide side = this.getAttackArm(entity);
        if (EntityHelper.holdingLamp(entity, side))
        {   ModelRenderer arm = side == HandSide.RIGHT ? this.rightArm : this.leftArm;
            lampArmRot = new float[] { arm.xRot, arm.yRot, arm.zRot };
        }
    }

    @Inject(method = "setupAttackAnimation", at = @At("TAIL"))
    public void swingLampArm(LivingEntity entity, float age, CallbackInfo ci)
    {
        if (lampArmRot == null) return;

        HandSide side = this.getAttackArm(entity);
        // Discard vanilla's swing, but keep the arm's position
        ModelRenderer arm = side == HandSide.RIGHT ? this.rightArm : this.leftArm;
        arm.xRot = lampArmRot[0];
        arm.yRot = lampArmRot[1];
        arm.zRot = lampArmRot[2];
        RenderLampHand.swingArm(model, side, model.attackTime);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void offsetLampArms(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, CallbackInfo ci)
    {
        if (!ConfigSettings.POSE_SOULSPRING_LAMP.get()) return;

        RenderLampHand.offsetArm(model, entity, HandSide.RIGHT);
        RenderLampHand.offsetArm(model, entity, HandSide.LEFT);
    }

    @Mixin(HeldItemLayer.class)
    public static class HeldItem
    {
        HeldItemLayer<?, ?> self = (HeldItemLayer<?, ?>) (Object) this;

        @Inject(method = "renderArmWithItem",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/model/IHasArm;translateToHand(Lnet/minecraft/util/HandSide;Lcom/mojang/blaze3d/matrix/MatrixStack;)V"))
        public void shiftSlimArmLamp(LivingEntity entity, ItemStack stack, ItemCameraTransforms.TransformType transformType, HandSide arm, MatrixStack ms,
                                     IRenderTypeBuffer bufferSource, int light, CallbackInfo ci)
        {
            if (!ConfigSettings.POSE_SOULSPRING_LAMP.get()) return;

            if (stack.getItem() == ModItems.SOULSPRING_LAMP && ClientOnlyHelper.isPlayerModelSlim(self))
            {   ms.translate((arm == HandSide.RIGHT ? -0.5 : 0.5) / 16f, 0, 0);
            }
        }
    }
}
