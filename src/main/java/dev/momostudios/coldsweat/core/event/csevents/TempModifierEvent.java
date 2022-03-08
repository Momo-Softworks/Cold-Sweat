package dev.momostudios.coldsweat.core.event.csevents;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.world.BlockEffectEntries;
import dev.momostudios.coldsweat.common.world.TempModifierEntries;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;

import java.util.function.Predicate;

/**
 * These events are fired when dealing with {@link TempModifier}s. <br>
 * They should not be side-specific. Do not limit them to run on any one side as it will cause desyncs.
 */
public class TempModifierEvent extends Event
{
    /**
     * Fired when a {@link TempModifier} is about to be added. <br>
     * <br>
     * {@link #maxCount} determines whether the TempModifier may be added if an instance already exists. <br>
     * {@link #player} is the player the TempModifier is being applied to. <br>
     * {@link #type} determines the modifier's {@link Temperature.Types}. It will never be {@link Temperature.Types#TOTAL} <br>
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
     * Fired when a {@link TempModifier} is about to be removed. <br>
     * <br>
     * {@link #player} is the player the TempModifier is being removed from. <br>
     * {@link #type} is the modifier's {@link Temperature.Types}. It will never be {@link Temperature.Types#TOTAL}. <br>
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
    public static class Tick extends TempModifierEvent
    {
        /**
         * Fired at the beginning of {@code calculate()}, before the {@code getValue()} method is called. <br>
         * <br>
         * {@link #player} - The player the TempModifier is attached to. <br>
         * {@link #modifier} - The TempModifier running the method. <br>
         * {@link #temperature} - The Temperature being passed into the {@code getValue()} method. <br>
         * <br>
         * This event is {@link Cancelable}. <br>
         * Cancelling this event results in the Temperature passing through without calling {@code calculate()}. <br>
         */
        @Cancelable
        public static class Pre extends Tick
        {
            public final Player player;
            private TempModifier modifier;
            private dev.momostudios.coldsweat.common.temperature.Temperature temperature;

            public Pre(TempModifier modifier, Player player, dev.momostudios.coldsweat.common.temperature.Temperature temperature)
            {
                this.player = player;
                this.modifier = modifier;
                this.temperature = temperature;
            }

            public TempModifier getModifier() {
                return modifier;
            }
            public dev.momostudios.coldsweat.common.temperature.Temperature getTemperature() {
                return temperature;
            }
        }

        /**
         * Fired after the {@code getValue()} method is run, but before the value is returned <br>
         * <br>
         * {@link #player} is the player the TempModifier is attached to. <br>
         * {@link #modifier} is the TempModifier running the method. <br>
         * {@link #temperature} is the Temperature after the {@code getValue())} method has been called. <br>
         * <br>
         * This event is not {@link Cancelable}. <br>
         */
        public static class Post extends Tick
        {
            public final Player player;
            private TempModifier modifier;
            private dev.momostudios.coldsweat.common.temperature.Temperature temperature;

            public Post(TempModifier modifier, Player player, dev.momostudios.coldsweat.common.temperature.Temperature temperature)
            {
                this.player = player;
                this.modifier = modifier;
                this.temperature = temperature;
            }

            public TempModifier getModifier() {
                return modifier;
            }
            public dev.momostudios.coldsweat.common.temperature.Temperature getTemperature() {
                return temperature;
            }
        }
    }


    /**
     * Fired when the {@link TempModifier} or {@link BlockEffect} registry is being built. <br>
     * The event is fired during {@link net.minecraftforge.event.world.WorldEvent.Load}. <br>
     * <br>
     * {@link Modifier} refers to registries being added to {@link TempModifierEntries}. <br>
     * {@link Block} refers to registries being added to {@link BlockEffectEntries}. <br>
     * Use {@code getPool().flush()} if calling manually to prevent duplicates. <br>
     * <br>
     * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    public static class Init extends TempModifierEvent
    {
        public static class Modifier extends TempModifierEvent
        {
            /**
             * @return the map of registered {@link TempModifier}s.
             */
            public final TempModifierEntries getPool() {
                return TempModifierEntries.getEntries();
            }

            /**
             * Adds a new {@link TempModifier} to the registry.
             * @param modifier the {@link TempModifier} to add.
             * @throws InstantiationException If the TempModifier has no default constructor.
             * @throws IllegalAccessException If the default constructor is not accessible.
             */
            public void addModifier(TempModifier modifier) throws InstantiationException, IllegalAccessException
            {
                this.getPool().add(modifier);
            }
        }

        public static class Block extends TempModifierEvent
        {
            /**
             * @return the map of registered {@link BlockEffect}s.
             */
            public final BlockEffectEntries getPool() {
                return BlockEffectEntries.getEntries();
            }

            /**
             * Adds a new {@link BlockEffect} to the registry.
             * @param blockEffect The BlockEffect to add.
             * @throws InstantiationException If the BlockEffect doesn't have a default constructor.
             * @throws IllegalAccessException If the default constructor is not accessible.
             */
            public void addBlockEffect(BlockEffect blockEffect) throws InstantiationException, IllegalAccessException
            {
                this.getPool().add(blockEffect);
            }
        }
    }
}
