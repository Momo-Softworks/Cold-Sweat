package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;

@Mixin(BipedModel.class)
public class MixinSoulLampRendering
{
    BipedModel model = (BipedModel) (Object) this;

    @Final
    @Shadow
    public ModelRenderer rightArm;

    @Final
    @Shadow
    public ModelRenderer leftArm;

    @Inject(method = "poseRightArm",
            at = @At("TAIL"))
    public void poseRightArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HandSide.RIGHT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.RIGHT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float rightArmRot = CSMath.toRadians(CSMath.blend(armRot.getSecond(), armRot.getFirst(), Minecraft.getInstance().getFrameTime(), 0, 1));

        if (!CSMath.betweenInclusive(rightArmRot, -0.01, 0.01))
        {
            switch (model.rightArmPose)
            {
                case EMPTY :
                {
                    this.rightArm.xRot = this.rightArm.xRot - rightArmRot;
                    this.rightArm.zRot = this.rightArm.zRot - (holdingLamp ? 0.05F : 0f);
                    this.rightArm.yRot = 0;
                    break;
                }
                case ITEM :
                {
                    this.rightArm.xRot = (holdingLamp ? this.rightArm.xRot * 0.15f - 0.35f : this.rightArm.xRot) - rightArmRot;
                    this.rightArm.zRot = this.rightArm.zRot - (holdingLamp ? 0.05F : 0f);
                    this.rightArm.yRot = 0;
                    break;
                }
            }
        }
        RenderLampHand.transformArm(entity, this.rightArm, HandSide.RIGHT);
    }

    @Inject(method = "poseLeftArm",
            at = @At("TAIL"))
    public void poseLeftArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HandSide.LEFT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.LEFT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float leftArmRot = CSMath.blend(CSMath.toRadians(armRot.getSecond()), CSMath.toRadians(armRot.getFirst()), Minecraft.getInstance().getFrameTime(), 0, 1);

        if (!CSMath.betweenInclusive(leftArmRot, -0.01, 0.01))
        {
            switch (model.leftArmPose)
            {
                case EMPTY :
                {
                    this.leftArm.xRot = this.leftArm.xRot - leftArmRot;
                    this.leftArm.zRot = this.leftArm.zRot + (holdingLamp ? 0.05F : 0f);
                    this.leftArm.yRot = 0.0F;
                    break;
                }
                case ITEM :
                {
                    this.leftArm.xRot = (holdingLamp ? this.leftArm.xRot * 0.15f - 0.35f : this.leftArm.xRot) - leftArmRot;
                    this.leftArm.zRot = this.leftArm.zRot + (holdingLamp ? 0.05F : 0f);
                    this.leftArm.yRot = 0.0F;
                    break;
                }
            }
        }
        RenderLampHand.transformArm(entity, this.leftArm, HandSide.LEFT);
    }

    @Mixin(HeldItemLayer.class)
    public static class HeldItem
    {
        HeldItemLayer self = (HeldItemLayer) (Object) this;

        private static boolean WAS_RIGHT_HAND_ADJUSTED = false;

        @Inject(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HeldItemLayer;renderArmWithItem(Lnet/minecraft/entity/LivingEntity;"
                                                  + "Lnet/minecraft/item/ItemStack;"
                                                  + "Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;"
                                                  + "Lnet/minecraft/util/HandSide;"
                                                  + "Lcom/mojang/blaze3d/matrix/MatrixStack;"
                                                  + "Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", ordinal = 0),
                locals = LocalCapture.CAPTURE_FAILHARD)
        public void shiftRightArmLamp(MatrixStack ms, IRenderTypeBuffer bufferSource, int light, LivingEntity entity, float limbSwing, float limbSwingAmount,
                                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci,
                                      // locals
                                      boolean isMainArm, ItemStack leftHand, ItemStack rightHand)
        {
            if (rightHand.getItem() == ModItems.SOULSPRING_LAMP && isPlayerModelSlim())
            {   ms.translate(-0.5/16f, 0, 0);
                WAS_RIGHT_HAND_ADJUSTED = true;
            }
        }

        @Inject(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HeldItemLayer;renderArmWithItem(Lnet/minecraft/entity/LivingEntity;"
                                                  + "Lnet/minecraft/item/ItemStack;"
                                                  + "Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;"
                                                  + "Lnet/minecraft/util/HandSide;"
                                                  + "Lcom/mojang/blaze3d/matrix/MatrixStack;"
                                                  + "Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", ordinal = 1),
                locals = LocalCapture.CAPTURE_FAILHARD)
        public void shiftLeftArmLamp(MatrixStack ms, IRenderTypeBuffer bufferSource, int light, LivingEntity entity, float limbSwing, float limbSwingAmount,
                                     float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci,
                                     // locals
                                     boolean isMainArm, ItemStack leftHand, ItemStack rightHand)
        {
            // Move the PS back to its original position
            if (WAS_RIGHT_HAND_ADJUSTED)
            {   ms.translate(0.5/16f, 0, 0);
                WAS_RIGHT_HAND_ADJUSTED = false;
            }
            if (leftHand.getItem() == ModItems.SOULSPRING_LAMP && isPlayerModelSlim())
            {   ms.translate(0.5/16f, 0, 0);
            }
        }

        private static final Field SLIM = ObfuscationReflectionHelper.findField(PlayerModel.class, "field_178735_y");
        static { SLIM.setAccessible(true); }

        private boolean isPlayerModelSlim()
        {
            if (self.getParentModel() instanceof PlayerModel<?>)
            {
                PlayerModel<?> playerModel = (PlayerModel<?>) self.getParentModel();
                try
                {
                    return (boolean) SLIM.get(playerModel);
                }
                catch (IllegalAccessException e)
                {   e.printStackTrace();
                }
            }
            return false;
        }
    }

    @Mixin(BipedArmorLayer.class)
    public static class ChestplateArms<T extends LivingEntity, A extends BipedModel<T>>
    {
        @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/model/BipedModel;copyPropertiesTo(Lnet/minecraft/client/renderer/entity/model/BipedModel;)V", shift = At.Shift.AFTER))
        public void renderChestplateArms(MatrixStack poseStack, IRenderTypeBuffer buffer, T entity, EquipmentSlotType slot, int light, A model, CallbackInfo ci)
        {
            if (slot == EquipmentSlotType.CHEST)
            {
                if (EntityHelper.holdingLamp(entity, HandSide.RIGHT))
                {
                    model.rightArm.zRot -= CSMath.toRadians(90);
                    model.rightArm.xRot = -model.rightArm.yRot - CSMath.toRadians(90);
                    model.rightArm.yRot = 0;
                    model.rightArm.x += 1;
                }
                if (EntityHelper.holdingLamp(entity, HandSide.LEFT))
                {
                    model.leftArm.zRot += CSMath.toRadians(90);
                    model.leftArm.xRot = model.leftArm.yRot - CSMath.toRadians(90);
                    model.leftArm.yRot = 0;
                    model.leftArm.x -= 1;
                }
            }
        }
    }
}
