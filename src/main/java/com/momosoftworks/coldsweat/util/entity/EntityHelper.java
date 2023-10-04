package com.momosoftworks.coldsweat.util.entity;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;

public class EntityHelper
{
    private EntityHelper() {}

    public static ItemStack getItemInHand(EntityLivingBase player)
    {   return player.getHeldItem();
    }

    // TODO: 10/1/23 Fix this when items are added
    public static boolean holdingLamp(EntityLivingBase player)
    {   return false;//return getItemInHand(player).getItem() == ModItems.SOULSPRING_LAMP;
    }

    static final Method GET_VOICE_PITCH;
    static
    {   GET_VOICE_PITCH = ReflectionHelper.findMethod(EntityLivingBase.class, null, new String[] {"func_70647_i"});
        GET_VOICE_PITCH.setAccessible(true);
    }
    public static float getVoicePitch(EntityLivingBase entity)
    {
        try
        {   return (float) GET_VOICE_PITCH.invoke(entity);
        }
        catch (Exception e)
        {   return 1f;
        }
    }
}
