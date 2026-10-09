package com.momosoftworks.coldsweat.client.renderer;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
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
 *
 * @param rightRaise How far the right arm is raised to hold the lamp, from 0 to 1
 * @param leftRaise How far the left arm is raised to hold the lamp, from 0 to 1
 */
public record SoulLampPose(boolean rightLamp, boolean leftLamp,
                           float rightRaise, float leftRaise,
                           boolean isPlayer)
{
    public static final ContextKey<SoulLampPose> KEY = new ContextKey<>(ColdSweat.createKey("soul_lamp_pose"));

    public boolean holdingLamp(HumanoidArm arm)
    {   return arm == HumanoidArm.RIGHT ? rightLamp : leftLamp;
    }

    public float getArmRaise(HumanoidArm arm)
    {   return arm == HumanoidArm.RIGHT ? rightRaise : leftRaise;
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
        float rightRaise = HandleSoulLampAnim.getArmRaise(entity, HumanoidArm.RIGHT, partialTick);
        float leftRaise = HandleSoulLampAnim.getArmRaise(entity, HumanoidArm.LEFT, partialTick);
        if (!rightLamp && !leftLamp && rightRaise <= 0.01f && leftRaise <= 0.01f)
        {   return null;
        }
        return new SoulLampPose(rightLamp, leftLamp, rightRaise, leftRaise, entity instanceof Player);
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
