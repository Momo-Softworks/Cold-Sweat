package dev.momostudios.coldsweat.client.renderer.model.armor;// Made with Blockbench 4.8.1
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class HoglinHoovesModel<T extends LivingEntity> extends BipedModel<T>
{
	private final ModelRenderer rightBoot;
	private final ModelRenderer leftBoot;

	public HoglinHoovesModel()
	{
		super(1f);
		texWidth = 64;
		texHeight = 128;

		rightBoot = new ModelRenderer(this);
		rightBoot.setPos(-0.15F, 0.0F, 0.0F);
		rightBoot.texOffs(40, 96).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		leftBoot = new ModelRenderer(this);
		leftBoot.setPos(0.15F, 0.0F, 0.0F);
		leftBoot.texOffs(40, 96).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		matrixStack.pushPose();
		matrixStack.translate(rightLeg.x / 16, rightLeg.y / 16, rightLeg.z / 16);
		matrixStack.mulPose(CSMath.getQuaternion(rightLeg.xRot, rightLeg.yRot, rightLeg.zRot));
		matrixStack.scale(1.3f, 1.25f, 1.3f);
		matrixStack.translate(0, -0.125, 0);
		rightBoot.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// left boot
		matrixStack.pushPose();
		matrixStack.translate(leftLeg.x / 16, leftLeg.y / 16, leftLeg.z / 16);
		matrixStack.mulPose(CSMath.getQuaternion(leftLeg.xRot, leftLeg.yRot, leftLeg.zRot));
		matrixStack.scale(1.3f, 1.25f, 1.3f);
		matrixStack.translate(0, -0.125, 0);
		leftBoot.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}

	public HoglinHoovesModel<T> withModelBase(BipedModel<?> modelBase)
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