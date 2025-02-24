package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.jozufozu.flywheel.core.model.ModelPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

import java.util.Arrays;

public class ChameleonChestplateModel<T extends LivingEntity> extends BipedModel<T>
{
    private final ModelRenderer chest_armor;
    private final ModelRenderer right_arm_armor;
    private final ModelRenderer left_arm_armor;

    // Scale factor preserved from newer model
    private final float scale = 0.8f;

    public ChameleonChestplateModel()
    {
        super(0.5f);
        texWidth = 64;
        texHeight = 128;

        chest_armor = new ModelRenderer(this);
        chest_armor.texOffs(0, 64).addBox(-4.0F, -24F, -2.0F, 8.0F, 12.0F, 4.0F, scale, false);
        chest_armor.setPos(0.0F, 24.0F, 0.0F);

        right_arm_armor = new ModelRenderer(this);
        right_arm_armor.texOffs(0, 80).addBox(-3.25F, -0.5F, -2.0F, 4.0F, 12.0F, 4.0F, scale, false);
        right_arm_armor.setPos(0.0F, -1.5F, 0.0F);

        left_arm_armor = new ModelRenderer(this);
        left_arm_armor.texOffs(0, 80).addBox(-0.75F, -0.5F, -2.0F, 4.0F, 12.0F, 4.0F, scale, true);
        left_arm_armor.setPos(0.0F, -1.5F, 0.0F);

        this.body.addChild(chest_armor);
        this.rightArm.addChild(right_arm_armor);
        this.leftArm.addChild(left_arm_armor);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        renderPieceFromModel(chest_armor, body, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        renderPieceFromModel(right_arm_armor, rightArm, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        renderPieceFromModel(left_arm_armor, leftArm, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void renderPieceFromModel(ModelRenderer armor, ModelRenderer parent, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        matrixStack.pushPose();
        matrixStack.translate(parent.x / 16, parent.y / 16, parent.z / 16);
        matrixStack.mulPose(CSMath.toQuaternion(parent.xRot, parent.yRot, parent.zRot));
        armor.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}