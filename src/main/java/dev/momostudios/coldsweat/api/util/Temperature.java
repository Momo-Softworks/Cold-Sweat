package dev.momostudios.coldsweat.api.util;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.TempModifierEvent;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import dev.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.math.InterruptableStreamer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * General helper class for temperature-related actions. (Previously TempHelper)<br>
 * Allows for manipulating player temperature and TempModifiers.
 */
public class Temperature
{
    /**
     * Returns the player's temperature of the specified type.
     */
    public static double get(Player player, Type type)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCap()).getTemp(type);
    }

    public static void set(Player player, double value, Type type)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCap()).setTemp(type, value);
    }

    public static void add(Player player, double value, Type type)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            cap.setTemp(type, value + cap.getTemp(type));
        });
    }

    /**
     * @return  a double representing what the Temperature would be after a TempModifier is applied.
     * @param player the player this modifier should use
     * @param modifiers the modifier(s) being applied to the {@code Temperature}
     */
    public static double apply(double temp, @Nonnull Player player, @Nonnull TempModifier... modifiers)
    {
        double temp2 = temp;
        for (TempModifier modifier : modifiers)
        {
            if (modifier == null) continue;

            temp2 = player.tickCount % modifier.getTickRate() == 0 || modifier.getTicksExisted() == 0
                    ? modifier.update(temp2, player)
                    : modifier.getResult(temp2);
        }
        return temp2;
    }

    /**
     * @return a double representing what the temperature would be after a collection of TempModifier(s) are applied.
     * @param player the player this list of modifiers should use
     * @param modifiers the list of modifiers being applied to the player's temperature
     */
    public static double apply(double temp, @Nonnull Player player, @Nonnull Collection<TempModifier> modifiers)
    {
        return apply(temp, player, modifiers.toArray(new TempModifier[0]));
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(Player player, Type type, Class<? extends TempModifier> modClass)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * @return The first modifier of the given class that is applied to the player.
     */
    @Nullable
    public static <T extends TempModifier> T getModifier(Player player, Type type, Class<T> modClass)
    {
        return getModifier(player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCap()), type, modClass);
    }

    @Nullable
    public static <T extends TempModifier> T getModifier(ITemperatureCap cap, Type type, Class<T> modClass)
    {
        for (TempModifier modifier : cap.getModifiers(type))
        {
            if (modClass.isInstance(modifier))
            {
                return (T) modifier;
            }
        }
        return null;
    }

    /**
     * @return The first modifier applied to the player that fits the predicate.
     */
    @Nullable
    public static TempModifier getModifier(Player player, Type type, Predicate<TempModifier> condition)
    {
        for (TempModifier modifier : player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCap()).getModifiers(type))
        {
            if (condition.test(modifier))
            {
                return modifier;
            }
        }
        return null;
    }

    /**
     * Adds the given modifier to the player.<br>
     * If duplicates are disabled and the modifier already exists, this action will fail.
     * @param allowDuplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(Player player, TempModifier modifier, Type type, boolean allowDuplicates)
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
    public static void replaceModifier(Player player, TempModifier modifier, Type type)
    {
        addModifier(player, modifier, type, 1, true);
    }

    public static void addModifier(Player player, TempModifier modifier, Type type, int maxCount, boolean replace)
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
                    int matchingCount = matchingMods.size();

                    // If there are more modifiers than allowed
                    if (matchingCount >= event.maxCount)
                    {
                        // If replacing, delete extra modifiers
                        if (replace)
                        {
                            modifiers.removeAll(matchingMods.stream().limit(matchingMods.size() - (event.maxCount - 1)).toList());
                            matchingCount = 0;
                        }
                        // Otherwise the modifier can't be added
                        else return;
                    }

                    // Add the modifier and update
                    if (matchingCount < event.maxCount)
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
    public static void removeModifiers(Player player, Type type, int count, Predicate<TempModifier> condition)
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

    public static void removeModifiers(Player player, Type type, Predicate<TempModifier> condition)
    {
        removeModifiers(player, type, Integer.MAX_VALUE, condition);
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param player is the player being sampled
     * @param type determines which TempModifier list to pull from
     * @return a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(Player player, Type type)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.getModifiers(type)).orElse(null);
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(Player player, Class<? extends TempModifier> modClass, Type type)
    {
        return player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param type determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(Player player, Type type, Consumer<TempModifier> action)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                cap.getModifiers(type).forEach(action);
            }
        });
    }

    public static void forEachModifier(Player player, Type type, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
    {
        player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                CSMath.breakableForEach(cap.getModifiers(type), action);
            }
        });
    }

    public static void updateTemperature(Player player, ITemperatureCap cap, boolean instant)
    {
        if (!player.level.isClientSide && cap instanceof PlayerTempCap playerCap)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
            new PlayerTempSyncMessage(playerCap.serializeTemps(), instant));
        }
    }

    public static void updateModifiers(Player player, ITemperatureCap cap)
    {
        if (!player.level.isClientSide && cap instanceof PlayerTempCap playerCap)
        {
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
            new PlayerModifiersSyncMessage(playerCap.serializeModifiers()));
        }
    }

    /**
     * Defines all temperature stats in Cold Sweat. <br>
     * These are used to get temperature stored on the player and/or to apply modifiers to it. <br>
     * <br>
     * {@link #WORLD}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * {@link #MAX}: An offset to the max temperature threshold, after which a player's body temperature starts rising. <br>
     * {@link #MIN}: An offset to the min temperature threshold, after which a player's body temperature starts falling. <br>
     * <br>
     * {@link #CORE}: The core temperature of the player (This is what "body" temperature typically refers to). <br>
     * {@link #BASE}: A static offset applied to the player's core temperature. <br>
     * {@link #BODY}: The sum of the player's core and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     */
    public enum Type
    {
        WORLD,
        MAX,
        MIN,
        CORE,
        BASE,
        BODY,
        RATE
    }

    /**
     * Units of measurement used by Cold Sweat.<br>
     * Most calculations are done in MC units, then converted to C or F when they are displayed.<br>
     */
    public enum Units
    {
        F,
        C,
        MC
    }
}
