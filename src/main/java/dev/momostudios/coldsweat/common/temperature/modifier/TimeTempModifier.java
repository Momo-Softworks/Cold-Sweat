package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.WorldHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import dev.momostudios.coldsweat.common.temperature.Temperature;

public class TimeTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        if (!player.world.getDimensionType().doesFixedTimeExist())
        {
            float timeTemp = 0;
            World world = player.world;
            for (BlockPos iterator : WorldHelper.getNearbyPositions(player.getPosition(), 200, 6))
            {
                RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, world.func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(world.getBiome(iterator)));
                if (BiomeDictionary.hasType(key, BiomeDictionary.Type.HOT) &&
                        BiomeDictionary.hasType(key, BiomeDictionary.Type.SANDY))
                {
                    timeTemp += Math.sin(world.getDayTime() / 3819.7186342) - 0.5;
                }
                else
                {
                    timeTemp += (Math.sin(world.getDayTime() / 3819.7186342) / 4d) - 0.125;
                }
            }

            return temp.get() + (timeTemp / 200);
        }
        else return temp.get();
    }

    public String getID()
    {
        return "cold_sweat:time";
    }
}