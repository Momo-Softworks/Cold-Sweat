package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.client.renderer.SoulLampPose;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The lamp arm's pose is applied by {@link RenderLampHand#LAMP_ARM_POSE}. The pose data is attached to the render state by {@link SoulLampPose}
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
    private void poseRightArm(HumanoidRenderState state) {}

    @Shadow
    private void poseLeftArm(HumanoidRenderState state) {}

    /**
     * Vanilla skips posing an arm if the other arm's pose affects it (i.e. drawing a bow), so the lamp arm would drop
     */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"))
    public void poseIdleLampArm(HumanoidRenderState state, CallbackInfo ci)
    {
        HumanoidModel.ArmPose lampPose = RenderLampHand.LAMP_ARM_POSE.getValue();
        if (state.rightArmPose == lampPose && state.leftArmPose.affectsOffhandPose())
        {   this.poseRightArm(state);
        }
        if (state.leftArmPose == lampPose && state.rightArmPose.affectsOffhandPose())
        {   this.poseLeftArm(state);
        }
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    public void lowerRightArm(HumanoidRenderState state, CallbackInfo ci)
    {   RenderLampHand.lowerArm(model, state, HumanoidArm.RIGHT);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    public void lowerLeftArm(HumanoidRenderState state, CallbackInfo ci)
    {   RenderLampHand.lowerArm(model, state, HumanoidArm.LEFT);
    }

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"))
    public void storeLampArmPose(HumanoidRenderState state, CallbackInfo ci)
    {
        lampArmPose = null;
        SoulLampPose pose = SoulLampPose.of(state);
        if (pose == null || state.attackTime <= 0 || !pose.isPlayer()) return;

        if (pose.holdingLamp(state.attackArm))
        {   lampArmPose = model.getArm(state.attackArm).storePose();
        }
    }

    @Inject(method = "setupAttackAnimation", at = @At("TAIL"))
    public void swingLampArm(HumanoidRenderState state, CallbackInfo ci)
    {
        if (lampArmPose == null) return;

        // Discard vanilla's swing, but keep the arm's position
        model.getArm(state.attackArm).setRotation(lampArmPose.xRot(), lampArmPose.yRot(), lampArmPose.zRot());
        RenderLampHand.swingArm(model, state.attackArm, state.attackTime);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    public void offsetLampArms(HumanoidRenderState state, CallbackInfo ci)
    {
        SoulLampPose pose = SoulLampPose.of(state);
        if (pose == null) return;

        RenderLampHand.offsetArm(model, pose, HumanoidArm.RIGHT);
        RenderLampHand.offsetArm(model, pose, HumanoidArm.LEFT);
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
}
