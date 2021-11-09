package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

import java.util.List;

/**
 * This is the basis of all ways that a Temperature can be changed.<br>
 * For example, biome temperature, time of day, depth, and waterskins are all TempModifiers<br>
 *<br>
 * It is up to you to apply and remove these modifiers manually.<br>
 * To make an instant modifier that does not persist on the player, you can call {@code PlayerTemp.removeModifier()} to remove it in {@code calculate()}.<br>
 *<br>
 * TempModifiers must be REGISTERED using {@link net.momostudios.coldsweat.core.event.csevents.TempModifierEvent.Init}<br>
 * (see {@link net.momostudios.coldsweat.core.event.InitTempModifiers} for an example)<br>
 */
public abstract class TempModifier
{
    public TempModifier() {}

    /**
     * Use these if your TempModifier has arguments.
     * @param args types MUST extend {@link INBT}, as they must be able to be stored in the PlayerEntity's NBT data and recalled later.
     */
    public TempModifier(List<INBT> args) {}

    /**
     * Returns a new TempModifier with the specified {@code [args]} stored.<br>
     * This is mainly used for processing.
     */
    public TempModifier with(List<INBT> args) {
        return this;
    }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.
     * This is basically a simple in-out system. It is given a {@link Temperature}, and returns a new Temperature based on the PlayerEntity.
     * @param temp should usually represent the player's body temperature or ambient temperature.
     */
    public abstract float calculate(Temperature temp, PlayerEntity player);

    /**
     * @return the String ID of the TempModifier. You should include your mod's ID to prevent duplicate names.
     * The ID is used to mark the TempModifier when it is stored in NBT
     */
    public abstract String getID();
}
