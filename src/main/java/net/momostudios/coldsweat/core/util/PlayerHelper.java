package net.momostudios.coldsweat.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import net.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import net.momostudios.coldsweat.core.util.registrylists.ModItems;

public class PlayerHelper
{
    public static ItemStack getItemInHand(PlayerEntity player, HandSide hand)
    {
        return player.getHeldItem(hand == player.getPrimaryHand() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static HandSide getHandSide(Hand hand, PlayerEntity player)
    {
        return hand == Hand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand() == HandSide.RIGHT ? HandSide.LEFT : HandSide.RIGHT;
    }

    public static boolean holdingLamp(PlayerEntity player, HandSide hand)
    {
        return PlayerHelper.getItemInHand(player, hand).getItem() == ModItems.SOULFIRE_LAMP;
    }

    public static void updateModifiers(PlayerEntity player)
    {
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                new PlayerModifiersSyncMessage(
                        player,
                        PlayerTemp.getModifiers(player, PlayerTemp.Types.AMBIENT),
                        PlayerTemp.getModifiers(player, PlayerTemp.Types.BODY),
                        PlayerTemp.getModifiers(player, PlayerTemp.Types.BASE),
                        PlayerTemp.getModifiers(player, PlayerTemp.Types.RATE)));
    }

    public static void updateTemperature(PlayerEntity player)
    {
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                new PlayerTempSyncMessage(PlayerTemp.getTemperature(player, PlayerTemp.Types.BODY).get(), PlayerTemp.getTemperature(player, PlayerTemp.Types.BASE).get()));
    }
}
