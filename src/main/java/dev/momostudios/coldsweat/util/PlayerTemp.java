package dev.momostudios.coldsweat.util;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerTemp
{
    //TODO: Remove these methods and use PlayerHelper instead
    @Deprecated // Use PlayerHelper instead
    public static Temperature getTemperature(Player player, PlayerHelper.Types type)
    {
        return PlayerHelper.getTemperature(player, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static void setTemperature(Player player, Temperature temp, PlayerHelper.Types type)
    {
        PlayerHelper.setTemperature(player, temp, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static void addModifier(Player player, TempModifier modifier, PlayerHelper.Types type, boolean duplicates)
    {
        PlayerHelper.addModifier(player, modifier, type, duplicates);
    }

    @Deprecated // Use PlayerHelper instead
    public static void removeModifiers(Player player, PlayerHelper.Types type, int count, Predicate<TempModifier> condition)
    {
        PlayerHelper.removeModifiers(player, type, count, condition);
    }

    @Deprecated // Use PlayerHelper instead
    public static List<TempModifier> getModifiers(Player player, PlayerHelper.Types type)
    {
        return PlayerHelper.getModifiers(player, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static boolean hasModifier(Player player, Class<? extends TempModifier> modClass, PlayerHelper.Types type)
    {
        return PlayerHelper.hasModifier(player, modClass, type);
    }

    @Deprecated // Use PlayerHelper instead
    public static void forEachModifier(Player player, PlayerHelper.Types type, Consumer<TempModifier> action)
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
