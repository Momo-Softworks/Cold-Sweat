package com.momosoftworks.coldsweat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

public class IceboxBlockEntityRenderer implements BlockEntityRenderer<IceboxBlockEntity>
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/icebox.png");
    public static final ResourceLocation TEXTURE_SMOKESTACK = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/icebox_smokestack.png");
    public static final ResourceLocation TEXTURE_FROST = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/icebox_frost.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ColdSweat.MOD_ID, "icebox"), "main");

    ModelPart container;
    ModelPart lid;

    public IceboxBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        ModelPart base = context.bakeLayer(LAYER_LOCATION);
        this.container = base.getChild("container");
        this.lid = base.getChild("lid");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -3.0F, -16.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 8.0F));
        PartDefinition container = partdefinition.addOrReplaceChild("container", CubeListBuilder.create().texOffs(0, 19)
                .addBox(-8.0F, -13.0F, -8.0F, 16.0F, 13.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 48);
    }

    @Override
    public void render(IceboxBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        BlockState blockstate = blockEntity.getBlockState();
        poseStack.pushPose();
        float f = blockstate.getValue(ChestBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);

        VertexConsumer vertexes = buffer.getBuffer(RenderType.entityCutout(getTexture(blockstate)));
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
        {   VertexConsumer frostedVertexes = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FROST));
            this.container.render(poseStack, frostedVertexes, light, overlay);
        }

        poseStack.popPose();
    }

    public static ResourceLocation getTexture(BlockState state)
    {   return state.getValue(IceboxBlock.SMOKESTACK) ? TEXTURE_SMOKESTACK : TEXTURE;
    }
}
