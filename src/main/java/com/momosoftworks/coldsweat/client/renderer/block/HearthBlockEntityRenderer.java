package com.momosoftworks.coldsweat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.HearthBottomBlock;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class HearthBlockEntityRenderer implements BlockEntityRenderer<HearthBlockEntity, HearthBlockEntityRenderer.HearthRenderState>
{
    public static final Identifier TEXTURE = ColdSweat.createKey("textures/block/hearth.png");
    public static final Identifier TEXTURE_SMART = ColdSweat.createKey("textures/block/hearth_smart.png");
    public static final Identifier TEXTURE_HEAT_ON = ColdSweat.createKey("textures/block/hearth_heat_on.png");
    public static final Identifier TEXTURE_COLD_ON = ColdSweat.createKey("textures/block/hearth_cold_on.png");
    public static final Identifier TEXTURE_FROST = ColdSweat.createKey("textures/block/hearth_frost.png");
    public static final Identifier TEXTURE_LIT = ColdSweat.createKey("textures/block/hearth_lit.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ColdSweat.createKey("hearth"), "main");

    private final ModelPart body;
    private final ModelPart grate;

    public static class HearthRenderState extends BlockEntityRenderState
    {   public @Nullable BlockState blockState;
    }

    public HearthBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        ModelPart base = context.bakeLayer(LAYER_LOCATION);
        this.body = base.getChild("body");
        this.grate = base.getChild("grate");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, 0.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));
        PartDefinition grate = partdefinition.addOrReplaceChild("grate", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -9.0F, -9.0F, 10.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public HearthRenderState createRenderState()
    {   return new HearthRenderState();
    }

    @Override
    public void extractRenderState(HearthBlockEntity blockEntity, HearthRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.blockState = blockEntity.getBlockState();
    }

    @Override
    public void submit(HearthRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
    {
        BlockState blockstate = state.blockState;
        if (blockstate == null || !blockstate.hasProperty(HorizontalDirectionalBlock.FACING)) return;
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        float f = blockstate.getValue(HorizontalDirectionalBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);

        RenderType baseType = RenderTypes.entityCutout(getTexture(blockstate));

        /* Main Body */
        collector.submitModelPart(this.body, poseStack, baseType, light, overlay, null);
        collector.submitModelPart(this.grate, poseStack, baseType, light, overlay, null);

        /* Fuel Textures */
        // Lit texture when fuel is burning
        if (blockstate.getValue(HearthBottomBlock.LIT))
        {   collector.submitModelPart(this.grate, poseStack, RenderTypes.entityCutout(TEXTURE_LIT), light, overlay, null);
        }
        // Frost texture when cold fuel is present
        if (blockstate.getValue(HearthBottomBlock.FROSTED))
        {   collector.submitModelPart(this.body, poseStack, RenderTypes.entityTranslucent(TEXTURE_FROST), light, overlay, null);
        }

        /* Redstone Power Textures */
        if (!blockstate.getValue(HearthBottomBlock.SMART))
        {
            // Redstone power to heat side
            if (blockstate.getValue(HearthBottomBlock.HEATING))
            {   collector.submitModelPart(this.body, poseStack, RenderTypes.entityCutout(TEXTURE_HEAT_ON), light, overlay, null);
            }
            // Redstone power to cool side
            if (blockstate.getValue(HearthBottomBlock.COOLING))
            {   collector.submitModelPart(this.body, poseStack, RenderTypes.entityCutout(TEXTURE_COLD_ON), light, overlay, null);
            }
        }
        poseStack.popPose();
    }

    public static Identifier getTexture(BlockState state)
    {   return state.getValue(HearthBottomBlock.SMART) ? TEXTURE_SMART : TEXTURE;
    }
}
