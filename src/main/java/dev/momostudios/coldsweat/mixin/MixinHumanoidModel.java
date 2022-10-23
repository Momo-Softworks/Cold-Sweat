package dev.momostudios.coldsweat.mixin;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.client.event.HandleSoulLampAnim;
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

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Inject(method = "poseRightArm",
            at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    public void poseRightArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = PlayerHelper.holdingLamp(entity, HumanoidArm.RIGHT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.RIGHT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float rightArmRot = CSMath.toRadians(CSMath.blend(armRot.getSecond(), armRot.getFirst(), Minecraft.getInstance().getFrameTime(), 0, 1));

        switch (model.rightArmPose)
        {
            case EMPTY ->
            {
                this.rightArm.xRot = this.rightArm.xRot - rightArmRot;
                this.rightArm.yRot = 0.0F;
            }
            case BLOCK ->
            {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F - rightArmRot;
                this.rightArm.yRot = (-(float) Math.PI / 6F);
            }
            case ITEM ->
            {
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
    @Inject(method = "poseLeftArm",
            at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    public void poseLeftArm(LivingEntity entity, CallbackInfo ci)
    {
        boolean holdingLamp = PlayerHelper.holdingLamp(entity, HumanoidArm.LEFT);
        Pair<Float, Float> armRot = HandleSoulLampAnim.LEFT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float leftArmRot = CSMath.blend(CSMath.toRadians(armRot.getSecond()), CSMath.toRadians(armRot.getFirst()), Minecraft.getInstance().getFrameTime(), 0, 1);

        switch (model.leftArmPose)
        {
            case EMPTY ->
            {
                this.leftArm.xRot = this.leftArm.xRot - leftArmRot;
                this.leftArm.yRot = 0.0F;
            }
            case BLOCK ->
            {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.9424779F - leftArmRot;
                this.leftArm.yRot = ((float) Math.PI / 6F);
            }
            case ITEM ->
            {
                this.leftArm.xRot = this.leftArm.xRot * (holdingLamp ? 0.15F : 0.5f) - ((float) Math.PI / 10F) - leftArmRot;
                this.leftArm.zRot = this.leftArm.zRot * (holdingLamp ? 0.15F : 0.5f);
                this.leftArm.yRot = 0.0F;
            }
        }
    }
}
