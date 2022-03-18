package dev.momostudios.coldsweat.util.entity;

import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerHelper
{
    public static ItemStack getItemInHand(LivingEntity player, HumanoidArm hand)
    {
        return player.getItemInHand(hand == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static HumanoidArm getArmFromHand(InteractionHand hand, Player player)
    {
        return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public static boolean holdingLamp(LivingEntity player, HumanoidArm arm)
    {
        return getItemInHand(player, arm).getItem() == ModItems.HELLSPRING_LAMP;
    }
}
