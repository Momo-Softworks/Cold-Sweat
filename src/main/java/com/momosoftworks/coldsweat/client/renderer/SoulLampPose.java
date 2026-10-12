package com.momosoftworks.coldsweat.client.renderer;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.HandleSoulLampAnim;
import com.momosoftworks.coldsweat.client.event.RenderLampHand;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
 * @param rightLamp Whether the right arm is posed with the lamp. False if the lamp is hidden by a two-handed pose
 * @param leftLamp Whether the left arm is posed with the lamp. False if the lamp is hidden by a two-handed pose
 * @param rightRaise How far the right arm is raised to hold the lamp, from 0 to 1
 * @param leftRaise How far the left arm is raised to hold the lamp, from 0 to 1
 * @param rightHidden Whether the lamp in the right hand is hidden by a two-handed pose
 * @param leftHidden Whether the lamp in the left hand is hidden by a two-handed pose
 */
public record SoulLampPose(boolean rightLamp, boolean leftLamp,
                           float rightRaise, float leftRaise,
                           boolean isPlayer,
                           boolean rightHidden, boolean leftHidden)
{
    public static final ContextKey<SoulLampPose> KEY = new ContextKey<>(ColdSweat.createKey("soul_lamp_pose"));

    public boolean holdingLamp(HumanoidArm arm)
    {   return arm == HumanoidArm.RIGHT ? rightLamp : leftLamp;
    }

    public boolean isLampHidden(HumanoidArm arm)
    {   return arm == HumanoidArm.RIGHT ? rightHidden : leftHidden;
    }

    public float getArmRaise(HumanoidArm arm)
    {   return arm == HumanoidArm.RIGHT ? rightRaise : leftRaise;
    }

    @Nullable
    public static SoulLampPose of(LivingEntityRenderState state)
    {   return state.getRenderData(KEY);
    }

    @Nullable
    public static SoulLampPose create(LivingEntity entity, LivingEntityRenderState state)
    {
        boolean rightLamp = EntityHelper.holdingLamp(entity, HumanoidArm.RIGHT);
        boolean leftLamp = EntityHelper.holdingLamp(entity, HumanoidArm.LEFT);
        float rightRaise = HandleSoulLampAnim.getArmRaise(entity, HumanoidArm.RIGHT, state.partialTick);
        float leftRaise = HandleSoulLampAnim.getArmRaise(entity, HumanoidArm.LEFT, state.partialTick);

        boolean isPlayer = entity instanceof Player;
        boolean rightHidden = false;
        boolean leftHidden = false;

        if (state instanceof HumanoidRenderState humanoidState)
        {
            // Hide the lamp if the other arm needs this one for a two-handed pose
            if (rightLamp && shouldHideLamp(humanoidState, HumanoidArm.RIGHT, isPlayer))
            {   rightLamp = false;
                rightRaise = 0;
                rightHidden = true;
            }
            if (leftLamp && shouldHideLamp(humanoidState, HumanoidArm.LEFT, isPlayer))
            {   leftLamp = false;
                leftRaise = 0;
                leftHidden = true;
            }
        }
        if (!rightLamp && !leftLamp && !rightHidden && !leftHidden && rightRaise <= 0.01f && leftRaise <= 0.01f)
        {   return null;
        }
        return new SoulLampPose(rightLamp, leftLamp, rightRaise, leftRaise, isPlayer, rightHidden, leftHidden);
    }

    /**
     * Checks if the other arm's pose needs both hands, and updates the lamp arm's pose to match.<br>
     * Loaded crossbows are exempt, since they can be fired with one hand.
     */
    private static boolean shouldHideLamp(HumanoidRenderState state, HumanoidArm lampArm, boolean isPlayer)
    {
        HumanoidModel.ArmPose lampPose = RenderLampHand.LAMP_ARM_POSE.getValue();
        HumanoidModel.ArmPose otherPose = lampArm == HumanoidArm.RIGHT ? state.leftArmPose : state.rightArmPose;
        HumanoidModel.ArmPose armPose = lampArm == HumanoidArm.RIGHT ? state.rightArmPose : state.leftArmPose;

        boolean hide = otherPose.isTwoHanded() && otherPose != HumanoidModel.ArmPose.CROSSBOW_HOLD;
        // Vanilla gives a player's lamp arm the ITEM pose when the other arm is two-handed
        HumanoidModel.ArmPose newPose = hide && armPose == lampPose ? HumanoidModel.ArmPose.ITEM
                                      : !hide && isPlayer && armPose == HumanoidModel.ArmPose.ITEM ? lampPose
                                      : armPose;
        if (lampArm == HumanoidArm.RIGHT)
        {   state.rightArmPose = newPose;
        }
        else state.leftArmPose = newPose;

        return hide;
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
                {   state.setRenderData(KEY, create(entity, state));
                }
                else state.setRenderData(KEY, null);
            });
        }
    }
}
