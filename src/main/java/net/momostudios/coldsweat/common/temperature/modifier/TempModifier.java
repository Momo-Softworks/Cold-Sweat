package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, Object> args = new HashMap<>();

    /**
     * Default constructor.
     */
    public TempModifier() {}

    /**
     * Adds a new argument to this TempModifier instance.<br>
     * @param name is the name of the argument. Used to retrieve the argument in {@link #getArgument(String)}
     * @param arg is value of the argument. It is stored in the {@link PlayerEntity} NBT.
     */
    public void addArgument(String name, Object arg) {
        args.put(name, arg);
    }

    /**
     * @param name the name of the argument
     * @return the value of the argument with the specified name.
     */
    public Object getArgument(String name) {
        return args.get(name);
    }

    /**
     * Sets the argument of the TempModifier instance to the specified value.<br>
     * @param name the name of the argument
     * @param arg the value of the argument
     */
    public void setArgument(String name, Object arg) {
        args.put(name, arg);
    }

    public final Map<String, Object> getArguments() {
        return args;
    }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.<br>
     * This is basically a simple in-out system. It is given a {@link Temperature}, and returns a new Temperature based on the PlayerEntity.
     * @param temp should usually represent the player's body temperature or ambient temperature.
     * @param player the player that is being affected by the modifier
     */
    public abstract double calculate(Temperature temp, PlayerEntity player);

    /**
     * @return the String ID of the TempModifier. You should include your mod's ID to prevent duplicate names.
     * The ID is used to mark the TempModifier when it is stored in NBT
     */
    public abstract String getID();
}
