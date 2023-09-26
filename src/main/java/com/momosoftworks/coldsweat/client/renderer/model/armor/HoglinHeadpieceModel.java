package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class HoglinHeadpieceModel<T extends LivingEntity> extends BipedModel<T>
{
	private final ModelRenderer headBase;
	private final ModelRenderer leftEar;
	private final ModelRenderer rightEar;
	private final ModelRenderer headpiece;

	public HoglinHeadpieceModel()
	{
		super(1f);
		texWidth = 64;
		texHeight = 128;

		headBase = new ModelRenderer(this);
		headBase.setPos(0.0F, -1.5F, 3.0F);
		setRotationAngle(headBase, 0.3927F, 0.0F, 0.0F);
		headBase.texOffs(0, 64).addBox(-7.0F, -14.075F, -5.675F, 2.0F, 8.0F, 2.0F, 0.0F, false);
		headBase.texOffs(0, 64).addBox(5.0F, -14.075F, -5.675F, 2.0F, 8.0F, 2.0F, 0.0F, true);
		headBase.texOffs(0, 78).addBox(0.0F, -16.075F, -1.65F, 0.0F, 17.0F, 12.0F, 0.0F, false);

		leftEar = new ModelRenderer(this);
		leftEar.setPos(5.0F, -10.575F, 2.85F);
		headBase.addChild(leftEar);
		setRotationAngle(leftEar, 0.0F, 0.0F, 0.6981F);
		leftEar.texOffs(0, 107).addBox(0.0F, -0.5F, -1.5F, 5.0F, 1.0F, 3.0F, 0.0F, false);

		rightEar = new ModelRenderer(this);
		rightEar.setPos(-5.0F, -10.575F, 2.85F);
		headBase.addChild(rightEar);
		setRotationAngle(rightEar, 0.0F, 0.0F, -0.6981F);
		rightEar.texOffs(0, 107).addBox(-5.0F, -0.5F, -1.5F, 5.0F, 1.0F, 3.0F, 0.0F, true);

		headpiece = new ModelRenderer(this);
		headpiece.setPos(0.0F, -9.5717F, -2.1651F);
		headBase.addChild(headpiece);
		setRotationAngle(headpiece, -1.5708F, 0.0F, 0.0F);
		headpiece.texOffs(0, 64).addBox(-5.0F, -6.5F, -1.5F, 10.0F, 13.0F, 13.0F, 0.0F, false);
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
		headBase.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{	modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}

	public HoglinHeadpieceModel<T> withModelBase(BipedModel<?> modelBase)
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