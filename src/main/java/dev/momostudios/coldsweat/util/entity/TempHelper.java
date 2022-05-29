package dev.momostudios.coldsweat.util.entity;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.TempModifierEvent;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCapability;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import dev.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.math.InterruptableStreamer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TempHelper
{
    /**
     * Returns the player's temperature of the specified type.
     */
    public static Temperature getTemperature(Player player, Temperature.Types type)
    {
        return new Temperature(player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCapability()).get(type));
    }

    /**
     * Use {@link TempModifier}s for over-time effects.
     */
    public static void setTemperature(Player player, Temperature value, Temperature.Types type)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(capability ->
        {
            capability.set(type, value.get());
        });
    }

    public static void addTemperature(Player player, Temperature value, Temperature.Types type)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(capability ->
        {
            capability.set(type, value.get() + capability.get(type));
        });
    }

    /**
     * Adds the given modifier to the player.<br>
     * If duplicates are disabled and the modifier already exists, this action will fail.
     * @param duplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(Player player, TempModifier modifier, Temperature.Types type, boolean duplicates)
    {
        addModifier(player, modifier, type, duplicates ? Integer.MAX_VALUE : 1, false);
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * Otherwise, it will add the modifier.<br>
     * @param player The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param type The type of temperature to apply the modifier to
     */
    public static void insertModifier(Player player, TempModifier modifier, Temperature.Types type)
    {
        addModifier(player, modifier, type, 1, true);
    }

    public static void addModifier(Player player, TempModifier modifier, Temperature.Types type, int maxCount, boolean replace)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, player, type, maxCount);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
            {
                TempModifier newModifier = event.getModifier();
                if (TempModifierRegistry.getEntries().containsKey(newModifier.getID()))
                {
                    List<TempModifier> modifiers = cap.getModifiers(type);
                    AtomicInteger duplicateCount = new AtomicInteger();

                    // If we're replacing, remove the old one first
                    if (replace)
                    {
                        // Test if there are more modifiers than maxCount allows
                        long modCount = modifiers.stream().filter(mod -> mod.getID().equals(newModifier.getID())).count();
                        int iterations = (int) modCount - maxCount;

                        // If there are more modifiers than maxCount allows, remove the excess
                        if (iterations >= 1)
                        {
                            cap.getModifiers(event.type).removeIf(mod ->
                            {
                                if (mod.getID().equals(newModifier.getID()))
                                {
                                    return duplicateCount.getAndIncrement() < iterations;
                                }
                                return false;
                            });
                        }
                    }
                    // If we're not replacing, test if there is room (# of modifiers of this type < maxCount)
                    else
                    {
                        for (TempModifier mod : cap.getModifiers(event.type))
                        {
                            if (mod.getID().equals(event.getModifier().getID()))
                            {
                                if (duplicateCount.getAndIncrement() >= event.maxCount)
                                {
                                    // Fail to add the modifier if there are already too many
                                    break;
                                }
                            }
                        }
                    }

                    // Add the modifier and update
                    if (duplicateCount.get() < event.maxCount)
                    {
                        cap.getModifiers(event.type).add(event.getModifier());
                        updateModifiers(player, cap);
                    }
                }
                else
                {
                    ColdSweat.LOGGER.error("Tried to reference invalid TempModifier with ID \"" + modifier.getID() + "\"! Is it not registered?");
                }
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
    public static void removeModifiers(Player player, Temperature.Types type, int count, Predicate<TempModifier> condition)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
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

            // Update modifiers if anything actually changed
            if (removed.get() > 0)
                updateModifiers(player, cap);
        });
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @return a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(Player player, Temperature.Types type)
    {
        List<TempModifier> mods =  player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCapability()).getModifiers(type);
        mods.removeIf(mod -> mod == null || mod.getID() == null ||mod.getID().isEmpty());
        return new ArrayList<>(mods);
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(Player player, Class<? extends TempModifier> modClass, Temperature.Types type)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param type determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(Player player, Temperature.Types type, Consumer<TempModifier> action)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                cap.getModifiers(type).forEach(action);
            }
        });
    }

    public static void forEachModifier(Player player, Temperature.Types type, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
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
    public static String getModifierTag(Temperature.Types type)
    {
        switch (type)
        {
            case CORE :      return "coreTempModifiers";
            case WORLD :     return "worldTempModifiers";
            case BASE :      return "baseTempModifiers";
            case RATE :      return "rateTempModifiers";
            case MAX:        return "hottestTempModifiers";
            case MIN:        return "coldestTempModifiers";
            default : throw new IllegalArgumentException("PlayerTempHandler.getModifierTag() received illegal Type argument");
        }
    }

    /**
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of Temperature to be stored. ({@link Temperature.Types#WORLD} should only be stored when needed to prevent lag)
     * @return The NBT tag name for the given type
     */
    public static String getTempTag(Temperature.Types type)
    {
        switch (type)
        {
            case CORE :      return "coreTemperature";
            case WORLD :     return "worldTemperature";
            case BASE :      return "baseTemperature";
            case BODY :      return "bodyTemperature";
            case MAX:        return "hottestTemperature";
            case MIN:        return "coldestTemperature";
            default : throw new IllegalArgumentException("PlayerTempHandler.getTempTag() received illegal Type argument");
        }
    }

    public static void updateTemperature(Player player, Temperature bodyTemp, Temperature baseTemp, Temperature worldTemp, Temperature max, Temperature
        min)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerTempSyncMessage(bodyTemp.get(), baseTemp.get(), worldTemp.get(), max.get(), min.get()));
        }
    }

    public static void updateModifiers(Player player, List<TempModifier> body, List<TempModifier> world, List<TempModifier> base, List<TempModifier> rate, List<TempModifier> hottest, List<TempModifier> coldest)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerModifiersSyncMessage(body, world, base, rate, hottest, coldest));
        }
    }

    public static void updateModifiers(Player player, ITemperatureCap cap)
    {
        if (!player.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerModifiersSyncMessage(
                            cap.getModifiers(Temperature.Types.CORE),
                            cap.getModifiers(Temperature.Types.WORLD),
                            cap.getModifiers(Temperature.Types.BASE),
                            cap.getModifiers(Temperature.Types.RATE),
                            cap.getModifiers(Temperature.Types.MAX),
                            cap.getModifiers(Temperature.Types.MIN)));
        }
    }
}
