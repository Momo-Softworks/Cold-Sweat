package com.momosoftworks.coldsweat.util.entity;

import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.lang.reflect.Field;
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

    public static GameType getGameModeForPlayer(PlayerEntity player)
    {
        return player instanceof ServerPlayerEntity
               ? ObjectBuilder.build((() ->
                 {
                     ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) player);
                     Field gameMode = ObfuscationReflectionHelper.findField(ServerPlayerEntity.class, "field_71134_c");
                     gameMode.setAccessible(true);
                     try
                     {   return (GameType) gameMode.get(serverPlayer);
                     }
                     catch (IllegalAccessException e)
                     {   throw new RuntimeException(e);
                     }
                 }))
               : ClientOnlyHelper.getGameMode();
    }

    public static ServerPlayerEntity getServerPlayer(PlayerEntity player)
    {   return ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getPlayerList().getPlayer(player.getUUID());
    }
}
