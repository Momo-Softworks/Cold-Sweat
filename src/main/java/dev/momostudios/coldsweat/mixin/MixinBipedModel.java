package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.PlayerHelper;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraftforge.client.model.animation.Animation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BipedModel.class)
public class MixinBipedModel
{
    @Shadow
    public BipedModel.ArmPose rightArmPose;
    @Shadow
    public BipedModel.ArmPose leftArmPose;
    @Shadow
    public ModelRenderer bipedRightArm;
    @Shadow
    public ModelRenderer bipedLeftArm;
    @Shadow
    public ModelRenderer bipedHead;

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Overwrite(remap = ColdSweat.remapMixins)
    private void func_241654_b_(LivingEntity p_241654_1_)
    {
        boolean holdingLamp = PlayerHelper.holdingLamp(p_241654_1_, HandSide.RIGHT);
        float armRot = CSMath.toRadians(p_241654_1_.getPersistentData().getFloat("rightArmRot"));
        float rotOffset = CSMath.toRadians(Animation.getPartialTickTime()) * (float) ((Math.toRadians(holdingLamp ? 70 : 0) - armRot) * 30);
        float rightArmRot = armRot + rotOffset;

        switch(this.rightArmPose)
        {
            case EMPTY:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX - rightArmRot;
                this.bipedRightArm.rotateAngleY = 0.0F;
                break;
            case BLOCK:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - 0.9424779F - rightArmRot;
                this.bipedRightArm.rotateAngleY = (-(float)Math.PI / 6F);
                break;
            case ITEM:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * (holdingLamp ? 0.15F : 0.5f) - ((float) Math.PI / 10F) - rightArmRot;
                this.bipedRightArm.rotateAngleZ = this.bipedRightArm.rotateAngleZ * (holdingLamp ? 0.15F : 0.5f);
                this.bipedRightArm.rotateAngleY = 0.0F;
                break;
            case THROW_SPEAR:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - (float)Math.PI;
                this.bipedRightArm.rotateAngleY = 0.0F;
                break;
            case BOW_AND_ARROW:
                this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY;
                this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY + 0.4F;
                this.bipedRightArm.rotateAngleX = (-(float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
                this.bipedLeftArm.rotateAngleX = (-(float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
                break;
            case CROSSBOW_CHARGE:
                ModelHelper.func_239102_a_(this.bipedRightArm, this.bipedLeftArm, p_241654_1_, true);
                break;
            case CROSSBOW_HOLD:
                ModelHelper.func_239104_a_(this.bipedRightArm, this.bipedLeftArm, this.bipedHead, true);
        }
    }

    /**
     * @author iMikul
     * @reason Adds functions for the Soulfire Lamp
     */
    @Overwrite(remap = ColdSweat.remapMixins)
    private void func_241655_c_(LivingEntity p_241655_1_)
    {
        boolean holdingLamp = PlayerHelper.holdingLamp(p_241655_1_, HandSide.LEFT);
        float armRot = CSMath.toRadians(p_241655_1_.getPersistentData().getFloat("leftArmRot"));
        float rotOffset = CSMath.toRadians(Animation.getPartialTickTime()) * (float) ((Math.toRadians(holdingLamp ? 70 : 0) - armRot) * 20);
        float leftArmRot = armRot + rotOffset;

        switch(this.leftArmPose)
        {
            case EMPTY:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX - leftArmRot;
                this.bipedLeftArm.rotateAngleY = 0.0F;
                break;
            case BLOCK:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5f - 0.9424779F - leftArmRot;
                this.bipedLeftArm.rotateAngleY = ((float)Math.PI / 6F);
                break;
            case ITEM:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * (holdingLamp ? 0.15F : 0.5f) - ((float)Math.PI / 10F) - leftArmRot;
                this.bipedLeftArm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ * (holdingLamp ? 0.15F : 0.5f);
                this.bipedLeftArm.rotateAngleY = 0.0F;
                break;
            case THROW_SPEAR:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - (float)Math.PI;
                this.bipedLeftArm.rotateAngleY = 0.0F;
                break;
            case BOW_AND_ARROW:
                this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY - 0.4F;
                this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY;
                this.bipedRightArm.rotateAngleX = (-(float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
                this.bipedLeftArm.rotateAngleX = (-(float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
                break;
            case CROSSBOW_CHARGE:
                ModelHelper.func_239102_a_(this.bipedRightArm, this.bipedLeftArm, p_241655_1_, false);
                break;
            case CROSSBOW_HOLD:
                ModelHelper.func_239104_a_(this.bipedRightArm, this.bipedLeftArm, this.bipedHead, false);
        }
    }
}
