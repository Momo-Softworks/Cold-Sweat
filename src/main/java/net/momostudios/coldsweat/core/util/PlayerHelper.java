package net.momostudios.coldsweat.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
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
}
