package com.momosoftworks.coldsweat.client.renderer;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import javax.annotation.Nullable;

/**
 * Data needed to pose an entity's arms while it holds a soulspring lamp.<br>
 * Models only have access to render states, so this is attached to the state when it's extracted.
 */
public record SoulLampPose(boolean rightLamp, boolean leftLamp,
                           float rightArmRot, float leftArmRot,
                           boolean crouching, @Nullable HumanoidArm swingingArm,
                           float attackAnim, float pitch)
{
    public static final ContextKey<SoulLampPose> KEY = new ContextKey<>(ColdSweat.createKey("soul_lamp_pose"));

    public boolean holdingLamp(HumanoidArm arm)
    {   return arm == HumanoidArm.RIGHT ? rightLamp : leftLamp;
    }

    @Nullable
    public static SoulLampPose of(LivingEntityRenderState state)
    {   return state.getRenderData(KEY);
    }

    @Nullable
    public static SoulLampPose create(LivingEntity entity, float partialTick)
    {
        boolean rightLamp = EntityHelper.holdingLamp(entity, HumanoidArm.RIGHT);
        boolean leftLamp = EntityHelper.holdingLamp(entity, HumanoidArm.LEFT);
        Pair<Float, Float> rightRot = HandleSoulLampAnim.RIGHT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        Pair<Float, Float> leftRot = HandleSoulLampAnim.LEFT_ARM_ROTATIONS.getOrDefault(entity, Pair.of(0f, 0f));
        float rightArmRot = CSMath.toRadians(CSMath.blend(rightRot.getSecond(), rightRot.getFirst(), partialTick, 0, 1));
        float leftArmRot = CSMath.blend(CSMath.toRadians(leftRot.getSecond()), CSMath.toRadians(leftRot.getFirst()), partialTick, 0, 1);
        if (!rightLamp && !leftLamp && CSMath.betweenInclusive(rightArmRot, -0.01, 0.01) && CSMath.betweenInclusive(leftArmRot, -0.01, 0.01))
        {   return null;
        }

        HumanoidArm swingingArm = entity instanceof Player player && player.swinging ? EntityHelper.getArmFromHand(player.swingingArm, player) : null;
        return new SoulLampPose(rightLamp, leftLamp, rightArmRot, leftArmRot, entity.isCrouching(), swingingArm,
                                entity.getAttackAnim(partialTick), entity.getViewXRot(partialTick));
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static final class Registration
    {
        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void registerModifiers(RegisterRenderStateModifiersEvent event)
        {
            event.registerEntityModifier((Class) LivingEntityRenderer.class, (LivingEntity entity, LivingEntityRenderState state) ->
            {
                if (ConfigSettings.POSE_SOULSPRING_LAMP.get())
                {   state.setRenderData(KEY, create(entity, state.partialTick));
                }
                else state.setRenderData(KEY, null);
            });
        }
    }
}
