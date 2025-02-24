package com.momosoftworks.coldsweat.client.renderer.model.armor;

// Made with Blockbench 4.12.3
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ChameleonBootsModel<T extends LivingEntity> extends BipedModel<T>
{
    private final ModelRenderer right_boot;
    private final ModelRenderer left_boot;

    private final float scale = 0.8f;

    public ChameleonBootsModel()
    {
        super(0.5f);
        texWidth = 64;
        texHeight = 128;

        right_boot = new ModelRenderer(this);
        right_boot.texOffs(48, 80).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale, false);
        right_boot.setPos(0.0F, -0.5F, 0.0F);

        left_boot = new ModelRenderer(this);
        left_boot.texOffs(48, 80).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale, true);
        left_boot.setPos(0.0F, -0.5F, 0.0F);

        this.rightLeg.addChild(right_boot);
        this.leftLeg.addChild(left_boot);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {   super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        renderPieceFromModel(right_boot, rightLeg, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        renderPieceFromModel(left_boot, leftLeg, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
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