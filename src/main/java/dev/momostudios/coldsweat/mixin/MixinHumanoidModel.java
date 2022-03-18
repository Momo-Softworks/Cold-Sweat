package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
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

    @Final
    @Shadow
    public ModelPart head;

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Inject(method = "poseRightArm", at = @At("TAIL"), remap = ColdSweat.remapMixins)
    private void poseRightArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = PlayerHelper.holdingLamp(entity, HumanoidArm.RIGHT);
        float armRot = CSMath.toRadians(entity.getPersistentData().getFloat("rightArmRot"));
        float rotOffset = CSMath.toRadians(Minecraft.getInstance().getFrameTime()) * (float) ((Math.toRadians(holdingLamp ? 70 : 0) - armRot) * 30);
        float rightArmRot = armRot + rotOffset;

        switch (model.rightArmPose)
        {
            case EMPTY -> {
                this.rightArm.xRot = this.rightArm.xRot - rightArmRot;
                this.rightArm.yRot = 0.0F;
            }
            case BLOCK -> {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F - rightArmRot;
                this.rightArm.yRot = (-(float) Math.PI / 6F);
            }
            case ITEM -> {
                this.rightArm.xRot = this.rightArm.xRot * (holdingLamp ? 0.15F : 0.5f) - ((float) Math.PI / 10F) - rightArmRot;
                this.rightArm.zRot = this.rightArm.zRot * (holdingLamp ? 0.15F : 0.5f);
                this.rightArm.yRot = 0.0F;
            }
        }
    }

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Inject(method = "poseLeftArm", at = @At("TAIL"), remap = ColdSweat.remapMixins)
    private void poseLeftArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = PlayerHelper.holdingLamp(entity, HumanoidArm.LEFT);
        float armRot = CSMath.toRadians(entity.getPersistentData().getFloat("leftArmRot"));
        float rotOffset = CSMath.toRadians(Minecraft.getInstance().getFrameTime()) * (float) ((Math.toRadians(holdingLamp ? 70 : 0) - armRot) * 20);
        float leftArmRot = armRot + rotOffset;

        switch (model.leftArmPose)
        {
            case EMPTY -> {
                this.leftArm.xRot = this.leftArm.xRot - leftArmRot;
                this.leftArm.yRot = 0.0F;
            }
            case BLOCK -> {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.9424779F - leftArmRot;
                this.leftArm.yRot = ((float) Math.PI / 6F);
            }
            case ITEM -> {
                this.leftArm.xRot = this.leftArm.xRot * (holdingLamp ? 0.15F : 0.5f) - ((float) Math.PI / 10F) - leftArmRot;
                this.leftArm.zRot = this.leftArm.zRot * (holdingLamp ? 0.15F : 0.5f);
                this.leftArm.yRot = 0.0F;
            }
        }
    }
}
