package net.momostudios.coldsweat.core.event.csevents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.util.PlayerHelper;

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
     * {@link #duplicatesAllowed} determines whether the TempModifier may be added if an instance already exists. <br>
     * {@link #player} is the player the TempModifier is being applied to. <br>
     * {@link #type} determines the modifier's {@link PlayerHelper.Types}. It will never be {@link PlayerHelper.Types#COMPOSITE} <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being added.<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Add extends TempModifierEvent
    {
        private PlayerEntity player;
        private TempModifier modifier;
        public boolean duplicatesAllowed;
        public PlayerHelper.Types type;

        public void setDuplicatesAllowed(boolean allowDuplicates) {
            this.duplicatesAllowed = allowDuplicates;
        }

        public void setModifierType(PlayerHelper.Types newType) {
            this.type = newType;
        }

        public final TempModifier getModifier() {
            return modifier;
        }

        public void setModifier(TempModifier modifier) {
            this.modifier = modifier;
        }

        public final PlayerEntity getPlayer() {
            return player;
        }

        public Add(TempModifier modifier, PlayerEntity player, PlayerHelper.Types type, boolean duplicates)
        {
            duplicatesAllowed = duplicates;
            this.player = player;
            this.type = type;
            this.modifier = modifier;
        }
    }


    /**
     * Fired when a {@link TempModifier} is about to be removed. <br>
     * <br>
     * {@link #player} is the player the TempModifier is being removed from. <br>
     * {@link #type} is the modifier's {@link PlayerHelper.Types}. It will never be {@link PlayerHelper.Types#COMPOSITE}. <br>
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
        public final PlayerEntity player;
        public final PlayerHelper.Types type;
        int count;
        Predicate<TempModifier> condition;

        public Remove(PlayerEntity player, PlayerHelper.Types type, int count, Predicate<TempModifier> condition)
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
            public final PlayerEntity player;
            private TempModifier modifier;
            private Temperature temperature;

            public Pre(TempModifier modifier, PlayerEntity player, Temperature temperature)
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
            public final PlayerEntity player;
            private TempModifier modifier;
            private Temperature temperature;

            public Post(TempModifier modifier, PlayerEntity player, Temperature temperature)
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
