package com.momosoftworks.coldsweat.api.util;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.common.TemperatureChangedEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.BiomeTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.BlockTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.ElevationTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.common.event.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.SyncModifiersMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncTemperaturesMessage;
import com.momosoftworks.coldsweat.core.properties.IEntityTempProperty;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.InterruptableStreamer;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * @param absolute Used when dealing with world temperatures with Minecraft units.
     * @return The converted temperature.
     */
    public static double convertUnits(double value, Units from, Units to, boolean absolute)
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
    public static double get(EntityLivingBase entity, Type type)
    {   return EntityTempManager.getTemperatureProperty(entity).getTemp(type);
    }

    public static void set(EntityLivingBase entity, Type type, double value)
    {
        TemperatureChangedEvent event = new TemperatureChangedEvent(entity, type, get(entity, type), value);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        EntityTempManager.getTemperatureProperty(entity).setTemp(type, event.getTemperature());
    }

    public static void add(EntityLivingBase entity, double value, Type type)
    {
        double oldTemp = get(entity, type);
        TemperatureChangedEvent event = new TemperatureChangedEvent(entity, type, oldTemp, oldTemp + value);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        EntityTempManager.getTemperatureProperty(entity).setTemp(type, event.getTemperature());
    }

    /**
     * @return  a double representing what the Temperature would be after a TempModifier is applied.
     * @param entity the entity this modifier should use
     * @param ignoreTickMultiplier if true, recalculates at the modifier's raw tick rate, ignoring {@code MODIFIER_TICK_RATE}
     * @param modifiers the modifier(s) being applied to the {@code Temperature}
     */
    public static double apply(double temp, @Nonnull EntityLivingBase entity, Type type, boolean ignoreTickMultiplier, @Nonnull TempModifier... modifiers)
    {
        if (modifiers.length == 0) return temp;

        double temp2 = temp;
        for (TempModifier modifier : modifiers)
        {
            if (modifier == null) continue;

            int tickRate = ignoreTickMultiplier
                           ? modifier.getTickRate()
                           : (int) (modifier.getTickRate() / ConfigSettings.MODIFIER_TICK_RATE.get());

            double newTemp = entity.ticksExisted % Math.max(1, tickRate) == 0 || modifier.getTicksExisted() == 0 || entity.ticksExisted <= 1
                    ? modifier.update(temp2, entity, type)
                    : modifier.getResult(temp2);
            if (!Double.isNaN(newTemp))
            {   temp2 = newTemp;
            }
        }
        return temp2;
    }

    public static double apply(double temp, @Nonnull EntityLivingBase entity, Type type, @Nonnull TempModifier... modifiers)
    {
        return apply(temp, entity, type, false, modifiers);
    }

    /**
     * @return a double representing what the temperature would be after a collection of TempModifier(s) are applied.
     * @param entity the entity this list of modifiers should use
     * @param modifiers the list of modifiers being applied to the player's temperature
     */
    public static double apply(double temp, @Nonnull EntityLivingBase entity, Type type, @Nonnull Collection<TempModifier> modifiers)
    {
        return apply(temp, entity, type, false, modifiers.toArray(new TempModifier[0]));
    }

    public static double apply(double temp, @Nonnull EntityLivingBase entity, Type type, boolean ignoreTickMultiplier, @Nonnull Collection<TempModifier> modifiers)
    {
        return apply(temp, entity, type, ignoreTickMultiplier, modifiers.toArray(new TempModifier[0]));
    }

    static Map<World, EntitySilverfish> DUMMIES = new HashMap<>();
    public static double getTemperatureAt(BlockPos pos, World world)
    {
        EntityLivingBase dummy = DUMMIES.computeIfAbsent(world, dim -> new EntitySilverfish(world));
        Vec3 vec = CSMath.atCenterOf(pos);
        dummy.setPosition(vec.xCoord, vec.yCoord, vec.zCoord);
        return apply(0, dummy, Type.WORLD, true, ListBuilder.<TempModifier>begin(new BiomeTempModifier(9))
                                                            .add(new ElevationTempModifier(),
                                                                 new BlockTempModifier()).build());
    }

    /**
     * @param modClass The class of the TempModifier to check for
     * @param type The type of TempModifier to check for
     * @return true if the player has a TempModifier that extends the given class
     */
    public static boolean hasModifier(EntityLivingBase entity, Type type, Class<? extends TempModifier> modClass)
    {   return EntityTempManager.getTemperatureProperty(entity).hasModifier(type, modClass);
    }

    /**
     * @return The first modifier of the given class that is applied to the player.
     */
    public static <T extends TempModifier> Optional<T> getModifier(EntityLivingBase entity, Type type, Class<T> modClass)
    {   return getModifier(EntityTempManager.getTemperatureProperty(entity), type, modClass);
    }

    public static <T extends TempModifier> Optional<T> getModifier(IEntityTempProperty cap, Type type, Class<T> modClass)
    {   return (Optional<T>) cap.getModifiers(type).stream().filter(modClass::isInstance).findFirst();
    }

    /**
     * @return The first modifier applied to the player that fits the predicate.
     */
    @Nullable
    public static TempModifier getModifier(EntityLivingBase entity, Type type, Predicate<TempModifier> condition)
    {
        for (TempModifier modifier : EntityTempManager.getTemperatureProperty(entity).getModifiers(type))
        {
            if (condition.test(modifier))
            {   return modifier;
            }
        }
        return null;
    }

    /**
     * Replaces an existing modifier (matched via {@code duplicateMatcher}) if it exists on the entity;
     * otherwise, adds the modifier to the end of the list.
     */
    public static boolean replaceOrAddModifier(Entity entity, TempModifier modifier, Type type, Matcher duplicateMatcher)
    {
        Placement placement = Placement.of(Mode.REPLACE, Order.FIRST, mod -> duplicateMatcher.check(modifier, mod)).orElse(Placement.LAST);
        return addModifier(entity, modifier, type, placement);
    }

    /**
     * Invokes addModifier() in a way that replaces the first occurrence of the modifier, if it exists.<br>
     * Otherwise, it will add the modifier.
     */
    public static boolean addOrReplaceModifier(Entity entity, TempModifier modifier, Type type)
    {   return replaceOrAddModifier(entity, modifier, type, Matcher.SAME_CLASS);
    }

    /**
     * Replaces the first matching modifier; fails (does nothing) if no matching modifier is found.
     */
    public static boolean replaceModifier(Entity entity, TempModifier modifier, Type type)
    {   return addModifier(entity, modifier, type, Placement.of(Mode.REPLACE, Order.FIRST, mod -> modifier.getID().equals(mod.getID())));
    }

    /**
     * Adds the given modifier to the entity.<br>
     * @param allowDupes allows or disallows duplicate (same-class) TempModifiers to be applied.
     */
    public static boolean addModifier(Entity entity, TempModifier modifier, Type type, boolean allowDupes)
    {   return addModifier(entity, modifier, type, allowDupes ? Placement.LAST : Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
    }

    /**
     * Adds the given modifier to the entity, with a custom {@link Placement}.
     */
    public static boolean addModifier(Entity entity, TempModifier modifier, Type type, Placement placement)
    {
        TempModifierEvent.Add event = new TempModifierEvent.Add(modifier, entity, type);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return false;

        TempModifier newModifier = event.getModifier();
        if (!TempModifierRegistry.getEntries().containsKey(newModifier.getID()))
        {   ColdSweat.LOGGER.error("Tried to reference invalid TempModifier with ID \"" + newModifier.getID() + "\"! Is it not registered?");
            return false;
        }

        IEntityTempProperty prop = EntityTempManager.getTemperatureProperty(entity);
        List<TempModifier> modifiers = prop.getModifiers(event.getType());
        Type finalType = event.getType();

        Consumer<TempModifier> onAdded = mod ->
        {
            if (entity instanceof EntityLivingBase)
            {   newModifier.onAdded((EntityLivingBase) entity, finalType);
                updateSiblingsAdd(modifiers, (EntityLivingBase) entity, finalType, newModifier);
            }
        };
        Consumer<TempModifier> onRemoved = mod ->
        {
            if (entity instanceof EntityLivingBase)
            {   mod.onRemoved((EntityLivingBase) entity, finalType);
                updateSiblingsRemove(modifiers, (EntityLivingBase) entity, finalType, mod);
            }
        };

        if (addModifier(modifiers, newModifier, placement, onAdded, onRemoved))
        {   updateModifiers(entity, prop);
            return true;
        }
        return false;
    }

    /**
     * Internal: inserts {@code modifier} into {@code modifiers} according to {@code placement}.<br>
     * Calls the {@code onAdded}/{@code onRemoved} callbacks where appropriate. Ported from 1.16.
     */
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
                {   modifiers.add(modifier);
                    if (onAdded != null) onAdded.accept(modifier);
                    return true;
                }
                else break tryAdd;
            }
            // Get the start of the iterator & which direction it's going
            int start = isForward ? 0 : (modifiers.size() - 1);
            for (int i = start; isForward ? i < modifiers.size() : i >= 0; i += isForward ? 1 : -1)
            {
                TempModifier modifierAt = modifiers.get(i);
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

    public static void addModifiers(EntityLivingBase entity, List<TempModifier> modifiers, Type type, boolean duplicates)
    {
        for (TempModifier modifier : modifiers)
        {   addModifier(entity, modifier, type, duplicates);
        }
    }

    /**
     * Removes the specified number of TempModifiers of the specified type from the player
     * @param entity The entity being sampled
     * @param type Determines which TempModifier list to pull from
     * @param count The number of modifiers of the given type to be removed
     * @param condition The predicate to determine which TempModifiers to remove
     */
    public static void removeModifiers(EntityLivingBase entity, Type type, int count, Predicate<TempModifier> condition)
    {
        IEntityTempProperty prop = EntityTempManager.getTemperatureProperty(entity);
        List<TempModifier> modifiers = prop.getModifiers(type);
        int removed = 0;

        for (int i = 0; i < modifiers.size() && removed < count; )
        {
            TempModifier modifier = modifiers.get(i);
            TempModifierEvent.Remove event = new TempModifierEvent.Remove(entity, modifier, type, count, condition);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && event.getCondition().test(modifier))
            {
                modifiers.remove(i);
                modifier.onRemoved(entity, type);
                updateSiblingsRemove(modifiers, entity, type, modifier);
                removed++;
            }
            else i++;
        }

        // Update modifiers if anything actually changed
        if (removed > 0)
        {   updateModifiers(entity, prop);
        }
    }

    public static void removeModifiers(EntityLivingBase entity, Type type, Predicate<TempModifier> condition)
    {   removeModifiers(entity, type, Integer.MAX_VALUE, condition);
    }

    /**
     * Gets all TempModifiers of the specified type on the player
     * @param entity is the entity being sampled
     * @param type determines which TempModifier list to pull from
     * @return the (mutable) list of all TempModifiers of the specified type
     */
    public static List<TempModifier> getModifiers(EntityLivingBase entity, Type type)
    {   return EntityTempManager.getTemperatureProperty(entity).getModifiers(type);
    }

    /**
     * Iterates through all TempModifiers of the specified type on the player
     */
    public static void forEachModifier(EntityLivingBase entity, Type type, Consumer<TempModifier> action)
    {   EntityTempManager.getTemperatureProperty(entity).getModifiers(type).forEach(action);
    }

    public static void forEachModifier(EntityLivingBase entity, Type type, BiConsumer<TempModifier, InterruptableStreamer<TempModifier>> action)
    {   CSMath.breakableForEach(EntityTempManager.getTemperatureProperty(entity).getModifiers(type), action);
    }

    public static void clearModifiers(EntityLivingBase entity, Type type)
    {
        IEntityTempProperty prop = EntityTempManager.getTemperatureProperty(entity);
        prop.clearModifiers(type);
        updateModifiers(entity, prop);
    }

    /**
     * @return the neutral world temperature, halfway between the entity's effective freezing and burning points.
     */
    public static double getNeutralWorldTemp(EntityLivingBase entity)
    {
        double min = ConfigSettings.MIN_TEMP.get() + get(entity, Type.FREEZING_POINT);
        double max = ConfigSettings.MAX_TEMP.get() + get(entity, Type.BURNING_POINT);
        return (min + max) / 2;
    }

    public static void updateSiblingsAdd(List<TempModifier> modifiers, EntityLivingBase entity, Type type, TempModifier modifier)
    {
        modifiers.forEach(mod ->
        {   if (mod == modifier) return;
            mod.onSiblingAdded(entity, type, modifier);
        });
    }

    public static void updateSiblingsRemove(List<TempModifier> modifiers, EntityLivingBase entity, Type type, TempModifier modifier)
    {
        modifiers.forEach(mod ->
        {   if (mod == modifier) return;
            mod.onSiblingRemoved(entity, type, modifier);
        });
    }

    public static void updateTemperature(EntityLivingBase entity, IEntityTempProperty prop, boolean instant)
    {
        if (!entity.worldObj.isRemote)
        {
            if (entity instanceof EntityPlayerMP)
            {   ColdSweatPacketHandler.CHANNEL.sendTo(new SyncTemperaturesMessage(entity.getEntityId(), prop.serializeTemps(), instant), (EntityPlayerMP) entity);
            }
            else
            {
                ColdSweatPacketHandler.CHANNEL.sendToAllAround(new SyncTemperaturesMessage(entity.getEntityId(), prop.serializeTemps(), instant),
                        new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 64d));
            }
        }
    }

    public static void updateModifiers(Entity entity, IEntityTempProperty prop)
    {
        if (!entity.worldObj.isRemote)
        {
            if (entity instanceof EntityPlayerMP)
            {   ColdSweatPacketHandler.CHANNEL.sendTo(new SyncModifiersMessage(entity.getEntityId(), prop.serializeModifiers()), (EntityPlayerMP) entity);
            }
            else
            {
                ColdSweatPacketHandler.CHANNEL.sendToAllAround(new SyncModifiersMessage(entity.getEntityId(), prop.serializeModifiers()),
                        new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 64d));
            }
        }
    }

    public static Map<Type, Double> getTemperatures(Entity entity)
    {   return EntityTempManager.getTemperatureProperty(entity).getTemperatures();
    }

    /**
     * Defines all temperature stats in Cold Sweat. <br>
     * These are used to get temperature stored on the player and/or to apply modifiers to it. <br>
     * <br>
     * {@link #WORLD}: The temperature of the area around the player. Should ONLY be changed by TempModifiers. <br>
     * {@link #FREEZING_POINT}: An offset to the min temperature threshold, below which a player's body temperature falls. <br>
     * {@link #BURNING_POINT}: An offset to the max temperature threshold, above which a player's body temperature rises. <br>
     * {@link #CORE}: The core temperature of the player (This is what "body" temperature typically refers to). <br>
     * {@link #BASE}: A static offset applied to the player's core temperature. <br>
     * {@link #BODY}: The sum of the player's core and base temperatures. (CANNOT be set) <br>
     * {@link #RATE}: Only used by TempModifiers. Affects the rate at which the player's body temperature changes. <br>
     * {@link #COLD_RESISTANCE}: Resistance to cold temperature-related damage. <br>
     * {@link #HEAT_RESISTANCE}: Resistance to heat temperature-related damage. <br>
     * {@link #COLD_DAMPENING}: Changes the rate of body temperature decrease. <br>
     * {@link #HEAT_DAMPENING}: Changes the rate of body temperature increase. <br>
     * <br>
     * New attribute traits are appended after {@link #RATE} so the original ordinals are preserved.
     */
    public enum Type implements StringRepresentable
    {
        WORLD("world", true, true, false),
        FREEZING_POINT("freezing_point", true, true, true),
        BURNING_POINT("burning_point", true, true, true),
        CORE("core", true, true, false),
        BASE("base", true, true, true),
        BODY("body", false, false, false),
        RATE("rate", false, true, false),
        COLD_RESISTANCE("cold_resistance", true, true, true),
        HEAT_RESISTANCE("heat_resistance", true, true, true),
        COLD_DAMPENING("cold_dampening", true, true, true),
        HEAT_DAMPENING("heat_dampening", true, true, true);

        private final String id;
        private final boolean forTemperature;
        private final boolean forModifiers;
        private final boolean forAttributes;

        Type(String id, boolean forTemperature, boolean forModifiers, boolean forAttributes)
        {   this.id = id;
            this.forTemperature = forTemperature;
            this.forModifiers = forModifiers;
            this.forAttributes = forAttributes;
        }

        public String getID()
        {   return id;
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

        /**
         * @return true if this trait represents world (ambient) temperature
         */
        public boolean isForWorld()
        {   return this == WORLD || this == BURNING_POINT || this == FREEZING_POINT;
        }

        /**
         * @return true if this trait is interpreted as a percentage or multiplier
         */
        public boolean isProportional()
        {   return this == COLD_RESISTANCE || this == HEAT_RESISTANCE || this == COLD_DAMPENING || this == HEAT_DAMPENING || this == RATE;
        }

        public boolean isNegativeValueGood()
        {   return this == FREEZING_POINT;
        }

        @Override
        public String getSerializedName()
        {   return id;
        }

        public static Type fromID(String id)
        {   return EnumHelper.byName(values(), id);
        }
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
