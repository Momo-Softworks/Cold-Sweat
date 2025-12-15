package com.momosoftworks.coldsweat.api.util;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.api.annotation.Internal;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TemperatureChangedEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.network.message.SyncTempModifiersMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncTemperatureMessage;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.InterruptibleIterator;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
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
     * Converts a double temperature to a different unit. If {@code from} and {@code to} are the same, returns {@code value}.<br>
     * @param value The temperature to convert.
     * @param from The unit to convert from.
     * @param to The unit to convert to.
     * @param absolute Used when dealing with absolute temperature.
     * @return The converted temperature.
     */
    public static double convert(double value, Units from, Units to, boolean absolute)
    {
        return switch (from)
        {
            case C -> switch (to)
            {
                case C -> value;
                case F -> value * 1.8 + (absolute ? 32d : 0d);
                case MC -> value / 25d;
            };
            case F -> switch (to)
            {
                case C -> (value - (absolute ? 32d : 0d)) / 1.8;
                case F -> value;
                case MC -> (value - (absolute ? 32d : 0d)) / 45d;
            };
            case MC -> switch (to)
            {
                case C -> value * 25d;
                case F -> value * 45d + (absolute ? 32d : 0d);
                case MC -> value;
            };
        };
    }

    public static double convertIfNeeded(double value, Trait trait, Units units)
    {
        if (trait.isForWorld())
        {   return convert(value, Units.MC, units, true);
        }
        return value;
    }

    /**
     * Returns the player's temperature of the specified type.
     */
    public static double get(LivingEntity entity, Trait trait)
    {   return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.getTrait(trait)).orElse(0.0);
    }

    public static void set(LivingEntity entity, Trait trait, double value)
    {
        TemperatureChangedEvent event = NeoForge.EVENT_BUS.post(new TemperatureChangedEvent(entity, trait, get(entity, trait), value));
        if (event.isCanceled())
        {   return;
        }
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> cap.setTrait(trait, event.getTemperature()));
        updateTemperature(entity);
    }

    public static void add(LivingEntity entity, Trait trait, double value)
    {
        double oldTemp = get(entity, trait);
        TemperatureChangedEvent event = NeoForge.EVENT_BUS.post(new TemperatureChangedEvent(entity, trait, oldTemp, oldTemp + value));
        if (event.isCanceled())
        {   return;
        }
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> cap.setTrait(trait, event.getTemperature()));
        updateTemperature(entity);
    }

    /**
     * @return  a double representing what the Temperature would be after a TempModifier is applied.
     * @param entity The entity this modifier should use
     * @param ignoreTickMultiplier Ignores the "modifier tick rate" setting and uses the normal tick rate
     * @param modifiers The modifier(s) being applied to the {@code Temperature}
     */
    public static double apply(double currentTemp, LivingEntity entity, Trait trait, boolean ignoreTickMultiplier, TempModifier... modifiers)
    {
        if (modifiers.length == 0) return currentTemp;

        double temp2 = currentTemp;
        for (TempModifier modifier : modifiers)
        {
            if (modifier == null) continue;

            int tickRate = ignoreTickMultiplier
                           ? modifier.getTickRate()
                           : (int) (modifier.getTickRate() / ConfigSettings.MODIFIER_TICK_RATE.get());

            double newTemp = entity.tickCount % tickRate == 0 || modifier.getTicksExisted() == 0 || entity.tickCount <= 1
                    ? modifier.update(temp2, entity, trait)
                    : modifier.apply(trait, temp2);
            if (!Double.isNaN(newTemp))
            {   temp2 = newTemp;
            }
        }
        return temp2;
    }
    public static double apply(double currentTemp, LivingEntity entity, Trait trait, TempModifier... modifiers)
    {   return apply(currentTemp, entity, trait, false, modifiers);
    }

    /**
     * @return a double representing what the temperature would be after a collection of TempModifier(s) are applied.
     * @param entity the entity this list of modifiers should use
     * @param modifiers the list of modifiers being applied to the player's temperature
     * @param ignoreTickMultiplier Ignores the "modifier tick rate" setting and uses the normal tick rate
     */
    public static double apply(double temp, LivingEntity entity, Trait trait, Collection<TempModifier> modifiers, boolean ignoreTickMultiplier)
    {   return apply(temp, entity, trait, ignoreTickMultiplier, modifiers.toArray(new TempModifier[0]));
    }
    public static double apply(double temp, LivingEntity entity, Trait trait, Collection<TempModifier> modifiers)
    {   return apply(temp, entity, trait, false, modifiers.toArray(new TempModifier[0]));
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param trait The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(LivingEntity entity, Trait trait, Class<? extends TempModifier> modClass)
    {   return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.hasModifier(trait, modClass)).orElse(false);
    }

    /**
     * Gets all TempModifiers of the specified type on the entity.<br>
     * @param entity is the entity being sampled
     * @param trait determines which TempModifier list to pull from
     * @return An <b>IMMUTABLE</b> list of all TempModifiers for the specified trait
     */
    public static List<TempModifier> getModifiers(LivingEntity entity, Trait trait)
    {   return EntityTempManager.getTemperatureCap(entity).map(cap -> ImmutableList.copyOf(cap.getModifiers(trait))).orElse(ImmutableList.of());
    }

    /**
     * Gets all TempModifiers of the specified type on the entity that match the given condition.<br>
     * @param entity is the entity being sampled
     * @param trait determines which TempModifier list to pull from
     * @param condition The predicate to filter the TempModifiers
     * @return An IMMUTABLE list of all TempModifiers for the specified trait that match the condition
     */
    public static List<TempModifier> getModifiers(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {   return getModifiers(entity, trait).stream().filter(condition).toList();
    }

    /**
     * @return The first modifier on the entity that matches the given condition
     */
    public static Optional<TempModifier> getModifier(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {   return getModifiers(entity, trait, condition).stream().findFirst();
    }

    /**
     * @return The first modifier on the entity that is an instance of the given class
     */
    public static <T extends TempModifier> Optional<T> getModifier(LivingEntity entity, Trait trait, Class<T> modClass)
    {   return (Optional<T>) getModifier(entity, trait, modClass::isInstance);
    }

    /**
     * Replaces an existing modifier if it exists on the entity; otherwise, adds the modifier to the entity.
     * @param entity The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param trait The type of temperature to apply the modifier to
     * @param duplicateMatcher The matcher to use to find duplicates
     */
    public static boolean replaceOrAddModifier(LivingEntity entity, TempModifier modifier, Trait trait, Matcher duplicateMatcher)
    {
        Placement placement = Placement.of(Mode.REPLACE, Order.FIRST, mod -> duplicateMatcher.check(modifier, mod)).orElse(Placement.LAST);
        return addModifier(entity, modifier, trait, placement);
    }

    /**
     * Adds the given modifier to the entity, with a custom placement.
     * @param entity The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param trait The type of temperature to apply the modifier to
     * @param placement The placement settings for the modifier
     */
    public static boolean addModifier(LivingEntity entity, TempModifier modifier, Trait trait, Placement placement)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(entity, trait, modifier);
        NeoForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            Optional<ITemperatureCap> optCap = EntityTempManager.getTemperatureCap(entity);
            if (optCap.isPresent())
            {
                ITemperatureCap cap = optCap.get();
                List<TempModifier> modifiers = cap.getModifiers(trait);
                Consumer<TempModifier> onAdded = mod ->
                {   modifier.onAdded(entity, trait);
                    updateSiblingsAdd(modifiers, entity, trait, modifier);
                };
                Consumer<TempModifier> onRemoved = mod ->
                {   modifier.onRemoved(entity, trait);
                    updateSiblingsRemove(modifiers, entity, trait, modifier);
                };
                if (addModifier(modifiers, event.getModifier(), placement, onAdded, onRemoved))
                {   updateModifiers(entity, cap);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * This method is mainly for internal use. {@link Temperature#addModifier(LivingEntity, TempModifier, Trait, Placement)} should be used instead.<br>
     * <br>
     * Be warned that it does call update methods or events, including:<br>
     * - {@link TempModifierEvent.Add}<br>
     * - {@link TempModifier#onAdded(LivingEntity, Trait)}<br>
     * - {@link TempModifier#onSiblingAdded(LivingEntity, Trait, TempModifier)}<br>
     */
    @Internal
    public static boolean addModifier(List<TempModifier> modifiers, TempModifier modifier, Placement placement,
                                      Consumer<TempModifier> onAdded, Consumer<TempModifier> onRemoved)
    {
        boolean added = false;
        Predicate<TempModifier> predicate = placement.predicate();
        if (predicate == null) predicate = mod -> true;

        boolean isForward = placement.order() == Order.FIRST;
        Matcher duplicateMatcher = placement.duplicates();
        int maxDuplicates = placement.maxDuplicates();

        tryAdd:
        {
            if (duplicateMatcher != Matcher.IGNORE
            && modifiers.stream().filter(mod -> duplicateMatcher.check(modifier, mod)).count() >= maxDuplicates)
            {   break tryAdd;
            }

            if (modifiers.isEmpty())
            {
                if (placement.mode().isAdding())
                {
                    modifiers.add(modifier);
                    if (onAdded != null) onAdded.accept(modifier);
                    return true;
                }
                else break tryAdd;
            }
            // Get the start of the iterator & which direction it's going
            int start = isForward ? 0 : (modifiers.size() - 1);
            // Iterate through the list (backwards if "forward" is false)
            for (int i = start; isForward ? i < modifiers.size() : i >= 0; i += isForward ? 1 : -1)
            {
                TempModifier modifierAt = modifiers.get(i);
                // If the predicate is true, inject the modifier at this position (or after it if "after" is true)
                if (predicate.test(modifierAt))
                {
                    added = true;
                    if (placement.mode() == Mode.REPLACE)
                    {   modifiers.set(i, modifier);
                        if (onRemoved != null) onRemoved.accept(modifierAt);
                    }
                    else
                    {   modifiers.add(i + (placement.mode() == Mode.ADD_AFTER ? 1 : 0), modifier);
                    }
                    if (onAdded != null) onAdded.accept(modifier);
                    break tryAdd;
                }
            }
        }
        // Use fallback if modifier was not added
        if (!added && placement.fallback() != null)
        {   added = addModifier(modifiers, modifier, placement.fallback(), onAdded, onRemoved);
        }
        return added;
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param entity The entity being sampled
     * @param trait Determines which TempModifier list to pull from
     * @param maxCount The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(LivingEntity entity, Trait trait, int maxCount, Order order, Predicate<TempModifier> condition)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            List<TempModifier> modifiers = cap.getModifiers(trait);
            boolean forwardOrder = order == Order.FIRST;
            int removed = 0;

            for (int i = forwardOrder ? 0 : modifiers.size() - 1; i >= 0 && i < modifiers.size(); i += forwardOrder ? 1 : -1)
            {
                if (removed < maxCount)
                {
                    TempModifier modifier = modifiers.get(i);
                    if (condition.test(modifier))
                    {
                        TempModifierEvent.Remove event = new TempModifierEvent.Remove(entity, trait, modifier);
                        NeoForge.EVENT_BUS.post(event);
                        if (!event.isCanceled())
                        {
                            cap.removeModifier(modifier, trait);
                            modifier.onRemoved(entity, trait);
                            updateSiblingsRemove(modifiers, entity, trait, modifier);
                            i += forwardOrder ? -1 : 1;
                            removed++;
                        }
                    }
                }
                else break;
            }

            // Update modifiers if anything actually changed
            if (removed > 0)
            {   updateModifiers(entity, cap);
            }
        });
    }

    public static void removeModifiers(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {   removeModifiers(entity, trait, Integer.MAX_VALUE, Order.FIRST, condition);
    }

    public static void removeModifiers(LivingEntity entity, Trait trait, Class<? extends TempModifier> clazz)
    {   removeModifiers(entity, trait, Integer.MAX_VALUE, Order.FIRST, clazz::isInstance);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     * @param trait determines which TempModifier list to pull from
     * @param action the action(s) to perform on each TempModifier
     */
    public static void forEachModifier(LivingEntity entity, Trait trait, Consumer<TempModifier> action)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            cap.getModifiers(trait).forEach(action);
        });
    }

    public static void forEachModifier(LivingEntity entity, Trait trait, BiConsumer<TempModifier, InterruptibleIterator<TempModifier>> action)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            CSMath.breakableForEach(cap.getModifiers(trait), action);
        });
    }

    public static double getNeutralWorldTemp(LivingEntity entity)
    {   return (get(entity, Trait.BURNING_POINT) + get(entity, Trait.FREEZING_POINT)) / 2;
    }

    @Internal
    public static void updateSiblingsAdd(List<TempModifier> modifiers, LivingEntity entity, Trait trait, TempModifier modifier)
    {
        modifiers.forEach(mod ->
        {
            if (mod == modifier) return;
            mod.onSiblingAdded(entity, trait, modifier);
        });
    }

    @Internal
    public static void updateSiblingsRemove(List<TempModifier> modifiers, LivingEntity entity, Trait trait, TempModifier modifier)
    {
        modifiers.forEach(mod ->
        {
            if (mod == modifier) return;
            mod.onSiblingRemoved(entity, trait, modifier);
        });
    }

    public static void updateTemperature(LivingEntity entity, ITemperatureCap cap, boolean instant)
    {
        if (!entity.level().isClientSide)
        {   PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SyncTemperatureMessage(entity, cap.serializeTraits(), instant));
        }
    }
    public static void updateTemperature(LivingEntity entity)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {   cap.syncValues(entity);
        });
    }

    public static void updateModifiers(LivingEntity entity, ITemperatureCap cap)
    {
        if (!entity.level().isClientSide)
        {   PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SyncTempModifiersMessage(entity, cap.serializeModifiers()));
        }
    }
    public static void updateModifiers(LivingEntity entity)
    {   EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> updateModifiers(entity, cap));
    }

    public static Map<Trait, Double> getTemperatures(LivingEntity entity)
    {   return EntityTempManager.getTemperatureCap(entity).map(ITemperatureCap::getTraits).orElse(new EnumMap<>(Trait.class));
    }

    public static EnumMap<Trait, List<TempModifier>> getModifiers(LivingEntity entity)
    {   return EntityTempManager.getTemperatureCap(entity).map(ITemperatureCap::getModifiers).orElseGet(() -> new EnumMap<>(Trait.class));
    }

    public static void clearModifiers(LivingEntity entity, Trait trait)
    {   EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> cap.clearModifiers(trait));
    }

    /**
     * Defines all temperature stats in Cold Sweat. <br>
     * These are used to get temperature stored on the player and/or to apply modifiers to it. <br>
     * <br>
     * {@link #WORLD}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * {@link #CORE}: The core temperature of the player (This is what "body" temperature typically refers to). <br>
     * {@link #BASE}: A static offset applied to the player's core temperature. <br>
     * {@link #BODY}: The sum of the player's core and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     * <br>
     * {@link #FREEZING_POINT}: The minimum temperature threshold, below which an entity starts freezing. <br>
     * {@link #BURNING_POINT}: The maximum temperature threshold, above which an entity starts overheating. <br>
     * {@link #COLD_RESISTANCE}: Resistance to cold temperature-related damage. <br>
     * {@link #HEAT_RESISTANCE}: Resistance to heat temperature-related damage. <br>
     * {@link #COLD_DAMPENING}: Changes the rate of body temperature increase. <br>
     * {@link #HEAT_DAMPENING}: Changes the rate of body temperature decrease. <br>
     */
    public enum Trait implements StringRepresentable
    {
        WORLD("world", true, true, true),
        CORE("core", true, true, false),
        BASE("base", true, true, true),
        BODY("body", false, false, false),
        RATE("rate", true, true, true),

        FREEZING_POINT("freezing_point", true, true, true),
        BURNING_POINT("burning_point", true, true, true),
        COLD_RESISTANCE("cold_resistance", true, true, true),
        HEAT_RESISTANCE("heat_resistance", true, true, true),
        COLD_DAMPENING("cold_dampening", true, true, true),
        HEAT_DAMPENING("heat_dampening", true, true, true);

        public static final Codec<Trait> CODEC = ExtraCodecs.enumIgnoreCase(values());

        private final String id;
        private final boolean forTemperature;
        private final boolean forModifiers;
        private final boolean forAttributes;

        Trait(String id, boolean forTemperature, boolean forModifiers, boolean forAttributes)
        {
            this.id = id;
            this.forTemperature = forTemperature;
            this.forModifiers = forModifiers;
            this.forAttributes = forAttributes;
        }

        public boolean isForTemperature()
        {   return forTemperature;
        }

        public boolean isForModifiers()
        {   return forModifiers;
        }

        public boolean isForAttributes()
        {   return forAttributes;
        }

        public boolean isForWorld()
        {   return this == WORLD || this == BURNING_POINT || this == FREEZING_POINT;
        }

        public static Trait fromID(String name)
        {   return EnumHelper.byName(values(), name);
        }

        @Override
        public String getSerializedName()
        {   return id;
        }
    }

    /**
     * Units of measurement used by Cold Sweat.<br>
     * Most calculations are done in MC units, then converted to C or F when they are displayed.<br>
     */
    public enum Units implements StringRepresentable
    {
        F("°F", "f"),
        C("°C", "c"),
        MC("MC", "mc");

        public static final Codec<Units> CODEC = ExtraCodecs.enumIgnoreCase(values());

        private final String name;
        private final String id;

        Units(String name, String id)
        {   this.name = name;
            this.id = id;
        }

        public static Units fromID(String name)
        {   return EnumHelper.byName(values(), name);
        }

        public String getFormattedName()
        {   return name;
        }

        @Override
        public String getSerializedName()
        {   return id;
        }
    }
}
