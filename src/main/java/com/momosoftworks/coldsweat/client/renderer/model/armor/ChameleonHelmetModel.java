package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ChameleonHelmetModel<T extends LivingEntity> extends BipedModel<T>
{
    private final ModelRenderer head_armor;
    private final ModelRenderer right_frill;
    private final ModelRenderer left_frill;

    private final float scale = 0.8f;

    public ChameleonHelmetModel()
    {
        super(0.5f);
        texWidth = 64;
        texHeight = 128;

        head_armor = new ModelRenderer(this);
        head_armor.setPos(0.0F, -1.5F, 3.0F);
        head_armor.texOffs(16, 80).addBox(-4.0F, -6.5F, -7.0F, 8.0F, 8.0F, 8.0F, scale, false);
        head_armor.texOffs(16, 97).addBox(4.8F, -1F, -1.52F, 1.0F, 3.0F, 3.0F, 0.075f, false);
        head_armor.texOffs(16, 97).addBox(-5.8F, -1F, -1.52F, 1.0F, 3.0F, 3.0F, 0.075f, true);

        right_frill = new ModelRenderer(this);
        right_frill.setPos(2.5F, -7.25F, -6.9F);
        head_armor.addChild(right_frill);
        setRotationAngle(right_frill, 0.0F, -0.3927F, 0.0F);
        right_frill.texOffs(24, 54).addBox(0.0F, -5.0F, 1.1F, 0.0F, 9.0F, 12.0F, scale, scale, scale*2f);

        left_frill = new ModelRenderer(this);
        left_frill.setPos(-1F, -7.25F, -7.5F);
        head_armor.addChild(left_frill);
        setRotationAngle(left_frill, 0.0F, 0.3927F, 0.0F);
        left_frill.texOffs(24, 54).addBox(0.0F, -5.0F, 1.1F, 0.0F, 9.0F, 12.0F, scale, scale, scale*2f);

        this.head.addChild(head_armor);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {   super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        renderPieceFromModel(head_armor, head, matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
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