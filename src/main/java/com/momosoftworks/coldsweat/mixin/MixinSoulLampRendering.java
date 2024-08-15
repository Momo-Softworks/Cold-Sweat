package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.EquipmentSlot;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HumanoidModel.class)
public class MixinSoulLampRendering
{
    HumanoidModel model = (HumanoidModel) (Object) this;

    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    @Inject(method = "poseRightArm",
            at = @At("TAIL"))
    public void poseRightArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HumanoidArm.RIGHT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.RIGHT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float rightArmRot = CSMath.toRadians(CSMath.blend(armRot.getSecond(), armRot.getFirst(), Minecraft.getInstance().getFrameTime(), 0, 1));

        if (!CSMath.betweenInclusive(rightArmRot, -0.01, 0.01))
        {
            switch (model.rightArmPose)
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
        RenderLampHand.transformArm(entity, this.rightArm, HumanoidArm.RIGHT);
    }

    @Inject(method = "poseLeftArm",
            at = @At("TAIL"))
    public void poseLeftArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HumanoidArm.LEFT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.LEFT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float leftArmRot = CSMath.blend(CSMath.toRadians(armRot.getSecond()), CSMath.toRadians(armRot.getFirst()), Minecraft.getInstance().getFrameTime(), 0, 1);

        if (!CSMath.betweenInclusive(leftArmRot, -0.01, 0.01))
        {
            switch (model.leftArmPose)
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
        RenderLampHand.transformArm(entity, this.leftArm, HumanoidArm.LEFT);
    }

    @Mixin(ItemInHandLayer.class)
    public static class HeldItem
    {
        ItemInHandLayer self = (ItemInHandLayer) (Object) this;

        private static boolean WAS_RIGHT_HAND_ADJUSTED = false;

        @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/ItemInHandLayer;renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 0),
                locals = LocalCapture.CAPTURE_FAILHARD)
        public void shiftRightArmLamp(PoseStack ms, MultiBufferSource bufferSource, int light, LivingEntity entity, float limbSwing, float limbSwingAmount,
                                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci,
                                      // locals
                                      boolean isMainArm, ItemStack leftHand, ItemStack rightHand)
        {
            if (rightHand.is(ModItems.SOULSPRING_LAMP) && ClientOnlyHelper.isPlayerModelSlim(self))
            {   ms.translate(-0.5/16f, 0, 0);
                WAS_RIGHT_HAND_ADJUSTED = true;
            }
        }

        @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/ItemInHandLayer;renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 1),
                locals = LocalCapture.CAPTURE_FAILHARD)
        public void shiftLeftArmLamp(PoseStack ms, MultiBufferSource bufferSource, int light, LivingEntity entity, float limbSwing, float limbSwingAmount,
                                     float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci,
                                     // locals
                                     boolean isMainArm, ItemStack leftHand, ItemStack rightHand)
        {
            // Move the PS back to its original position
            if (WAS_RIGHT_HAND_ADJUSTED)
            {   ms.translate(0.5/16f, 0, 0);
                WAS_RIGHT_HAND_ADJUSTED = false;
            }
            if (leftHand.is(ModItems.SOULSPRING_LAMP) && ClientOnlyHelper.isPlayerModelSlim(self))
            {   ms.translate(0.5/16f, 0, 0);
            }
        }
    }

    @Mixin(HumanoidArmorLayer.class)
    public static class ChestplateArms<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
    {
        HumanoidArmorLayer<T, M, A> self = (HumanoidArmorLayer<T, M, A>) (Object) this;
        @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;copyPropertiesTo(Lnet/minecraft/client/model/HumanoidModel;)V", shift = At.Shift.AFTER))
        public void renderChestplateArms(PoseStack poseStack, MultiBufferSource buffer, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci)
        {
            if (slot == EquipmentSlot.CHEST)
            {
                if (EntityHelper.holdingLamp(entity, HumanoidArm.RIGHT))
                {
                    model.rightArm.zRot -= CSMath.toRadians(90);
                    model.rightArm.xRot = -model.rightArm.yRot - CSMath.toRadians(90);
                    model.rightArm.yRot = 0;
                    model.rightArm.x += 1;
                    if (!ClientOnlyHelper.isPlayerModelSlim(self))
                    {   model.rightArm.y -= 1;
                    }
                }
                if (EntityHelper.holdingLamp(entity, HumanoidArm.LEFT))
                {
                    model.leftArm.zRot += CSMath.toRadians(90);
                    model.leftArm.xRot = model.leftArm.yRot - CSMath.toRadians(90);
                    model.leftArm.yRot = 0;
                    model.leftArm.x -= 1;
                    if (!ClientOnlyHelper.isPlayerModelSlim(self))
                    {   model.leftArm.y -= 1;
                    }
                }
            }
        }
    }

    @Mixin(HumanoidModel.class)
    public static class ShiftWidePlayerArm
    {
        HumanoidModel self = (HumanoidModel) (Object) this;

        @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
        public void shiftWidePlayerArm(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, CallbackInfo ci)
        {
            if (self instanceof PlayerModel playerModel && !ClientOnlyHelper.isPlayerModelSlim(self))
            {
                if (EntityHelper.holdingLamp(entity, HumanoidArm.RIGHT))
                {
                    playerModel.rightArm.y += 1;
                    if (entity instanceof Player player && player.getAttackAnim(Minecraft.getInstance().getFrameTime()) > 0
                    && EntityHelper.getArmFromHand(player.swingingArm, player) == HumanoidArm.RIGHT)
                    {   playerModel.rightArm.x -= 1;
                    }
                }
                if (EntityHelper.holdingLamp(entity, HumanoidArm.LEFT))
                {
                    playerModel.leftArm.y += 1;
                    if (entity instanceof Player player && player.getAttackAnim(Minecraft.getInstance().getFrameTime()) > 0
                    && EntityHelper.getArmFromHand(player.swingingArm, player) == HumanoidArm.LEFT)
                    {   playerModel.leftArm.x += 1;
                    }
                }
            }
        }
    }
}
