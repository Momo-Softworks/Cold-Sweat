package dev.momostudios.coldsweat.client.renderer.model.armor;// Made with Blockbench 4.8.1
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class GoatPantsModel<T extends LivingEntity> extends BipedModel<T>
{
	private final ModelRenderer waist;
	private final ModelRenderer rightLegging;
	private final ModelRenderer leftLegging;

	public GoatPantsModel()
	{
		super(1f);
		texWidth = 64;
		texHeight = 128;

		waist = new ModelRenderer(this);
		waist.setPos(0.0F, 23.0F, 0.0F);
		waist.texOffs(16, 112).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

		rightLegging = new ModelRenderer(this);
		rightLegging.setPos(0.0F, 0.0F, 0.0F);
		rightLegging.texOffs(0, 112).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		leftLegging = new ModelRenderer(this);
		leftLegging.setPos(0.0F, 0.0F, 0.0F);
		leftLegging.texOffs(0, 112).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		// waist
		matrixStack.pushPose();
		matrixStack.translate(body.x / 16, body.y / 16, body.z / 16);
		matrixStack.mulPose(CSMath.getQuaternion(body.xRot, body.yRot, body.zRot));
		matrixStack.scale(1.1f, 1.1f, 1.2f);
		waist.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// right legging
		matrixStack.pushPose();
		matrixStack.translate(rightLeg.x / 16, rightLeg.y / 16, rightLeg.z / 16);
		matrixStack.mulPose(CSMath.getQuaternion(rightLeg.xRot, rightLeg.yRot, rightLeg.zRot));
		matrixStack.scale(1.25f, 1.15f, 1.25f);
		rightLegging.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();

		// left legging
		matrixStack.pushPose();
		matrixStack.translate(leftLeg.x / 16, leftLeg.y / 16, leftLeg.z / 16);
		matrixStack.mulPose(CSMath.getQuaternion(leftLeg.xRot, leftLeg.yRot, leftLeg.zRot));
		matrixStack.scale(1.25f, 1.15f, 1.25f);
		leftLegging.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		matrixStack.popPose();
	}

	public GoatPantsModel<T> withModelBase(BipedModel<?> modelBase)
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