package com.momosoftworks.coldsweat.util.entity;

import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class EntityHelper
{
    private EntityHelper() {}

    public static ItemStack getItemInHand(LivingEntity player, HandSide hand)
    {   return player.getItemInHand(hand == player.getMainArm() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static HandSide getArmFromHand(Hand hand, PlayerEntity player)
    {   return hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm() == HandSide.RIGHT ? HandSide.LEFT : HandSide.RIGHT;
    }

    public static boolean holdingLamp(LivingEntity player, HandSide arm)
    {   return getItemInHand(player, arm).getItem() == ModItems.SOULSPRING_LAMP;
    }

    public static HandSide getHandSide(Hand hand, PlayerEntity player)
    {   return hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm() == HandSide.RIGHT ? HandSide.LEFT : HandSide.RIGHT;
    }

    static final Method GET_VOICE_PITCH;
    static
    {   GET_VOICE_PITCH = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_70647_i");
        GET_VOICE_PITCH.setAccessible(true);
    }
    public static float getVoicePitch(LivingEntity entity)
    {   try
        {   return (float) GET_VOICE_PITCH.invoke(entity);
        }
        catch (Exception e)
        {   return 1f;
        }
    }
}
