package dev.momostudios.coldsweat.util;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.world.TempModifierEntries;
import dev.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import dev.momostudios.coldsweat.core.event.csevents.TempModifierEvent;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import dev.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerHelper
{
    /**
     * Returns the player's temperature of the specified type.
     */
    public static Temperature getTemperature(Player player, Types type)
    {
        return new Temperature(player.getCapability(PlayerTempCapability.TEMPERATURE).orElse(new PlayerTempCapability()).get(type));
    }

    /**
     * Use {@link TempModifier}s for over-time effects.
     */
    public static void setTemperature(Player player, Temperature value, Types type)
    {
        setTemperature(player, value, type, true);
    }

    public static void setTemperature(Player player, Temperature value, Types type, boolean sync)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(capability ->
        {
            if (sync && !player.level.isClientSide)
            {
                updateTemperature(player,
                        type == Types.BODY ? value : getTemperature(player, Types.BODY),
                        type == Types.BASE ? value : getTemperature(player, Types.BASE),
                        type == Types.AMBIENT ? value : getTemperature(player, Types.AMBIENT));
            }
            capability.set(type, value.get());
        });
    }

    public static void addTemperature(Player player, Temperature value, Types type)
    {
        addTemperature(player, value, type, true);
    }

    public static void addTemperature(Player player, Temperature value, Types type, boolean sync)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(capability ->
        {
            capability.set(type, value.get() + capability.get(type));
            if (sync && !player.level.isClientSide)
            {
                updateTemperature(player,
                        type == Types.BODY ? value : getTemperature(player, Types.BODY),
                        type == Types.BASE ? value : getTemperature(player, Types.BASE),
                        type == Types.AMBIENT ? value : getTemperature(player, Types.AMBIENT));
            }
        });
    }

    /**
     * Applies the given modifier to the player.<br>
     *
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(Player player, TempModifier modifier, Types type, boolean duplicates)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, player, type, duplicates);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
            {
                if (TempModifierEntries.getEntries().getMap().containsKey(event.getModifier().getID()))
                {
                    if (!cap.hasModifier(event.type, event.getModifier().getClass()) || event.duplicatesAllowed)
                    {
                        cap.getModifiers(event.type).add(event.getModifier());
                    }
                }
                else
                {
                    ColdSweat.LOGGER.error("TempModifierEvent.Add: No TempModifier with ID " + modifier.getID() + " found!");
                }

                if (!player.level.isClientSide)
                    updateModifiers(player, cap.getModifiers(Types.BODY), cap.getModifiers(Types.BASE), cap.getModifiers(Types.AMBIENT), cap.getModifiers(Types.RATE));
            });
        }
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param player The player being sampled
     * @param type Determines which TempModifier list to pull from
     * @param count The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(Player player, Types type, int count, Predicate<TempModifier> condition)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            AtomicInteger removed = new AtomicInteger(0);
            cap.getModifiers(type).removeIf(modifier ->
            {
                if (removed.get() < count)
                {
                    TempModifierEvent.Remove event = new TempModifierEvent.Remove(player, type, count, condition);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        if (event.getCondition().test(modifier))
                        {
                            removed.incrementAndGet();
                            return true;
                        }
                    }
                }
                return false;
            });

            if (!player.level.isClientSide)
                updateModifiers(player, cap.getModifiers(Types.BODY), cap.getModifiers(Types.BASE), cap.getModifiers(Types.AMBIENT), cap.getModifiers(Types.RATE));
        });
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @returns a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(Player player, Types type)
    {
        List<TempModifier> mods =  player.getCapability(PlayerTempCapability.TEMPERATURE).orElse(new PlayerTempCapability()).getModifiers(type);
        mods.removeIf(mod -> mod == null || mod.getID() == null ||mod.getID().isEmpty());
        return mods;
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(Player player, Class<? extends TempModifier> modClass, Types type)
    {
        return player.getCapability(PlayerTempCapability.TEMPERATURE).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param type determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(Player player, Types type, Consumer<TempModifier> action)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                cap.getModifiers(type).forEach(action);
            }
        });
    }

    /**
     * Defines all types of temperature in Cold Sweat. <br>
     * These are used to get the player's temperature and/or to apply modifiers to it. <br>
     * <br>
     * {@link #AMBIENT}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * {@link #BODY}: The temperature of the player's body. <br>
     * {@link #BASE}: A static offset applied to the player's body temperature. <br>
     * {@link #COMPOSITE}: The sum of the player's body and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     */
    public enum Types
    {
        AMBIENT,
        BODY,
        BASE,
        COMPOSITE,
        RATE
    }

    /**
     * Used for storing TempModifiers in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of TempModifier to be stored
     * @return The NBT tag name for the given type
     */
    public static String getModifierTag(Types type)
    {
        switch (type)
        {
            case BODY :     return "body_temp_modifiers";
            case AMBIENT :  return "ambient_temp_modifiers";
            case BASE :     return "base_temp_modifiers";
            case RATE :     return "rate_temp_modifiers";
            default : throw new IllegalArgumentException("PlayerTempHandler.getModifierTag() received illegal Type argument");
        }
    }

    /**
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of Temperature to be stored. ({@link Types#AMBIENT} should only be stored when needed to prevent lag)
     * @return The NBT tag name for the given type
     */
    public static String getTempTag(Types type)
    {
        switch (type)
        {
            case BODY :      return "body_temperature";
            case AMBIENT :   return "ambient_temperature";
            case BASE :      return "base_temperature";
            case COMPOSITE : return "composite_temperature";
            default : throw new IllegalArgumentException("PlayerTempHandler.getTempTag() received illegal Type argument");
        }
    }

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

    public static void updateTemperature(Player player, Temperature bodyTemp, Temperature baseTemp, Temperature ambientTemp)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerTempSyncMessage(bodyTemp.get(), baseTemp.get(), ambientTemp.get()));
        }
    }

    public static void updateModifiers(Player player, List<TempModifier> body, List<TempModifier> ambient, List<TempModifier> base, List<TempModifier> rate)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerModifiersSyncMessage(body, ambient, base, rate));
        }
    }
}
