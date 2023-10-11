
package com.momosoftworks.coldsweat.api.event.common;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Predicate;

/**
 * These events are fired when dealing with {@link TempModifier}s. <br>
 * They should not be side-specific. Do not limit them to run on any one side as it will cause desyncs.
 */
public class TempModifierEvent extends Event
{
    protected final Entity entity;
    protected TempModifier modifier;
    protected Temperature.Type type;

    private TempModifierEvent(Entity entity, TempModifier modifier, Temperature.Type type)
    {   this.entity = entity;
        this.modifier = modifier;
        this.type = type;
    }

    public final Entity getEntity()
    {   return entity;
    }

    public TempModifier getModifier()
    {   return modifier;
    }

    public Temperature.Type getType()
    {   return type;
    }

    /**
     * Fired when a {@link TempModifier} is about to be added to an entity. <br>
     * <br>
     * {@link #entity} is the player the TempModifier is being applied to. <br>
     * {@link #type} determines the modifier's {@link Temperature.Type}. It will never be {@link Temperature.Type#BODY} <br>
     * <br>
     * This event is {@link Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being added.<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Add extends TempModifierEvent
    {
        public void setModifierType(Temperature.Type newType)
        {   this.type = newType;
        }

        public Add(TempModifier modifier, Entity entity, Temperature.Type type)
        {   super(entity, modifier, type);
        }
    }

    /**
     * Fired when a {@link TempModifier} is about to be removed from an entity. <br>
     * <br>
     * {@link #entity} is the player the TempModifier is being removed from. <br>
     * {@link #type} is the modifier's {@link Temperature.Type}. It will never be {@link Temperature.Type#BODY}. <br>
     * {@link #count} is the number of TempModifiers of the specified class being removed. <br>
     * {@link #condition} is the predicate used to determine which TempModifiers are being removed. <br>
     * <br>
     * This event is {@link Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being removed. <br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Remove extends TempModifierEvent
    {
        int count;
        Predicate<TempModifier> condition;

        public Remove(Entity entity, TempModifier modifier, Temperature.Type type, int count, Predicate<TempModifier> condition)
        {   super(entity, modifier, type);
            this.count = count;
            this.condition = condition;
        }
        public void setCount(int count) {
            this.count = count;
        }

        public void setCondition(Predicate<TempModifier> condition) {
            this.condition = condition;
        }

        public int getCount() {
            return count;
        }

        public Predicate<TempModifier> getCondition() {
            return condition;
        }
    }


    /**
     * Fired when a TempModifier runs the {@code calculate()} method. <br>
     * {@code Pre} and {@code Post} are fired on the {@link MinecraftForge#EVENT_BUS} before/after the calculation respectively. <br>
     */
    public static class Calculate extends TempModifierEvent
    {
        public double temperature;

        private Calculate(Entity entity, TempModifier modifier, Temperature.Type type, double temperature)
        {   super(entity, modifier, type);
            this.modifier = modifier;
            this.temperature = temperature;
        }

        public double getTemperature() {
            return temperature;
        }

        /**
         * Fired at the beginning of {@code calculate()}, before the {@code getValue()} method is called. <br>
         * <br>
         * {@link #entity} - The player the TempModifier is attached to. <br>
         * {@link #modifier} - The TempModifier running the method. <br>
         * {@link #temperature} - The Temperature being passed into the {@code getValue()} method. <br>
         * <br>
         * This event is {@link Cancelable}. <br>
         * Cancelling this event results in the modifier not being processed, remaining unchanged. <br>
         */
        @Cancelable
        public static class Pre extends Calculate
        {
            public Pre(Entity entity, TempModifier modifier, Temperature.Type type, double temperature)
            {   super(entity, modifier, type, temperature);
            }
        }

        /**
         * Fired by {@code calculate()} after the {@code getResult()} method is run, but before the value is returned <br>
         * <br>
         * {@link #entity} is the player the TempModifier is attached to. <br>
         * {@link #modifier} is the TempModifier running the method. <br>
         * {@link #temperature} is the Temperature after the {@code getValue())} method has been called. <br>
         * <br>
         * This event is NOT {@link Cancelable}. <br>
         */
        public static class Post extends Calculate
        {
            public Post(Entity entity, TempModifier modifier, Temperature.Type type, double temperature)
            {   super(entity, modifier, type, temperature);
            }
        }
    }
}
