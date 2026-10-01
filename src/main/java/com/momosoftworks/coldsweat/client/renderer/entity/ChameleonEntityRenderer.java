package com.momosoftworks.coldsweat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.renderer.entity.state.ChameleonRenderState;
import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonColorLayer;
import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonTongueLayer;
import com.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public class ChameleonEntityRenderer extends AgeableMobRenderer<Chameleon, ChameleonRenderState, ChameleonModel>
{
    public static final Identifier CHAMELEON_SHED  = ColdSweat.createKey("textures/entity/chameleon/chameleon_shed.png");
    public static final Identifier CHAMELEON_GREEN = ColdSweat.createKey("textures/entity/chameleon/chameleon_green.png");
    public static final Identifier CHAMELEON_RED   = ColdSweat.createKey("textures/entity/chameleon/chameleon_red.png");
    public static final Identifier CHAMELEON_BLUE  = ColdSweat.createKey("textures/entity/chameleon/chameleon_blue.png");

    private final ChameleonModel adultModel;
    private final ChameleonModel babyModel;

    public ChameleonEntityRenderer(EntityRendererProvider.Context context)
    {
        this(context, new ChameleonModel(context.bakeLayer(ChameleonModel.LAYER_LOCATION)),
                      new ChameleonModel(context.bakeLayer(ChameleonModel.BABY_LAYER_LOCATION)));
    }

    private ChameleonEntityRenderer(EntityRendererProvider.Context context, ChameleonModel adultModel, ChameleonModel babyModel)
    {
        super(context, adultModel, babyModel, 0.5f);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        this.addLayer(new ChameleonColorLayer(this));
        this.addLayer(new ChameleonTongueLayer(this,
                                               new ChameleonModel(context.bakeLayer(ChameleonModel.LAYER_LOCATION), true),
                                               new ChameleonModel(context.bakeLayer(ChameleonModel.BABY_LAYER_LOCATION), true)));
    }

    @Override
    public ChameleonRenderState createRenderState()
    {   return new ChameleonRenderState();
    }

    @Override
    public void extractRenderState(Chameleon entity, ChameleonRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        state.chameleon = entity;

        // Riding on a player's head
        if (entity.getVehicle() instanceof Player player)
        {
            state.ridingPlayer = true;
            state.ridingYaw = CSMath.blend(player.yHeadRotO, player.yHeadRot, partialTick, 0, 1);
            state.ridingPitch = player.getViewXRot(partialTick);
            state.ridingOffset = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HOGLIN_HELMET) ? 0.65f : 0.4f;
        }
        else state.ridingPlayer = false;

        // Fade out after getting hurt
        long tickCount = entity.getAgeTicks();
        long hurtTime = entity.getHurtTimestamp();
        if (entity.isAlive())
        {
            if (CSMath.betweenInclusive(tickCount - hurtTime, 0, 40) && hurtTime != 0 && entity.opacity > 0.15f)
            {   entity.opacity = CSMath.blend(1, 0.15f, tickCount + partialTick - hurtTime, 0, 40);
            }
            else if (entity.opacity < 1)
            {   entity.opacity = CSMath.blend(0.15f, 1, tickCount + partialTick - hurtTime, 120, 180);
            }
        }
        state.opacity = entity.opacity;

        // Animate once per frame; the models apply the stored poses
        (state.isBaby ? this.babyModel : this.adultModel).computeAnimation(entity, state);
    }

    @Override
    public void submit(ChameleonRenderState state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera)
    {
        ps.pushPose();
        if (state.ridingPlayer)
        {
            ps.mulPose(CSMath.toQuaternion(0, -CSMath.toRadians(state.ridingYaw), 0));
            ps.translate(0, -0.4, 0);
            ps.mulPose(CSMath.toQuaternion(CSMath.toRadians(state.ridingPitch), 0, 0));
            ps.translate(0, state.ridingOffset, 0);
            ps.mulPose(CSMath.toQuaternion(0, CSMath.toRadians(state.ridingYaw), 0));
        }
        super.submit(state, ps, collector, camera);
        ps.popPose();
    }

    @Override
    protected int getModelTint(ChameleonRenderState state)
    {   return ARGB.white(state.opacity);
    }

    @Override
    public Identifier getTextureLocation(ChameleonRenderState state)
    {   return CHAMELEON_GREEN;
    }
}
