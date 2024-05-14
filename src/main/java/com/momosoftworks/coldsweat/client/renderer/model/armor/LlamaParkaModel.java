package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class LlamaParkaModel<T extends LivingEntity> extends BipedModel<T>
{
	public final ModelRenderer chestplate;
	public final ModelRenderer fluff;
	public final ModelRenderer rightSleeve;
	public final ModelRenderer leftSleeve;

	public LlamaParkaModel()
	{
		super(1f);
		texWidth = 64;
		texHeight = 128;

		chestplate = new ModelRenderer(this);
		chestplate.setPos(0.0F, 24.0F, 0.0F);
		chestplate.texOffs(0, 64).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

		fluff = new ModelRenderer(this);
		fluff.setPos(0.0F, 0.0F, 0.0F);
		fluff.texOffs(24, 64).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 6.0F, 10.0F, 0.0F, false);

		rightSleeve = new ModelRenderer(this);
		rightSleeve.setPos(1.0F, -2.0F, 0.0F);
		rightSleeve.texOffs(0, 80).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		leftSleeve = new ModelRenderer(this);
		leftSleeve.setPos(-1.0F, -2.0F, 0.0F);
		leftSleeve.texOffs(0, 80).addBox(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		// chestplate
		matrixStack.pushPose();
		matrixStack.translate(body.x / 16f, body.y / 16f, body.z / 16f);
		matrixStack.mulPose(CSMath.toQuaternion(body.xRot, body.yRot, body.zRot));
		matrixStack.scale(1.25f, 1.1f, 1.25f);
		chestplate.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.scale(0.95f, 1f, 0.9f);
		matrixStack.translate(0, 0.02, 0);
		fluff.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// right sleeve
		matrixStack.pushPose();
		matrixStack.translate(rightArm.x / 16f, rightArm.y / 16f, rightArm.z / 16f);
		matrixStack.mulPose(CSMath.toQuaternion(rightArm.xRot, rightArm.yRot, rightArm.zRot));
		matrixStack.scale(1.25f, 1.1f, 1.25f);
		rightSleeve.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// left sleeve
		matrixStack.pushPose();
		matrixStack.translate(leftArm.x / 16f, leftArm.y / 16f, leftArm.z / 16f);
		matrixStack.mulPose(CSMath.toQuaternion(leftArm.xRot, leftArm.yRot, leftArm.zRot));
		matrixStack.scale(1.25f, 1.1f, 1.25f);
		leftSleeve.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	public LlamaParkaModel<T> withModelBase(BipedModel<?> modelBase)
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