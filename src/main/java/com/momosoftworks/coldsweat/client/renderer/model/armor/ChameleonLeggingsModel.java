package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ChameleonLeggingsModel<T extends LivingEntity> extends BipedModel<T>
{
    private final ModelRenderer waist_armor;
    private final ModelRenderer right_legging;
    private final ModelRenderer left_legging;

    private final float scale = 0.6f;

    public ChameleonLeggingsModel()
    {
        super(0.5f);
        texWidth = 64;
        texHeight = 128;

        waist_armor = new ModelRenderer(this);
        waist_armor.texOffs(16, 112).addBox(-4.0F, -23.5F, -2.0F, 8.0F, 12.0F, 4.0F, scale*1.2f, false);
        waist_armor.setPos(0.0F, 23.0F, 0.0F);

        right_legging = new ModelRenderer(this);
        right_legging.texOffs(0, 112).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale, false);
        right_legging.setPos(0.0F, 0.0F, 0.0F);

        left_legging = new ModelRenderer(this);
        left_legging.texOffs(0, 112).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale, true);
        left_legging.setPos(0.0F, 0.0F, 0.0F);

        this.body.addChild(waist_armor);
        this.rightLeg.addChild(right_legging);
        this.leftLeg.addChild(left_legging);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        renderPieceFromModel(waist_armor, body, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        renderPieceFromModel(right_legging, rightLeg, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        renderPieceFromModel(left_legging, leftLeg, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
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