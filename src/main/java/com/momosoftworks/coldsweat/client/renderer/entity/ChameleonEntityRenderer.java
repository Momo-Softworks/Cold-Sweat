package com.momosoftworks.coldsweat.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonColorLayer;
import com.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class ChameleonEntityRenderer<T extends ChameleonEntity> extends MobRenderer<ChameleonEntity, ChameleonModel<ChameleonEntity>>
{
    public static final ResourceLocation CHAMELEON_SHED  = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_shed.png");
    public static final ResourceLocation CHAMELEON_GREEN = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_green.png");
    public static final ResourceLocation CHAMELEON_RED   = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_red.png");
    public static final ResourceLocation CHAMELEON_BLUE  = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_blue.png");

    public ChameleonEntityRenderer(EntityRendererManager context)
    {
        super(context, new ChameleonModel<>(), 0.5f);
        this.addLayer(new ChameleonColorLayer<>(this));
    }

    @Override
    public void render(ChameleonEntity entity, float p_115456_, float partialTick, MatrixStack ms, IRenderTypeBuffer buffer, int light)
    {
        ms.pushPose();
        if (entity.getVehicle() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entity.getVehicle();
            float playerHeadYaw = CSMath.blend(player.yHeadRotO, player.yHeadRot, partialTick, 0, 1);
            float playerHeadPitch = player.getViewXRot(partialTick);
            float ridingOffset = (float) entity.getMyRidingOffset();
            ms.translate(0, -(ridingOffset - 0.05), 0);
            ms.mulPose(CSMath.toQuaternion(CSMath.toRadians(playerHeadPitch), -CSMath.toRadians(playerHeadYaw), 0));
            ms.translate(0, ridingOffset, 0);
            ms.mulPose(CSMath.toQuaternion(0, CSMath.toRadians(playerHeadYaw), 0));
            ms.translate(0, -1.45, 0);
        }
        super.render(entity, p_115456_, partialTick, ms, buffer, light);
        ms.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ChameleonEntity entity)
    {   return CHAMELEON_GREEN;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(ChameleonEntity chameleon, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_)
    {   return RenderType.entityTranslucent(this.getTextureLocation(chameleon));
    }
}

