package dev.momostudios.coldsweat.api.event.common;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

import java.util.function.Predicate;

/**
 * These events are fired when dealing with {@link TempModifier}s. <br>
 * They should not be side-specific. Do not limit them to run on any one side as it will cause desyncs.
 */
public class TempModifierEvent extends Event
{
    /**
     * Fired when a {@link TempModifier} is about to be added to an entity. <br>
     * <br>
     * {@link #maxCount} determines whether the TempModifier may be added if an instance already exists. <br>
     * {@link #player} is the player the TempModifier is being applied to. <br>
     * {@link #type} determines the modifier's {@link Temperature.Types}. It will never be {@link Temperature.Types#BODY} <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being added.<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Add extends TempModifierEvent
    {
        private Player player;
        private TempModifier modifier;
        public int maxCount;
        public Temperature.Types type;

        public void setMaxCount(int count) {
            this.maxCount = count;
        }

        public void setModifierType(Temperature.Types newType) {
            this.type = newType;
        }

        public final TempModifier getModifier() {
            return modifier;
        }

        public void setModifier(TempModifier modifier) {
            this.modifier = modifier;
        }

        public final Player getPlayer() {
            return player;
        }

        public Add(TempModifier modifier, Player player, Temperature.Types type, int duplicates)
        {
            maxCount = duplicates;
            this.player = player;
            this.type = type;
            this.modifier = modifier;
        }
    }


    /**
     * Fired when a {@link TempModifier} is about to be removed from an entity. <br>
     * <br>
     * {@link #player} is the player the TempModifier is being removed from. <br>
     * {@link #type} is the modifier's {@link Temperature.Types}. It will never be {@link Temperature.Types#BODY}. <br>
     * {@link #count} is the number of TempModifiers of the specified class being removed. <br>
     * {@link #condition} is the predicate used to determine which TempModifiers are being removed. <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being removed. <br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Remove extends TempModifierEvent
    {
        public final Player player;
        public final Temperature.Types type;
        int count;
        Predicate<TempModifier> condition;

        public Remove(Player player, Temperature.Types type, int count, Predicate<TempModifier> condition)
        {
            this.player = player;
            this.type = type;
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
        /**
         * Fired at the beginning of {@code calculate()}, before the {@code getValue()} method is called. <br>
         * <br>
         * {@link #player} - The player the TempModifier is attached to. <br>
         * {@link #modifier} - The TempModifier running the method. <br>
         * {@link #temperature} - The Temperature being passed into the {@code getValue()} method. <br>
         * <br>
         * This event is {@link Cancelable}. <br>
         * Cancelling this event results in {@code getValue()} not being called (the Temperature stays unchanged). <br>
         */
        @Cancelable
        public static class Pre extends Calculate
        {
            public final Player player;
            private final TempModifier modifier;
            private Temperature temperature;

            public Pre(TempModifier modifier, Player player, Temperature temperature)
            {
                this.player = player;
                this.modifier = modifier;
                this.temperature = temperature;
            }

            public TempModifier getModifier() {
                return modifier;
            }
            public Temperature getTemperature() {
                return temperature;
            }
        }

        /**
         * Fired by {@code calculate()} after the {@code getResult()} method is run, but before the value is returned <br>
         * <br>
         * {@link #player} is the player the TempModifier is attached to. <br>
         * {@link #modifier} is the TempModifier running the method. <br>
         * {@link #temperature} is the Temperature after the {@code getValue())} method has been called. <br>
         * <br>
         * This event is NOT {@link Cancelable}. <br>
         */
        public static class Post extends Calculate
        {
            public final Player player;
            private final TempModifier modifier;
            private Temperature temperature;

            public Post(TempModifier modifier, Player player, Temperature temperature)
            {
                this.player = player;
                this.modifier = modifier;
                this.temperature = temperature;
            }

            public TempModifier getModifier() {
                return modifier;
            }
            public Temperature getTemperature() {
                return temperature;
            }
        }
    }
}
