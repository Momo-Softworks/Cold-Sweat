package com.momosoftworks.coldsweat.mixin;

import net.minecraft.client.renderer.model.ModelRenderer;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedModel.class)
public class MixinHumanoidModel
{
    BipedModel model = (BipedModel) (Object) this;

    @Shadow
    public ModelRenderer rightArm;

    @Shadow
    public ModelRenderer leftArm;

    @Inject(method = "poseRightArm",
            at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    public void poseRightArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HandSide.RIGHT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.RIGHT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float rightArmRot = CSMath.toRadians(CSMath.blend(armRot.getSecond(), armRot.getFirst(), Minecraft.getInstance().getFrameTime(), 0, 1));

        if (!CSMath.withinRange(rightArmRot, -0.01, 0.01))
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
    }

    @Inject(method = "poseLeftArm",
            at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    public void poseLeftArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HandSide.LEFT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.LEFT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float leftArmRot = CSMath.blend(CSMath.toRadians(armRot.getSecond()), CSMath.toRadians(armRot.getFirst()), Minecraft.getInstance().getFrameTime(), 0, 1);

        if (!CSMath.withinRange(leftArmRot, -0.01, 0.01))
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
    }
}
