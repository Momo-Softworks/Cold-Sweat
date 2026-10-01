package com.momosoftworks.coldsweat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class IceboxBlockEntityRenderer implements BlockEntityRenderer<IceboxBlockEntity, IceboxBlockEntityRenderer.IceboxRenderState>
{
    public static final Identifier TEXTURE = ColdSweat.createKey("textures/block/icebox.png");
    public static final Identifier TEXTURE_SMOKESTACK = ColdSweat.createKey("textures/block/icebox_smokestack.png");
    public static final Identifier TEXTURE_FROST = ColdSweat.createKey("textures/block/icebox_frost.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ColdSweat.createKey("icebox"), "main");

    private final IceboxModel model;
    private final ModelPart container;

    public static class IceboxRenderState extends BlockEntityRenderState
    {
        public @Nullable BlockState blockState;
        public float lidRot;
    }

    /**
     * Parts are drawn after submission, so the lid angle is applied per-icebox from the render state
     */
    public static class IceboxModel extends Model<IceboxRenderState>
    {
        private final ModelPart lid;

        public IceboxModel(ModelPart root)
        {   super(root, RenderTypes::entityCutout);
            this.lid = root.getChild("lid");
        }

        @Override
        public void setupAnim(IceboxRenderState state)
        {   super.setupAnim(state);
            this.lid.xRot = state.lidRot;
        }
    }

    public IceboxBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.model = new IceboxModel(context.bakeLayer(LAYER_LOCATION));
        this.container = context.bakeLayer(LAYER_LOCATION).getChild("container");
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
    public IceboxRenderState createRenderState()
    {   return new IceboxRenderState();
    }

    @Override
    public void extractRenderState(IceboxBlockEntity blockEntity, IceboxRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        BlockState blockstate = blockEntity.getBlockState();
        state.blockState = blockstate;
        if (blockstate.hasProperty(IceboxBlock.SMOKESTACK) && !blockstate.getValue(IceboxBlock.SMOKESTACK))
        {
            float openness = blockEntity.getOpenNess(partialTick);
            openness = 1.0F - openness;
            openness = 1.0F - (float) Math.pow(openness, 3f);
            state.lidRot = -(openness * ((float)Math.PI / 2F)) * 0.999f;
        }
        else state.lidRot = 0;
    }

    @Override
    public void submit(IceboxRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
    {
        BlockState blockstate = state.blockState;
        if (blockstate == null || !blockstate.hasProperty(HorizontalDirectionalBlock.FACING)) return;

        poseStack.pushPose();
        float rotation = blockstate.getValue(HorizontalDirectionalBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);

        collector.submitModel(this.model, state, poseStack, RenderTypes.entityCutout(getTexture(blockstate)), state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);

        // Render frost texture
        if (blockstate.getValue(IceboxBlock.FROSTED))
        {   collector.submitModelPart(this.container, poseStack, RenderTypes.entityTranslucent(TEXTURE_FROST), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        }

        poseStack.popPose();
    }

    public static Identifier getTexture(BlockState state)
    {   return state.getValue(IceboxBlock.SMOKESTACK) ? TEXTURE_SMOKESTACK : TEXTURE;
    }
}
