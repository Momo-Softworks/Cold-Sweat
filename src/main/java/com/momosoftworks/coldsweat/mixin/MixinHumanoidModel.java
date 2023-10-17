package com.momosoftworks.coldsweat.mixin;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class MixinHumanoidModel
{
    HumanoidModel model = (HumanoidModel) (Object) this;

    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Inject(method = "poseRightArm",
            at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    public void poseRightArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HumanoidArm.RIGHT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.RIGHT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float rightArmRot = CSMath.toRadians(CSMath.blend(armRot.getSecond(), armRot.getFirst(), Minecraft.getInstance().getFrameTime(), 0, 1));

        if (!CSMath.isWithin(rightArmRot, -0.01, 0.01))
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
    }

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Inject(method = "poseLeftArm",
            at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    public void poseLeftArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = EntityHelper.holdingLamp(entity, HumanoidArm.LEFT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.LEFT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float leftArmRot = CSMath.blend(CSMath.toRadians(armRot.getSecond()), CSMath.toRadians(armRot.getFirst()), Minecraft.getInstance().getFrameTime(), 0, 1);

        if (!CSMath.isWithin(leftArmRot, -0.01, 0.01))
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
    }
}
