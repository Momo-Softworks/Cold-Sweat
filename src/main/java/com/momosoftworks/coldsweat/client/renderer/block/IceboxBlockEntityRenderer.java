package com.momosoftworks.coldsweat.client.renderer.block;

import com.jozufozu.flywheel.core.model.ModelPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.renderer.model.PartPose;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class IceboxBlockEntityRenderer extends TileEntityRenderer<IceboxBlockEntity>
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/icebox.png");
    public static final ResourceLocation TEXTURE_SMOKESTACK = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/icebox_smokestack.png");
    public static final ResourceLocation TEXTURE_FROST = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/icebox_frost.png");

    private final ModelRenderer lid;
    private final ModelRenderer container;

    public IceboxBlockEntityRenderer(TileEntityRendererDispatcher context)
    {
        super(context);

        lid = new ModelRenderer(64, 48, 0, 0);
        lid.setPos(0.0F, 11.0F, 8.0F);
        lid.texOffs(0, 0).addBox(-8.0F, -3.0F, -16.0F, 16.0F, 3.0F, 16.0F, 0.0F, false);

        container = new ModelRenderer(64, 48, 0, 19);
        container.setPos(0.0F, 24.0F, 0.0F);
        container.texOffs(0, 19).addBox(-8.0F, -13.0F, -8.0F, 16.0F, 13.0F, 16.0F, 0.0F, false);
    }

    @Override
    public void render(IceboxBlockEntity blockEntity, float partialTick, MatrixStack poseStack, IRenderTypeBuffer buffer, int light, int overlay)
    {
        BlockState blockstate = blockEntity.getBlockState();
        poseStack.pushPose();
        float f = blockstate.getValue(ChestBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);

        IVertexBuilder vertexes = buffer.getBuffer(RenderType.entityCutout(getTexture(blockstate)));
        if (!blockEntity.hasSmokeStack())
        {
            float openness = blockEntity.getOpenNess(partialTick);
            openness = 1.0F - openness;
            openness = 1.0F - (float) Math.pow(openness, 3f);
            this.lid.xRot = -(openness * ((float)Math.PI / 2F)) * 0.999f;
        }
        else this.lid.xRot = 0;
        this.container.render(poseStack, vertexes, light, overlay);
        this.lid.render(poseStack, vertexes, light, overlay);

        // Render frost texture
        if (blockstate.getValue(IceboxBlock.FROSTED))
        {   IVertexBuilder frostedVertexes = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FROST));
            this.container.render(poseStack, frostedVertexes, light, overlay);
        }

        poseStack.popPose();
    }

    public static ResourceLocation getTexture(BlockState state)
    {   return state.getValue(IceboxBlock.SMOKESTACK) ? TEXTURE_SMOKESTACK : TEXTURE;
    }
}
