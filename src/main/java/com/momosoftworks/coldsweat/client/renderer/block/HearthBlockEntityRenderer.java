package com.momosoftworks.coldsweat.client.renderer.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.HearthBottomBlock;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class HearthBlockEntityRenderer extends TileEntityRenderer<HearthBlockEntity>
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/hearth.png");
    public static final ResourceLocation TEXTURE_SMART = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/hearth_smart.png");
    public static final ResourceLocation TEXTURE_HEAT_ON = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/hearth_heat_on.png");
    public static final ResourceLocation TEXTURE_COLD_ON = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/hearth_cold_on.png");
    public static final ResourceLocation TEXTURE_FROST = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/hearth_frost.png");
    public static final ResourceLocation TEXTURE_LIT = new ResourceLocation(ColdSweat.MOD_ID, "textures/block/hearth_lit.png");

    private final ModelRenderer body;
    private final ModelRenderer grate;

    public HearthBlockEntityRenderer(TileEntityRendererDispatcher context)
    {
        super(context);

        body = new ModelRenderer(64, 64, 0, 0);
        body.setPos(8.0F, 24.0F, -8.0F);
        body.texOffs(0, 0).addBox(-16.0F, -16.0F, 0.0F, 16.0F, 16.0F, 16.0F, 0.0F, false);

        grate = new ModelRenderer(64, 64, 0, 32);
        grate.setPos(0.0F, 24.0F, 0.0F);
        grate.texOffs(0, 32).addBox(-5.0F, -9.0F, -9.0F, 10.0F, 7.0F, 1.0F, 0.0F, false);
    }

    @Override
    public void render(HearthBlockEntity blockEntity, float pPartialTicks, MatrixStack poseStack, IRenderTypeBuffer buffer, int light, int overlay)
    {
        BlockState blockstate = blockEntity.getBlockState();
        poseStack.pushPose();
        float f = blockstate.getValue(HorizontalBlock.FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180));
        poseStack.translate(0, -1, 0);

        IVertexBuilder baseVertexes = buffer.getBuffer(RenderType.entityCutout(getTexture(blockstate)));

        /* Main Body */
        this.body.render(poseStack, baseVertexes, light, overlay);
        this.grate.render(poseStack, baseVertexes, light, overlay);

        /* Fuel Textures */
        // Lit texture when fuel is burning
        if (blockstate.getValue(HearthBottomBlock.LIT))
        {   IVertexBuilder litVertexes = buffer.getBuffer(RenderType.entityCutout(TEXTURE_LIT));
            this.grate.render(poseStack, litVertexes, light, overlay);
        }
        // Frost texture when cold fuel is present
        if (blockstate.getValue(HearthBottomBlock.FROSTED))
        {   IVertexBuilder frostedVertexes = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FROST));
            this.body.render(poseStack, frostedVertexes, light, overlay);
        }

        /* Redstone Power Textures */
        if (!blockstate.getValue(HearthBottomBlock.SMART))
        {
            // Heating
            if (blockstate.getValue(HearthBottomBlock.HEATING))
            {   // Redstone power to heat side
                IVertexBuilder heatingVertexes = buffer.getBuffer(RenderType.entityCutout(TEXTURE_HEAT_ON));
                this.body.render(poseStack, heatingVertexes, light, overlay);
            }
            // Cooling
            if (blockstate.getValue(HearthBottomBlock.COOLING))
            {   // Redstone power to cool side
                IVertexBuilder coolingVertexes = buffer.getBuffer(RenderType.entityCutout(TEXTURE_COLD_ON));
                this.body.render(poseStack, coolingVertexes, light, overlay);
            }
        }
        poseStack.popPose();
    }

    public static ResourceLocation getTexture(BlockState state)
    {   return state.getValue(HearthBottomBlock.SMART) ? TEXTURE_SMART : TEXTURE;
    }
}
