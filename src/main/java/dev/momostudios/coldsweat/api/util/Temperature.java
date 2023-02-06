package dev.momostudios.coldsweat.api.util;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.TempModifierEvent;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.TempModifiersSyncMessage;
import dev.momostudios.coldsweat.core.network.message.TemperatureSyncMessage;
import dev.momostudios.coldsweat.util.entity.EntityHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.math.InterruptableStreamer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
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
    private Temperature() {}

    /**
     * Returns the player's temperature of the specified type.
     */
    public static double get(LivingEntity entity, Type type)
    {
        return getTemperatureCap(entity).map(cap -> cap.getTemp(type)).orElse(0.0);
    }

    public static void set(LivingEntity entity, double value, Type type)
    {
        getTemperatureCap(entity).orElse(new PlayerTempCap()).setTemp(type, value);
    }

    public static void add(LivingEntity entity, double value, Type type)
    {
        getTemperatureCap(entity).ifPresent(cap ->
        {
            cap.setTemp(type, value + cap.getTemp(type));
        });
    }

    /**
     * @return  a double representing what the Temperature would be after a TempModifier is applied.
     * @param entity the entity this modifier should use
     * @param modifiers the modifier(s) being applied to the {@code Temperature}
     */
    public static double apply(double temp, @Nonnull LivingEntity entity, @Nonnull TempModifier... modifiers)
    {
        double temp2 = temp;
        for (TempModifier modifier : modifiers)
        {
            if (modifier == null) continue;

            temp2 = entity.tickCount % modifier.getTickRate() == 0 || modifier.getTicksExisted() == 0
                    ? modifier.update(temp2, entity)
                    : modifier.getResult(temp2);
        }
        return temp2;
    }

    /**
     * @return a double representing what the temperature would be after a collection of TempModifier(s) are applied.
     * @param entity the entity this list of modifiers should use
     * @param modifiers the list of modifiers being applied to the player's temperature
     */
    public static double apply(double temp, @Nonnull LivingEntity entity, @Nonnull Collection<TempModifier> modifiers)
    {
        return apply(temp, entity, modifiers.toArray(new TempModifier[0]));
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(LivingEntity entity, Type type, Class<? extends TempModifier> modClass)
    {
        return getTemperatureCap(entity).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * @return The first modifier of the given class that is applied to the player.
     */
    @Nullable
    public static <T extends TempModifier> T getModifier(LivingEntity entity, Type type, Class<T> modClass)
    {
        return getModifier(getTemperatureCap(entity).orElse(new PlayerTempCap()), type, modClass);
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
    public static TempModifier getModifier(LivingEntity entity, Type type, Predicate<TempModifier> condition)
    {
        for (TempModifier modifier : getTemperatureCap(entity).orElse(new PlayerTempCap()).getModifiers(type))
        {
            if (condition.test(modifier))
            {
                return modifier;
            }
        }
        return null;
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

    /**
     * Adds the given modifier to the player.<br>
     * If duplicates are disabled and the modifier already exists, this action will fail.
     * @param allowDuplicates allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(LivingEntity entity, TempModifier modifier, Type type, boolean allowDuplicates)
    {
        addModifier(entity, modifier, type, allowDuplicates ? Integer.MAX_VALUE : 1, false);
    }

    public static void addModifier(LivingEntity entity, TempModifier modifier, Type type, int maxCount, boolean replace)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, entity, type, maxCount);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            TempModifier newModifier = event.getModifier();
            if (TempModifierRegistry.getEntries().containsKey(newModifier.getID()))
            {
                getTemperatureCap(entity).ifPresent(cap ->
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
                        updateModifiers(entity, cap);
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
     * @param entity The entity being sampled
     * @param type Determines which TempModifier list to pull from
     * @param count The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(LivingEntity entity, Type type, int count, Predicate<TempModifier> condition)
    {
        AtomicInteger removed = new AtomicInteger(0);

        getTemperatureCap(entity).ifPresent(cap ->
        {
            cap.getModifiers(type).removeIf(modifier ->
            {
                if (removed.get() < count)
                {
                    TempModifierEvent.Remove event = new TempModifierEvent.Remove(entity, type, count, condition);
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
                updateModifiers(entity, cap);
        });
    }

    public static void removeModifiers(LivingEntity entity, Type type, Predicate<TempModifier> condition)
    {
        removeModifiers(entity, type, Integer.MAX_VALUE, condition);
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param entity is the entity being sampled
     * @param type determines which TempModifier list to pull from
     * @return a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(LivingEntity entity, Type type)
    {
        return getTemperatureCap(entity).map(cap -> cap.getModifiers(type)).orElse(List.of());
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(LivingEntity entity, Class<? extends TempModifier> modClass, Type type)
    {
        return getTemperatureCap(entity).map(cap -> cap.hasModifier(type, modClass)).orElse(false);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param type determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(LivingEntity entity, Type type, Consumer<TempModifier> action)
    {
        getTemperatureCap(entity).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                cap.getModifiers(type).forEach(action);
            }
        });
    }

    public static void forEachModifier(LivingEntity entity, Type type, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
    {
        getTemperatureCap(entity).ifPresent(cap ->
        {
            if (cap.getModifiers(type) != null)
            {
                CSMath.breakableForEach(cap.getModifiers(type), action);
            }
        });
    }

    public static LazyOptional<ITemperatureCap> getTemperatureCap(LivingEntity entity)
    {
        return entity.getCapability(EntityHelper.getTemperatureCap(entity));
    }

    public static void updateTemperature(LivingEntity entity, ITemperatureCap cap, boolean instant)
    {
        if (!entity.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(entity instanceof Player player
                            ? PacketDistributor.PLAYER.with(() -> (ServerPlayer) player)
                            : PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
            new TemperatureSyncMessage(entity, cap.serializeTemps(), instant));
        }
    }

    public static void updateModifiers(LivingEntity entity, ITemperatureCap cap)
    {
        if (!entity.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(entity instanceof Player player
                            ? PacketDistributor.PLAYER.with(() -> (ServerPlayer) player)
                            : PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
            new TempModifiersSyncMessage(entity, cap.serializeModifiers()));
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
