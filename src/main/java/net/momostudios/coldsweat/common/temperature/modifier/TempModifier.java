package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the basis of all ways that a Temperature can be changed.
 * For example, biome temperature, time of day, depth, and waterskins are all TempModifiers
 *
 * It is up to you to apply and remove these modifiers manually.
 * To make an instant modifier that does not persist on the player, you can call {@code PlayerTemp.removeModifier()} to remove it in {@code calculate()}.
 *
 * TempModifiers must be REGISTERED to {@link net.momostudios.coldsweat.common.world.TempModifierEntries} to be used
 * (see {@link net.momostudios.coldsweat.core.event.InitTempModifiers} for an example)
 */
public class TempModifier extends ForgeRegistryEntry<TempModifier>
{
    /**
     * This default constructor is important!
     */
    public TempModifier() {}

    /**
     * Use these if your TempModifier has arguments.
     * @param args types MUST extend {@link INBT}, as they must be able to be stored in the PlayerEntity's NBT data and recalled later.
     */
    public TempModifier(List<INBT> args) {}
    public TempModifier with(List<INBT> args)
    {
        return this;
    }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.
     * This is basically a simple in-out system. It is given a {@link Temperature}, and returns a new Temperature based on the PlayerEntity.
     * @param temp should usually represent the player's body temperature or ambient temperature.
     */
    public double calculate(Temperature temp, PlayerEntity player)
    {
        return temp.get();
    }

    /**
     * @return the String ID of the TempModifier. You should include your mod's ID to prevent duplicate names.
     * The ID is used to mark the TempModifier when it is stored in NBT
     */
    public String getID()
    {
        return "cold_sweat:temp_modifier";
    }
}
