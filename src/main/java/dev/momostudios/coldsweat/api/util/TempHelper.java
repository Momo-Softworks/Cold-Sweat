package dev.momostudios.coldsweat.api.util;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.TempModifierEvent;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import dev.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.math.InterruptableStreamer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TempHelper
{
    /**
     * Returns the player's temperature of the specified type.
     */
    public static Temperature getTemperature(Player player, Temperature.Type type)
    {
        return new Temperature(player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.get(type)).orElse(0.0));
    }

    public static void setTemperature(Player player, Temperature value, Temperature.Type type)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            cap.set(type, value.get());
        });
    }

    public static void addTemperature(Player player, Temperature value, Temperature.Type type)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            cap.set(type, value.add(cap.get(type)).get());
        });
    }

    /**
     * @return The first modifier of the given class that is applied to the player.
     */
    public static TempModifier getModifier(Player player, Temperature.Type type, Class<? extends TempModifier> modClass)
    {
        AtomicReference<TempModifier> mod = new AtomicReference<>();
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            mod.set(cap.getModifiers(type).stream().filter(modClass::isInstance).findFirst().orElse(null));
        });
        return mod.get();
    }

    /**
     * Adds the given modifier to the player.<br>
     * If duplicates are disabled and the modifier already exists, this action will fail.
     * @param allowDuplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(Player player, TempModifier modifier, Temperature.Type type, boolean allowDuplicates)
    {
        addModifier(player, modifier, type, allowDuplicates ? Integer.MAX_VALUE : 1, false);
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * Otherwise, it will add the modifier.<br>
     * @param player The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param type The type of temperature to apply the modifier to
     */
    public static void replaceModifier(Player player, TempModifier modifier, Temperature.Type type)
    {
        addModifier(player, modifier, type, 1, true);
    }

    public static void addModifier(Player player, TempModifier modifier, Temperature.Type type, int maxCount, boolean replace)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, player, type, maxCount);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            TempModifier newModifier = event.getModifier();
            if (TempModifierRegistry.getEntries().containsKey(newModifier.getID()))
            {
                player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                {
                    List<TempModifier> modifiers = cap.getModifiers(event.type);

                    // Find all the modifiers of this type
                    List<TempModifier> matchingMods = modifiers.stream().filter(mod -> mod.getID().equals(newModifier.getID())).toList();

                    // If we're replacing, remove the old one if needed
                    if (replace && matchingMods.size() >= event.maxCount)
                    {
                        modifiers.removeAll(matchingMods.stream().limit(matchingMods.size() - event.maxCount).toList());
                    }

                    // Add the modifier and update
                    if (replace || matchingMods.size() < event.maxCount)
                    {
                        modifiers.add(event.getModifier());
                        updateModifiers(player, cap);
                    }
                });
            }
            else
            {
                ColdSweat.LOGGER.error("Tried to reference invalid TempModifier with ID \"" + modifier.getID() + "\"! Is it not registered?");
            }
        }
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param player The player being sampled
     * @param type Determines which TempModifier list to pull from
     * @param count The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(Player player, Temperature.Type type, int count, Predicate<TempModifier> condition)
    {
        AtomicInteger removed = new AtomicInteger(0);

        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
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
                    return false;
                }
                return false;
            });

            // Update modifiers if anything actually changed
            if (removed.get() > 0)
                updateModifiers(player, cap);
        });
    }

    public static void removeModifiers(Player player, Temperature.Type type, Predicate<TempModifier> condition)
    {
        removeModifiers(player, type, Integer.MAX_VALUE, condition);
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @return a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(Player player, Temperature.Type type)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.getModifiers(type)).orElse(null);
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(Player player, Class<? extends TempModifier> modClass, Temperature.Type type)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param type determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(Player player, Temperature.Type type, Consumer<TempModifier> action)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                cap.getModifiers(type).forEach(action);
            }
        });
    }

    public static void forEachModifier(Player player, Temperature.Type type, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                CSMath.breakableForEach(cap.getModifiers(type), action);
            }
        });
    }

    /**
     * Used for storing TempModifiers in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of TempModifier to be stored
     * @return The NBT tag name for the given type
     */
    public static String getModifierTag(Temperature.Type type)
    {
        return switch (type)
                {
                    case CORE -> "coreTempModifiers";
                    case WORLD -> "worldTempModifiers";
                    case BASE -> "baseTempModifiers";
                    case RATE -> "rateTempModifiers";
                    case MAX -> "hottestTempModifiers";
                    case MIN -> "coldestTempModifiers";
                    default -> throw new IllegalArgumentException("PlayerTempHandler.getModifierTag(): \"" + type + "\" is not a valid type!");
                };
    }

    /**
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of Temperature to be stored. ({@link Temperature.Type#WORLD} should only be stored when needed to prevent lag)
     * @return The NBT tag name for the given type
     */
    public static String getTempTag(Temperature.Type type)
    {
        return switch (type)
                {
                    case CORE -> "coreTemp";
                    case WORLD -> "worldTemp";
                    case BASE -> "baseTemp";
                    case MAX -> "maxTWorldTemp";
                    case MIN -> "minWorldTemp";
                    default -> throw new IllegalArgumentException("PlayerTempHandler.getTempTag(): \"" + type + "\" is not a valid type!");
                };
    }

    public static void updateTemperature(Player player, ITemperatureCap cap, boolean instant)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
            new PlayerTempSyncMessage(cap.get(Temperature.Type.WORLD),
                cap.get(Temperature.Type.CORE),
                cap.get(Temperature.Type.BASE),
                cap.get(Temperature.Type.MAX),
                cap.get(Temperature.Type.MIN), instant));
        }
    }


    public static void updateModifiers(Player player, ITemperatureCap cap)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
            new PlayerModifiersSyncMessage(
                cap.getModifiers(Temperature.Type.WORLD),
                cap.getModifiers(Temperature.Type.CORE),
                cap.getModifiers(Temperature.Type.BASE),
                cap.getModifiers(Temperature.Type.RATE),
                cap.getModifiers(Temperature.Type.MAX),
                cap.getModifiers(Temperature.Type.MIN)));
        }
    }
}
