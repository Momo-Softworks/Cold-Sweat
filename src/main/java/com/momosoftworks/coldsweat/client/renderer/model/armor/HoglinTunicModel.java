package com.momosoftworks.coldsweat.client.renderer.model.armor;// Made with Blockbench 4.8.1
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class HoglinTunicModel<T extends LivingEntity> extends BipedModel<T>
{
	private final ModelRenderer bodyBase;
	private final ModelRenderer rightArmBase;
	private final ModelRenderer rightSleeve;
	private final ModelRenderer leftArmBase;
	private final ModelRenderer leftSleeve;

	public HoglinTunicModel()
	{
		super(1f);
	 	texWidth = 64;
		texHeight = 128;

		bodyBase = new ModelRenderer(this);
		bodyBase.setPos(0.0F, -0.3F, 0.0F);
		bodyBase.texOffs(0, 112).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);


		rightArmBase = new ModelRenderer(this);
		rightArmBase.setPos(5f, 22.0F, 0.0F);

		rightSleeve = new ModelRenderer(this);
		rightSleeve.setPos(0.0f, -0.3F, 0.0F);
		rightArmBase.addChild(rightSleeve);
		rightSleeve.texOffs(24, 112).addBox(-8.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		rightSleeve.texOffs(2, 90).addBox(-11.0F, -27.0F, 0.0F, 7.0F, 6.0F, 0.0F, 0.0F, false);


		leftArmBase = new ModelRenderer(this);
		leftArmBase.setPos(-10F, 22.0F, 0.0F);

		leftSleeve = new ModelRenderer(this);
		leftSleeve.setPos(0.0F, -0.3F, 0.0F);
		leftArmBase.addChild(leftSleeve);
		leftSleeve.texOffs(24, 112).addBox(9.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
		leftSleeve.texOffs(2, 90).addBox(9.0F, -27.0F, 0.0F, 7.0F, 6.0F, 0.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		// body
		matrixStack.pushPose();
		matrixStack.translate(body.x / 16.0, body.y / 16.0, body.z / 16.0);
		matrixStack.mulPose(CSMath.getQuaternion(body.xRot, body.yRot, body.zRot));
		matrixStack.scale(1.25f, 1.1f, 1.25f);
		bodyBase.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// right arm
		matrixStack.pushPose();
		matrixStack.translate(rightArm.x / 16.0, rightArm.y / 16.0, rightArm.z / 16.0);
		matrixStack.mulPose(CSMath.getQuaternion(rightArm.xRot, rightArm.yRot, rightArm.zRot));
		matrixStack.scale(1.35f, 1.15f, 1.35f);
		rightArmBase.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// left arm
		matrixStack.pushPose();
		matrixStack.translate(leftArm.x / 16.0, leftArm.y / 16.0, leftArm.z / 16.0);
		matrixStack.mulPose(CSMath.getQuaternion(leftArm.xRot, leftArm.yRot, leftArm.zRot));
		matrixStack.scale(1.35f, 1.15f, 1.35f);
		leftArmBase.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}

	public HoglinTunicModel<T> withModelBase(BipedModel<?> modelBase)
	{
		this.body = modelBase.body;
		this.rightArm = modelBase.rightArm;
		this.leftArm = modelBase.leftArm;
		this.rightLeg = modelBase.rightLeg;
		this.leftLeg = modelBase.leftLeg;
		this.hat = modelBase.hat;
		return this;
	}
}