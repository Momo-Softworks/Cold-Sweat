package com.momosoftworks.coldsweat.util.entity;

import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.reflect.Field;

public class EntityHelper
{
    private EntityHelper() {}

    public static ItemStack getItemInHand(LivingEntity player, HumanoidArm hand)
    {   return player.getItemInHand(hand == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static HumanoidArm getArmFromHand(InteractionHand hand, Player player)
    {   return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public static boolean holdingLamp(LivingEntity player, HumanoidArm arm)
    {   return getItemInHand(player, arm).getItem() == ModItems.SOULSPRING_LAMP;
    }

    public static GameType getGameModeForPlayer(Player player)
    {
        return player instanceof ServerPlayer serverPlayer
               ? ObjectBuilder.build((() ->
                 {
                     Field gameMode = ObfuscationReflectionHelper.findField(ServerPlayer.class, "f_8941_");
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

    public static ServerPlayer getServerPlayer(Player player)
    {   return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUUID());
    }
}
