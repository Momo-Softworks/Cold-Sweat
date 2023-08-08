package dev.momostudios.coldsweat.util.entity;

import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

public class EntityHelper
{
    private EntityHelper() {}

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
        return getItemInHand(player, arm).getItem() == ModItems.SOULSPRING_LAMP;
    }

    public static Capability<ITemperatureCap> getTemperatureCap(LivingEntity entity)
    {
        return entity instanceof Player ? ModCapabilities.PLAYER_TEMPERATURE : ModCapabilities.ENTITY_TEMPERATURE;
    }
}
