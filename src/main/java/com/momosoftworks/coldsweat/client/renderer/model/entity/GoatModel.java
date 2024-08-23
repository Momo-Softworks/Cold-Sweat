package com.momosoftworks.coldsweat.client.renderer.model.entity;

import com.google.common.collect.ImmutableList;
import com.momosoftworks.coldsweat.common.entity.GoatEntity;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoatModel<T extends GoatEntity> extends AgeableModel<T>
{
    private final ModelRenderer left_back_leg;
    private final ModelRenderer right_back_leg;
    private final ModelRenderer right_front_leg;
    private final ModelRenderer left_front_leg;
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer nose;
    private final ModelRenderer right_horn;
    private final ModelRenderer left_horn;

    public GoatModel() {
        super(true, 19.0F, 1.0F, 2.5F, 2.0F, 24.0F);
        this.texWidth = 64;
        this.texHeight = 64;
        this.left_back_leg = new ModelRenderer(this);
        this.left_back_leg.setPos(1.0F, 14.0F, 4.0F);
        this.left_back_leg.texOffs(36, 29).addBox(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F, 0.0F, false);
        this.right_back_leg = new ModelRenderer(this);
        this.right_back_leg.setPos(-3.0F, 14.0F, 4.0F);
        this.right_back_leg.texOffs(49, 29).addBox(0.0F, 4.0F, 0.0F, 3.0F, 6.0F, 3.0F, 0.0F, false);
        this.right_front_leg = new ModelRenderer(this);
        this.right_front_leg.setPos(-3.0F, 14.0F, -6.0F);
        this.right_front_leg.texOffs(49, 2).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, 0.0F, false);
        this.left_front_leg = new ModelRenderer(this);
        this.left_front_leg.setPos(1.0F, 14.0F, -6.0F);
        this.left_front_leg.texOffs(35, 2).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, 0.0F, false);
        this.body = new ModelRenderer(this);
        this.body.setPos(0.0F, 24.0F, 0.0F);
        this.body.texOffs(1, 1).addBox(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F, 0.0F, false);
        this.body.texOffs(0, 28).addBox(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F, 0.0F, false);
        this.head = new ModelRenderer(this);
        this.head.setPos(0.5F, 7.0F, -8.0F);
        this.head.texOffs(2, 61).addBox(2.5F, -4.0F, -2.0F, 3.0F, 2.0F, 1.0F, 0.0F, true);
        this.head.texOffs(2, 61).addBox(-5.5F, -4.0F, -2.0F, 3.0F, 2.0F, 1.0F, 0.0F, false);
        this.head.texOffs(23, 52).addBox(0.0F, 4.0F, -6.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        this.nose = new ModelRenderer(this);
        this.nose.setPos(0.5F, -1.0F, 0.0F);
        this.head.addChild(this.nose);
        this.setRotationAngle(this.nose, 0.9599F, 0.0F, 0.0F);
        this.nose.texOffs(34, 46).addBox(-3.0F, -4.0F, -8.0F, 5.0F, 7.0F, 10.0F, 0.0F, false);
        this.right_horn = new ModelRenderer(this);
        this.right_horn.setPos(0.5F, -1.0F, 0.0F);
        this.head.addChild(this.right_horn);
        this.right_horn.texOffs(12, 55).addBox(-2.99F, -8.0F, -2.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);
        this.left_horn = new ModelRenderer(this);
        this.left_horn.setPos(0.5F, -1.0F, 0.0F);
        this.head.addChild(this.left_horn);
        this.left_horn.texOffs(12, 55).addBox(-0.01F, -8.0F, -2.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(T goat, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch)
    {
        this.left_horn.visible = !goat.isBaby();
        this.right_horn.visible = !goat.isBaby();
        this.head.xRot = headPitch * 0.017453292F;
        this.head.yRot = netHeadYaw * 0.017453292F;
        this.right_back_leg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.left_back_leg.xRot = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        this.right_front_leg.xRot = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        this.left_front_leg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float rotation = goat.getHeadPitch();
        if (rotation != 0.0F) {
            this.head.x = rotation;
        }
    }

    protected Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(this.head);
    }

    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(this.body, this.right_back_leg, this.left_back_leg, this.right_front_leg, this.left_front_leg);
    }

    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.left_horn.visible = !entityIn.isBaby();
        this.right_horn.visible = !entityIn.isBaby();
        this.head.xRot = headPitch * 0.017453292F;
        this.head.yRot = netHeadYaw * 0.017453292F;
        this.right_back_leg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.left_back_leg.xRot = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        this.right_front_leg.xRot = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        this.left_front_leg.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float rotation = entityIn.getHeadPitch();
        if (rotation != 0.0F) {
            this.head.x = rotation;
        }

    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
