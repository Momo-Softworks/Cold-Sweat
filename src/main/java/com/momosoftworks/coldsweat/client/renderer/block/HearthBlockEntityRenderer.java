package com.momosoftworks.coldsweat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.HearthBottomBlock;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HearthBlockEntityRenderer implements BlockEntityRenderer<HearthBlockEntity>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/block/hearth.png");
    public static final ResourceLocation TEXTURE_SMART = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/block/hearth_smart.png");
    public static final ResourceLocation TEXTURE_HEAT_ON = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/block/hearth_heat_on.png");
    public static final ResourceLocation TEXTURE_COLD_ON = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/block/hearth_cold_on.png");
    public static final ResourceLocation TEXTURE_FROST = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/block/hearth_frost.png");
    public static final ResourceLocation TEXTURE_LIT = ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/block/hearth_lit.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hearth"), "main");

    private final ModelPart body;
    private final ModelPart grate;

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
    public void render(HearthBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        BlockState blockstate = blockEntity.getBlockState();
        poseStack.pushPose();
        float f = blockstate.getValue(HorizontalDirectionalBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);

        VertexConsumer baseVertexes = buffer.getBuffer(RenderType.entityCutout(getTexture(blockstate)));

        /* Main Body */
        this.body.render(poseStack, baseVertexes, light, overlay);
        this.grate.render(poseStack, baseVertexes, light, overlay);

        /* Fuel Textures */
        // Lit texture when fuel is burning
        if (blockstate.getValue(HearthBottomBlock.LIT))
        {   VertexConsumer litVertexes = buffer.getBuffer(RenderType.entityCutout(TEXTURE_LIT));
            this.grate.render(poseStack, litVertexes, light, overlay);
        }
        // Frost texture when cold fuel is present
        if (blockstate.getValue(HearthBottomBlock.FROSTED))
        {   VertexConsumer frostedVertexes = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FROST));
            this.body.render(poseStack, frostedVertexes, light, overlay);
        }

        /* Redstone Power Textures */
        if (!blockstate.getValue(HearthBottomBlock.SMART))
        {
            // Heating
            if (blockstate.getValue(HearthBottomBlock.HEATING))
            {   // Redstone power to heat side
                VertexConsumer heatingVertexes = buffer.getBuffer(RenderType.entityCutout(TEXTURE_HEAT_ON));
                this.body.render(poseStack, heatingVertexes, light, overlay);
            }
            // Cooling
            if (blockstate.getValue(HearthBottomBlock.COOLING))
            {   // Redstone power to cool side
                VertexConsumer coolingVertexes = buffer.getBuffer(RenderType.entityCutout(TEXTURE_COLD_ON));
                this.body.render(poseStack, coolingVertexes, light, overlay);
            }
        }
        poseStack.popPose();
    }

    public static ResourceLocation getTexture(BlockState state)
    {   return state.getValue(HearthBottomBlock.SMART) ? TEXTURE_SMART : TEXTURE;
    }
}
