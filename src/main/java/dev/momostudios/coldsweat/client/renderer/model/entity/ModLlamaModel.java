package dev.momostudios.coldsweat.client.renderer.model.entity;// Made with Blockbench 4.8.1
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.math.MathHelper;

public class ModLlamaModel extends net.minecraft.client.renderer.entity.model.LlamaModel<LlamaEntity>
{
	public final ModelRenderer head;
	public final ModelRenderer chest1;
	public final ModelRenderer chest2;
	public final ModelRenderer chest2_r1;
	public final ModelRenderer body;
	public final ModelRenderer leg0;
	public final ModelRenderer leg1;
	public final ModelRenderer leg2;
	public final ModelRenderer leg3;

	public ModLlamaModel(float expansion)
	{
		super(expansion);
		texWidth = 128;
		texHeight = 64;

		head = new ModelRenderer(this);
		head.setPos(0.0F, 7.0F, -6.0F);
		head.texOffs(0, 0).addBox(-2.0F, -14.0F, -9.5F, 4.0F, 4.0F, 9.0F, 0.0F, false);
		head.texOffs(0, 14).addBox(-4.0F, -16.0F, -5.5F, 8.0F, 18.0F, 6.0F, 0.0F, false);
		head.texOffs(17, 0).addBox(-4.0F, -19.0F, -3.5F, 3.0F, 3.0F, 2.0F, 0.0F, false);
		head.texOffs(17, 0).addBox(1.0F, -19.0F, -3.5F, 3.0F, 3.0F, 2.0F, 0.0F, false);
		head.texOffs(0, 44).addBox(-3.0F, -14.5F, -4.0F, 6.0F, 16.0F, 4.0F, 0.0F, false);

		chest1 = new ModelRenderer(this);
		chest1.setPos(-8.5F, 3.0F, 3.0F);
		setRotationAngle(chest1, 0.0F, 1.5708F, 0.0F);
		chest1.texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, 0.0F, false);

		chest2 = new ModelRenderer(this);
		chest2.setPos(5.5F, 3.0F, 3.0F);
		setRotationAngle(chest2, 0.0F, 1.5708F, 0.0F);


		chest2_r1 = new ModelRenderer(this);
		chest2_r1.setPos(1.0F, 4.0F, 1.5F);
		chest2.addChild(chest2_r1);
		setRotationAngle(chest2_r1, 0.0F, 3.1416F, 0.0F);
		chest2_r1.texOffs(45, 28).addBox(-4.0F, -4.0F, -1.5F, 8.0F, 8.0F, 3.0F, 0.0F, false);

		body = new ModelRenderer(this);
		body.setPos(0.0F, 5.0F, 2.0F);
		setRotationAngle(body, 1.5708F, 0.0F, 0.0F);
		body.texOffs(45, 39).addBox(-5.5F, -8.0F, -5.0F, 11.0F, 14.0F, 7.0F, 0.0F, false);
		body.texOffs(29, 0).addBox(-6.0F, -9.5F, -6.0F, 12.0F, 18.0F, 10.0F, 0.0F, false);

		leg0 = new ModelRenderer(this);
		leg0.setPos(-3.5F, 10.0F, 6.0F);
		leg0.texOffs(29, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

		leg1 = new ModelRenderer(this);
		leg1.setPos(3.5F, 10.0F, 6.0F);
		leg1.texOffs(29, 28).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, 0.0F, true);

		leg2 = new ModelRenderer(this);
		leg2.setPos(-3.5F, 10.0F, -5.0F);
		leg2.texOffs(29, 28).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

		leg3 = new ModelRenderer(this);
		leg3.setPos(3.5F, 10.0F, -5.0F);
		leg3.texOffs(29, 28).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 14.0F, 4.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(LlamaEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.body.xRot = ((float)Math.PI / 2F);
		this.leg0.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg1.xRot = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg2.xRot = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg3.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		boolean flag = !entity.isBaby() && entity.hasChest();
		this.chest1.visible = flag;
		this.chest2.visible = flag;
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		matrixStack.pushPose();
		if (this.young)
		{	matrixStack.scale(0.71428573F, 0.64935064F, 0.7936508F);
			matrixStack.translate(0.0D, 1.3125D, (double)0.22F);
			this.head.render(matrixStack, buffer, packedLight, packedOverlay, red, blue, green, alpha);
			matrixStack.popPose();
			matrixStack.pushPose();
			matrixStack.scale(0.625F, 0.45454544F, 0.45454544F);
			matrixStack.translate(0.0D, 2.0625D, 0.0D);
			this.body.render(matrixStack, buffer, packedLight, packedOverlay, red, blue, green, alpha);
			matrixStack.popPose();
			matrixStack.pushPose();
			matrixStack.scale(0.45454544F, 0.41322312F, 0.45454544F);
			matrixStack.translate(0.0D, 2.0625D, 0.0D);
		}
		ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.leg2, this.leg3, this.chest1, this.chest2).forEach((p_228279_8_) ->
	    {	p_228279_8_.render(matrixStack, buffer, packedLight, packedOverlay, red, blue, green, alpha);
	    });
		matrixStack.popPose();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}