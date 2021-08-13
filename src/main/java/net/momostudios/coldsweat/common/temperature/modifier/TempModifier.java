package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This is the basis of all ways that a Temperature can be changed.
 * For example, biome temperature, time of day, depth, and waterskins are all {@link net.momostudios.coldsweat.common.temperature.modifier.TempModifier}
 * This is basically a simple in-out system. It is given a Temperature, and returns a new Temperature based on the PlayerEntity
 *
 * It is up to you to apply and remove these modifiers manually.
 * To make an instant modifier that does not stay on the player, you can call {@code PlayerTemp.removeModifier()} to remove it in {@code calculate()}
 */
public class TempModifier extends ForgeRegistryEntry<TempModifier> {
    /**
     * Use this if your TempModifier has arguments
     */
    public TempModifier with(List<Object> args) { return null; }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to
     * @param temp should usually represent the player's body temperature or ambient temperature
     */
    public double calculate(Temperature temp, PlayerEntity player)
    {
        return temp.get();
    }
}
