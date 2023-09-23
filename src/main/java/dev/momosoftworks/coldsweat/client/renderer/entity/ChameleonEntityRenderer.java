package dev.momosoftworks.coldsweat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.momosoftworks.coldsweat.ColdSweat;
import dev.momosoftworks.coldsweat.client.renderer.layer.ChameleonColorLayer;
import dev.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import dev.momosoftworks.coldsweat.common.entity.Chameleon;
import dev.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ChameleonEntityRenderer<T extends Chameleon> extends MobRenderer<Chameleon, ChameleonModel<Chameleon>>
{
    public static final ResourceLocation CHAMELEON_SHED  = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_shed.png");
    public static final ResourceLocation CHAMELEON_GREEN = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_green.png");
    public static final ResourceLocation CHAMELEON_RED   = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_red.png");
    public static final ResourceLocation CHAMELEON_BLUE  = new ResourceLocation(ColdSweat.MOD_ID, "textures/entities/chameleon_blue.png");

    public ChameleonEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context, new ChameleonModel<>(context.bakeLayer(ChameleonModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new ChameleonColorLayer<>(this));
    }

    @Override
    public void render(Chameleon entity, float p_115456_, float partialTick, PoseStack ps, MultiBufferSource buffer, int light)
    {
        ps.pushPose();
        if (entity.getVehicle() instanceof Player player)
        {
            float playerHeadYaw = CSMath.blend(player.yHeadRotO, player.yHeadRot, partialTick, 0, 1);
            float playerHeadPitch = player.getViewXRot(partialTick);
            float ridingOffset = (float) entity.getMyRidingOffset();
            ps.translate(0, -(ridingOffset - 0.05), 0);
            ps.mulPose(CSMath.getQuaternion(CSMath.toRadians(playerHeadPitch), -CSMath.toRadians(playerHeadYaw), 0));
            ps.translate(0, ridingOffset, 0);
            ps.mulPose(CSMath.getQuaternion(0, CSMath.toRadians(playerHeadYaw), 0));

        }
        super.render(entity, p_115456_, partialTick, ps, buffer, light);
        ps.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(Chameleon entity)
    {
        return CHAMELEON_GREEN;
    }
}

