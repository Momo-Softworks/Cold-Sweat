package com.momosoftworks.coldsweat.api.util;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.common.TempModifierEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.common.event.capability.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.TempModifiersSyncMessage;
import com.momosoftworks.coldsweat.core.network.message.TemperatureSyncMessage;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.InterruptableStreamer;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
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
     * Converts a double temperature to a different unit. If {@code from} and {@code to} are the same, returns {@code value}.<br>
     * @param value The temperature to convert.
     * @param from The unit to convert from.
     * @param to The unit to convert to.
     * @param absolute Used when dealing with world temperatures with Minecraft units.
     * @return The converted temperature.
     */
    public static double convert(double value, Units from, Units to, boolean absolute)
    {
        switch (from)
        {
            case C : switch (to)
            {
                case C  : return value;
                case F  : return value * 1.8 + (absolute ? 32d : 0d);
                case MC : return value / 25d;
            }
            case F : switch (to)
            {
                case C  : return (value - (absolute ? 32d : 0d)) / 1.8;
                case F  : return value;
                case MC : return (value - (absolute ? 32d : 0d)) / 45d;
            }
            case MC : switch (to)
            {
                case C  : return value * 25d;
                case F  : return value * 45d + (absolute ? 32d : 0d);
                case MC : return value;
            }
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
    {   EntityTempManager.getTemperatureCap(entity).orElse(new PlayerTempCap()).setTrait(trait, value);
    }

    public static void add(LivingEntity entity, Trait trait, double value)
    {   EntityTempManager.getTemperatureCap(entity).ifPresent(cap -> cap.setTrait(trait, cap.getTrait(trait) + value));
    }

    /**
     * @return  a double representing what the Temperature would be after a TempModifier is applied.
     * @param entity the entity this modifier should use
     * @param modifiers the modifier(s) being applied to the {@code Temperature}
     */
    public static double apply(double currentTemp, LivingEntity entity, Trait trait, TempModifier... modifiers)
    {
        double temp2 = currentTemp;
        for (TempModifier modifier : modifiers)
        {
            if (modifier == null) continue;

            double newTemp = entity.tickCount % modifier.getTickRate() == 0 || modifier.getTicksExisted() == 0
                    ? modifier.update(temp2, entity, trait)
                    : modifier.getResult(temp2);
            if (!Double.isNaN(newTemp))
            {   temp2 = newTemp;
            }
        }
        return temp2;
    }

    /**
     * @return a double representing what the temperature would be after a collection of TempModifier(s) are applied.
     * @param entity the entity this list of modifiers should use
     * @param modifiers the list of modifiers being applied to the player's temperature
     */
    public static double apply(double temp, LivingEntity entity, Trait trait, Collection<TempModifier> modifiers)
    {   return apply(temp, entity, trait, modifiers.toArray(new TempModifier[0]));
    }

    static Map<ResourceLocation, DummyPlayer> DUMMIES = new HashMap<>();

    public static double getTemperatureAt(BlockPos pos, World level)
    {
        ResourceLocation dimension = level.dimension().location();
        // There is one "dummy" entity per world, which TempModifiers are applied to
        DummyPlayer dummy = DUMMIES.get(dimension);
        // If the dummy for this dimension is invalid, make a new one
        if (dummy == null || dummy.level != level)
        {   DUMMIES.put(dimension, dummy = new DummyPlayer(level));
            // Use default player modifiers to determine the temperature
            GatherDefaultTempModifiersEvent event = new GatherDefaultTempModifiersEvent(dummy, Trait.WORLD);
            MinecraftForge.EVENT_BUS.post(event);
            addModifiers(dummy, event.getModifiers(), Trait.WORLD, true);
        }
        // Move the dummy to the position being tested
        Vector3d centerPos = CSMath.getCenterPos(pos);
        dummy.setPos(centerPos.x, centerPos.y, centerPos.z);
        return apply(0, dummy, Trait.WORLD, getModifiers(dummy, Trait.WORLD));
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param trait The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(LivingEntity entity, Trait trait, Class<? extends TempModifier> modClass)
    {
        return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.hasModifier(trait, modClass)).orElse(false);
    }

    /**
     * @return The first modifier of the given class that is applied to the player.
     */
    public static <T extends TempModifier> Optional<T> getModifier(LivingEntity entity, Trait trait, Class<T> modClass)
    {
        return getModifier(EntityTempManager.getTemperatureCap(entity).orElse(new PlayerTempCap()), trait, modClass);
    }

    public static <T extends TempModifier> Optional<T> getModifier(ITemperatureCap cap, Trait trait, Class<T> modClass)
    {
        return (Optional<T>) cap.getModifiers(trait).stream().filter(modClass::isInstance).findFirst();
    }

    /**
     * @return The first modifier applied to the player that fits the predicate.
     */
    @Nullable
    public static TempModifier getModifier(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {
        for (TempModifier modifier : EntityTempManager.getTemperatureCap(entity).orElse(new PlayerTempCap()).getModifiers(trait))
        {
            if (condition.test(modifier))
            {   return modifier;
            }
        }
        return null;
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * Otherwise, it will add the modifier.<br>
     * @param player The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param trait The type of temperature to apply the modifier to
     */
    public static void addOrReplaceModifier(PlayerEntity player, TempModifier modifier, Trait trait)
    {
        addModifier(player, modifier, trait, false, Addition.of(Addition.Mode.REPLACE_OR_ADD, Addition.Order.FIRST, mod -> mod.equals(modifier)));
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * It will not add the modifier if an existing instance of the same TempModifier class is not found.<br>
     * @param player The player to apply the modifier to
     * @param modifier The modifier to apply
     * @param trait The type of temperature to apply the modifier to
     */
    public static void replaceModifier(PlayerEntity player, TempModifier modifier, Trait trait)
    {
        addModifier(player, modifier, trait, false, Addition.of(Addition.Mode.REPLACE, Addition.Order.FIRST, mod -> mod.equals(modifier)));
    }

    /**
     * Adds the given modifier to the player.<br>
     * If duplicates are disabled and the modifier already exists, this action will fail.
     * @param allowDupes allows or disallows duplicate TempModifiers to be applied
     * (You might use this for things that have stacking effects, for example)
     */
    public static void addModifier(LivingEntity entity, TempModifier modifier, Trait trait, boolean allowDupes)
    {
        addModifier(entity, modifier, trait, allowDupes, Addition.AFTER_LAST);
    }

    public static void addModifier(LivingEntity entity, TempModifier modifier, Trait trait, boolean allowDupes, Addition params)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, entity, trait);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled())
        {
            TempModifier newMod = event.getModifier();
            EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
            {
                List<TempModifier> modifiers = cap.getModifiers(event.trait);
                boolean changed = false;
                try
                {
                    Predicate<TempModifier> predicate = params.predicate;
                    if (predicate == null) predicate = mod -> true;

                    boolean replace = params.mode  == Addition.Mode.REPLACE || params.mode == Addition.Mode.REPLACE_OR_ADD;
                    boolean after   = params.mode  == Addition.Mode.AFTER;
                    boolean forward = params.order == Addition.Order.FIRST;

                    if (!allowDupes && !replace
                    && modifiers.stream().anyMatch(mod -> mod.equals(modifier)))
                    {   return;
                    }

                    // Get the start of the iterator & which direction it's going
                    int start = forward ? 0 : (modifiers.size() - 1);
                    // Iterate through the list (backwards if "forward" is false)
                    for (int i = start; forward ? i < modifiers.size() : i >= 0; i += forward ? 1 : -1)
                    {
                        TempModifier mod = modifiers.get(i);
                        // If the predicate is true, inject the modifier at this position (or after it if "after" is true)
                        if (predicate.test(mod))
                        {
                            if (replace)
                            {   modifiers.set(i, newMod);
                            }
                            else
                            {   modifiers.add(i + (after ? 1 : 0), newMod);
                            }
                            changed = true;
                            return;
                        }
                    }
                    // Add the modifier if the insertion check fails
                    if (params.mode != Addition.Mode.REPLACE)
                    {   modifiers.add(newMod);
                        changed = true;
                    }
                }
                finally
                {   if (changed) updateModifiers(entity, cap);
                }
            });
        }
    }

    public static void addModifiers(LivingEntity entity, List<TempModifier> modifiers, Trait trait, boolean duplicates)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            for (TempModifier modifier : modifiers)
            {   addModifier(entity, modifier, trait, duplicates);
            }
            updateModifiers(entity, cap);
        });
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param entity The entity being sampled
     * @param trait Determines which TempModifier list to pull from
     * @param count The number of modifiers of the given type to be removed (can be higher than the number of modifiers on the player)
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(LivingEntity entity, Trait trait, int count, Predicate<TempModifier> condition)
    {
        AtomicInteger removed = new AtomicInteger(0);

        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            cap.getModifiers(trait).removeIf(modifier ->
            {
                if (removed.get() < count)
                {
                    TempModifierEvent.Remove event = new TempModifierEvent.Remove(entity, trait, count, condition);
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

    public static void removeModifiers(LivingEntity entity, Trait trait, Predicate<TempModifier> condition)
    {
        removeModifiers(entity, trait, Integer.MAX_VALUE, condition);
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param entity is the entity being sampled
     * @param trait determines which TempModifier list to pull from
     * @return a NEW list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(LivingEntity entity, Trait trait)
    {
        return EntityTempManager.getTemperatureCap(entity).map(cap -> cap.getModifiers(trait)).orElse(Arrays.asList());
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
            if (cap.getModifiers(trait) != null)
            {
                cap.getModifiers(trait).forEach(action);
            }
        });
    }

    public static void forEachModifier(LivingEntity entity, Trait trait, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
    {
        EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
        {
            if (cap.getModifiers(trait) != null)
            {
                CSMath.breakableForEach(cap.getModifiers(trait), action);
            }
        });
    }

    public static void updateTemperature(LivingEntity entity, ITemperatureCap cap, boolean instant)
    {
        if (!entity.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(entity instanceof ServerPlayerEntity
                            ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity)
                            : PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
            new TemperatureSyncMessage(entity, cap.serializeTraits(), instant));
        }
    }

    public static void updateModifiers(LivingEntity entity, ITemperatureCap cap)
    {
        if (!entity.level.isClientSide)
        {
            ColdSweatPacketHandler.INSTANCE.send(entity instanceof ServerPlayerEntity
                            ? PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity)
                            : PacketDistributor.TRACKING_ENTITY.with(() -> entity),
            new TempModifiersSyncMessage(entity, cap.serializeModifiers()));
        }
    }

    public static Map<Trait, Double> getTemperatures(LivingEntity entity)
    {   return EntityTempManager.getTemperatureCap(entity).map(ITemperatureCap::getTraits).orElse(new EnumMap<>(Trait.class));
    }

    /**
     * Defines all temperature stats in Cold Sweat. <br>
     * These are used to get temperature stored on the player and/or to apply modifiers to it. <br>
     * <br>
     * {@link #WORLD}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * <br>
     * {@link #CORE}: The core temperature of the player (This is what "body" temperature typically refers to). <br>
     * {@link #BASE}: A static offset applied to the player's core temperature. <br>
     * {@link #BODY}: The sum of the player's core and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     * {@link #FREEZING_POINT}: An offset to the max temperature threshold, after which a player's body temperature starts rising. <br>
     * {@link #BURNING_POINT}: An offset to the min temperature threshold, after which a player's body temperature starts falling. <br>
     * {@link #COLD_RESISTANCE}: Resistance to cold temperature-related damage. <br>
     * {@link #HEAT_RESISTANCE}: Resistance to heat temperature-related damage. <br>
     * {@link #COLD_DAMPENING}: Changes the rate of body temperature increase. <br>
     * {@link #HEAT_DAMPENING}: Changes the rate of body temperature decrease. <br>
     */
    public enum Trait implements StringRepresentable
    {
        WORLD("world"),
        CORE("core"),
        BASE("base"),
        BODY("body"),
        RATE("rate"),

        FREEZING_POINT("freezing_point"),
        BURNING_POINT("burning_point"),
        COLD_RESISTANCE("cold_resistance"),
        HEAT_RESISTANCE("heat_resistance"),
        COLD_DAMPENING("cold_dampening"),
        HEAT_DAMPENING("heat_dampening");

        public static final Codec<Trait> CODEC = StringRepresentable.fromEnum(Trait::values);

        private final String id;

        Trait(String id)
        {   this.id = id;
        }

        public static Trait fromID(String id)
        {
            for (Trait trait : values())
            {
                if (trait.getSerializedName().equals(id))
                    return trait;
            }
            return null;
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

        public static final Codec<Units> CODEC = StringRepresentable.fromEnum(Units::values);

        private final String name;
        private final String id;

        Units(String name, String id)
        {   this.name = name;
            this.id = id;
        }

        public static Units fromID(String id)
        {
            for (Units unit : values())
            {
                if (unit.getSerializedName().equals(id))
                    return unit;
            }
            return null;
        }

        public String getFormattedName()
        {   return name;
        }

        @Override
        public String getSerializedName()
        {   return id;
        }
    }

    public static class Addition
    {
        private final Mode mode;
        private final Order order;
        private final Predicate<TempModifier> predicate;

        public static final Addition AFTER_LAST = Addition.of(Mode.AFTER, Order.LAST, mod -> true);
        public static final Addition BEFORE_FIRST = Addition.of(Mode.BEFORE, Order.FIRST, mod -> true);

        public Addition(Mode mode, Order order, Predicate<TempModifier> predicate)
        {   this.mode = mode;
            this.order = order;
            this.predicate = predicate;
        }

        public static Addition of(Mode mode, Order order, Predicate<TempModifier> predicate)
        {   return new Addition(mode, order, predicate);
        }

        public Mode getRelation()
        {   return mode;
        }

        public Predicate<TempModifier> getPredicate()
        {   return predicate;
        }

        public Order getOrder()
        {   return order;
        }

        public enum Mode
        {
            // Inserts the new modifier before the targeted modifier's position
            BEFORE,
            // Inserts the new modifier after the targeted modifier's position
            AFTER,
            // Replace the desired instance of the modifier (fails if no modifiers pass the predicate)
            REPLACE,
            // Replace the desired instance of the modifier if it exists, otherwise add it to the end
            REPLACE_OR_ADD
        }

        public enum Order
        {
            // Targets the first modifier that passes the predicate
            FIRST,
            // Targets the last modifier that passes the predicate
            LAST
        }
    }
}
