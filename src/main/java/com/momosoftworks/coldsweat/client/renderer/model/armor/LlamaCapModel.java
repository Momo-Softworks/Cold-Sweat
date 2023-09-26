package com.momosoftworks.coldsweat.client.renderer.model.armor;// Made with Blockbench 4.8.1
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class LlamaCapModel<T extends LivingEntity> extends BipedModel<T>
{
	private final ModelRenderer helmet;

	public LlamaCapModel()
	{
		super(1f);
		texWidth = 64;
		texHeight = 128;

		helmet = new ModelRenderer(this);
		helmet.setPos(0.0F, -1.5F, 3.0F);
		helmet.texOffs(16, 80).addBox(-4.0F, -6.5F, -7.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		matrixStack.pushPose();
		matrixStack.translate(head.x / 16, head.y / 16, head.z / 16);
		matrixStack.mulPose(CSMath.getQuaternion(head.xRot, head.yRot, head.zRot));
		matrixStack.scale(1.25f, 1.25f, 1.25f);
		matrixStack.translate(0, 0.047f, 0);
		helmet.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	public LlamaCapModel<T> withModelBase(BipedModel<?> modelBase)
	{
		this.body = modelBase.body;
		this.rightArm = modelBase.rightArm;
		this.leftArm = modelBase.leftArm;
		this.rightLeg = modelBase.rightLeg;
		this.leftLeg = modelBase.leftLeg;
		this.hat = modelBase.hat;
		return this;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}