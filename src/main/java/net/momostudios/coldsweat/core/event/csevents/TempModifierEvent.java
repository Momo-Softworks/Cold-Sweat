package net.momostudios.coldsweat.core.event.csevents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.ITemperatureCapability;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.event.StorePlayerData;
import net.momostudios.coldsweat.core.util.NBTHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;

public class TempModifierEvent extends Event
{
    /**
     * Fired when a {@link TempModifier} is about to be added. <br>
     * <br>
     * {@link #duplicatesAllowed} determines whether the TempModifier may be added if an instance already exists. <br>
     * {@link #player} is the player the TempModifier is being applied to. <br>
     * {@link #type} determines the modifier's {@link PlayerTemp.Types}. It will never be {@link PlayerTemp.Types#COMPOSITE} <br>
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
        public PlayerTemp.Types type;

        public void setArgs(Object... args) {
            try {
                this.modifier = this.modifier.getClass().getConstructor(Object[].class).newInstance(args);
            } catch (Exception e) {}
        }

        public void setDuplicatesAllowed(boolean allowDuplicates) {
            this.duplicatesAllowed = allowDuplicates;
        }

        public void setModifierType(PlayerTemp.Types newType) {
            this.type = newType;
        }

        public final TempModifier getModifier() {
            return modifier;
        }

        public final PlayerEntity getPlayer() {
            return player;
        }

        public Add(TempModifier modifier, PlayerEntity player, PlayerTemp.Types type, boolean duplicates)
        {
            duplicatesAllowed = duplicates;
            this.player = player;
            this.type = type;
            this.modifier = modifier;

            if (!this.isCanceled())
            {
                StorePlayerData.syncData(player);
                player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
                {
                    if (TempModifierEntries.getEntries().getMap().containsKey(modifier.getID()))
                    {
                        if (cap.getModifiers(type) != null && !cap.getModifiers(type).contains(modifier) || this.duplicatesAllowed)
                        {
                            cap.addModifier(type, modifier);
                        }
                    }
                    else
                        System.err.println("TempModifierEvent.Add: No TempModifier with ID " + modifier.getID() + " found!");
                });
            }
        }
    }


    /**
     * Fired when a {@link TempModifier} is about to be removed. <br>
     * <br>
     * {@link #player} is the player the TempModifier is being removed from. <br>
     * {@link #type} is the modifier's {@link PlayerTemp.Types}. It will never be {@link PlayerTemp.Types#COMPOSITE}. <br>
     * {@link #modifierClass} is the class of the TempModifier being removed. <br>
     * {@link #count} is the number of TempModifiers of the specified class being removed. <br>
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
        public final Class<? extends TempModifier> modifierClass;
        public final PlayerTemp.Types type;
        public int count;

        public Remove(PlayerEntity player, Class<? extends TempModifier> modClass, PlayerTemp.Types type, int count)
        {
            this.player = player;
            this.modifierClass = modClass;
            this.type = type;
            this.count = count;

            if (!this.isCanceled())
            {
                StorePlayerData.syncData(player);
                player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
                {
                    if (cap.getModifiers(type) != null && !cap.getModifiers(type).isEmpty())
                    {
                        this.count = Math.min(this.count, cap.getModifiers(type).size());
                        for (int i = 0; i < this.count; i++)
                        {
                            cap.getModifiers(type).removeIf(modifier -> modifier.getClass() == this.modifierClass);
                        }
                    }
                });
            }
        }
    }


    /**
     * Fired when a {@link TempModifier} registry is being built. <br>
     * The event is fired during {@link net.minecraftforge.event.world.WorldEvent.Load}. <br>
     * <br>
     * {@link Modifier} refers to registries being added to {@link TempModifierEntries} <br>
     * {@link Block} refers to registries being added to {@link BlockEffectEntries} <br>
     * <br>
     * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}.
     */
    public static class Init extends TempModifierEvent
    {
        public static class Modifier extends TempModifierEvent
        {
            public TempModifierEntries getPool() {
                return TempModifierEntries.getEntries();
            }

            public void addModifier(Class<? extends TempModifier> clazz) throws InstantiationException, IllegalAccessException
            {
                this.getPool().add(clazz.newInstance());
            }
        }

        public static class Block extends TempModifierEvent
        {
            public BlockEffectEntries getPool() {
                return BlockEffectEntries.getEntries();
            }

            public void addBlockEffect(BlockEffect effect) {
                this.getPool().add(effect);
            }
        }
    }
}
