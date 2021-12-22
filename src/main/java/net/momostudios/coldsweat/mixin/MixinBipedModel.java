package net.momostudios.coldsweat.mixin;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerHelper;
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
     */
    @Overwrite
    private void func_241654_b_(LivingEntity p_241654_1_)
    {
        float rightArmRot = p_241654_1_.getPersistentData().getFloat("rightArmRot");

        if (PlayerHelper.holdingLamp(p_241654_1_, HandSide.RIGHT) && (this.rightArmPose == BipedModel.ArmPose.BLOCK || this.rightArmPose == BipedModel.ArmPose.ITEM))
        {
            if (rightArmRot < 70)
                p_241654_1_.getPersistentData().putFloat("rightArmRot", rightArmRot + (71 - rightArmRot) / 7);
        }
        else
        {
            if (rightArmRot > 0)
                p_241654_1_.getPersistentData().putFloat("rightArmRot", rightArmRot + (0 - rightArmRot) / 7);
        }

        switch(this.rightArmPose)
        {
            case EMPTY:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX - MathHelperCS.toRadians(rightArmRot);
                this.bipedRightArm.rotateAngleY = 0.0F;
                break;
            case BLOCK:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - 0.9424779F;
                this.bipedRightArm.rotateAngleY = (-(float)Math.PI / 6F);
                break;
            case ITEM:
            {
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.15F - ((float) Math.PI / 10F) - MathHelperCS.toRadians(rightArmRot);
                this.bipedRightArm.rotateAngleZ = this.bipedRightArm.rotateAngleZ * 0.15F;
                this.bipedRightArm.rotateAngleY = 0.0F;
                break;
            }
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
     */
    @Overwrite
    private void func_241655_c_(LivingEntity p_241655_1_)
    {
        float leftArmRot = p_241655_1_.getPersistentData().getFloat("leftArmRot");

        if (PlayerHelper.holdingLamp(p_241655_1_, HandSide.LEFT) && (this.leftArmPose == BipedModel.ArmPose.BLOCK || this.leftArmPose == BipedModel.ArmPose.ITEM))
        {
            if (leftArmRot < 70)
                p_241655_1_.getPersistentData().putFloat("leftArmRot", leftArmRot + (71 - leftArmRot) / 7);
        }
        else
        {
            if (leftArmRot > 0)
                p_241655_1_.getPersistentData().putFloat("leftArmRot", leftArmRot + (0 - leftArmRot) / 7);
        }

        switch(this.leftArmPose)
        {
            case EMPTY:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX - MathHelperCS.toRadians(leftArmRot);
                this.bipedLeftArm.rotateAngleY = 0.0F;
                break;
            case BLOCK:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - 0.9424779F;
                this.bipedLeftArm.rotateAngleY = ((float)Math.PI / 6F);
                break;
            case ITEM:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.15F - ((float)Math.PI / 10F) - MathHelperCS.toRadians(leftArmRot);
                this.bipedLeftArm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ * 0.15F;
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
