package net.momostudios.coldsweat.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import net.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import net.momostudios.coldsweat.util.registrylists.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerTemp
{
    @Deprecated // Use PlayerHelper instead
    public static Temperature getTemperature(PlayerEntity player, PlayerHelper.Types type)
    {
        return PlayerHelper.getTemperature(player, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static void setTemperature(PlayerEntity player, PlayerHelper.Types type, Temperature temp)
    {
        PlayerHelper.setTemperature(player, temp, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static void addModifier(PlayerEntity player, TempModifier modifier, PlayerHelper.Types type, boolean duplicates)
    {
        PlayerHelper.addModifier(player, modifier, type, duplicates);
    }

    @Deprecated // Use PlayerHelper instead
    public static void removeModifiers(PlayerEntity player, PlayerHelper.Types type, int count, Predicate<TempModifier> condition)
    {
        PlayerHelper.removeModifiers(player, type, count, condition);
    }

    @Deprecated // Use PlayerHelper instead
    public static List<TempModifier> getModifiers(PlayerEntity player, PlayerHelper.Types type)
    {
        return PlayerHelper.getModifiers(player, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static boolean hasModifier(PlayerEntity player, Class<? extends TempModifier> modClass, PlayerHelper.Types type)
    {
        return PlayerHelper.hasModifier(player, modClass, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static void forEachModifier(PlayerEntity player, PlayerHelper.Types type, Consumer<TempModifier> action)
    {
        PlayerHelper.forEachModifier(player, type, action);
    }

    @Deprecated // Use PlayerHelper instead
    public enum Types
    {
        AMBIENT,
        BODY,
        BASE,
        COMPOSITE,
        RATE
    }
}
