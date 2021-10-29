package net.momostudios.coldsweat.core.event.csevents;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.util.ListNBTHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;

public class TempModifierEvent extends Event
{
    /**
     * Fired when a {@link TempModifier} is about to be added. <br>
     * <br>
     * {@link #args} is a list of INBT used as arguments for the TempModifier. <br>
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
        public INBT[] args;
        public boolean duplicatesAllowed;
        public final PlayerEntity player;
        public PlayerTemp.Types type;

        public void setArgs(INBT[] args) {
            this.args = args;
        }

        public void setDuplicatesAllowed(boolean allowDuplicates) {
            this.duplicatesAllowed = allowDuplicates;
        }

        public void setModifierType(PlayerTemp.Types newType) {
            this.type = newType;
        }

        public Add(TempModifier modifier, PlayerEntity player, PlayerTemp.Types type, boolean duplicates, INBT... arguments)
        {
            args = arguments;
            duplicatesAllowed = duplicates;
            this.player = player;
            this.type = type;

            if (!this.isCanceled())
            {
                ListNBT nbt = ListNBTHelper.createIfNull(PlayerTemp.getModifierTag(this.type), this.player);
                if (TempModifierEntries.getEntries().getEntryName(modifier) != null)
                {
                    if (!ListNBTHelper.doesNBTContain(nbt, modifier) || this.duplicatesAllowed)
                    {
                        CompoundNBT modifierData = new CompoundNBT();
                        modifierData.putString("modifier_name", TempModifierEntries.getEntries().getEntryName(modifier));

                        if (args != null)
                        {
                            int modifierIndex = 0;
                            for (INBT argument : args)
                            {
                                modifierData.put("argument_" + modifierIndex, argument);
                            }
                        }
                        nbt.add(modifierData);
                    }
                    this.player.getPersistentData().put(PlayerTemp.getModifierTag(this.type), nbt);
                }
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
                ListNBT modifierList = ListNBTHelper.createIfNull(PlayerTemp.getModifierTag(this.type), this.player);
                {
                    if (!modifierList.isEmpty())
                    {
                        this.count = Math.min(this.count, modifierList.size());
                        int removed = 0;
                        for (int i = 0; i < this.count; i++)
                        {
                            INBT inbt = modifierList.get(i);
                            if (TempModifierEntries.getEntries().getEntryFor(((CompoundNBT) inbt).getString("modifier_name")).getClass().equals(this.modifierClass))
                            {
                                modifierList.remove(inbt);
                                removed++;

                                if (removed >= this.count)
                                    break;
                            }
                        }
                    }
                }
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

            public void addModifier(TempModifier modifier) {
                this.getPool().add(modifier);
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
