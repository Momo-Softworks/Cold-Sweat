package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.WorldInfo;

public class TimeTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double timeTemp = 0;
        World world = player.world;
        for (BlockPos iterator : WorldInfo.getNearbyPositions(player.getPosition(), 200, 6))
        {
            RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, world.func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(world.getBiome(iterator)));
            if (BiomeDictionary.hasType(key, BiomeDictionary.Type.HOT) &&
                BiomeDictionary.hasType(key, BiomeDictionary.Type.SANDY))
            {
                timeTemp += (Math.cos((world.getDayTime() / 3819.7186342) - 1.5707963268) / 1d) - 1;
            }
            else
            {
                timeTemp += (Math.cos((world.getDayTime() / 3819.7186342) - 1.5707963268) / 4d) - 0.1;
            }
        }

        return temp.get() + (timeTemp / 200);
    }
}