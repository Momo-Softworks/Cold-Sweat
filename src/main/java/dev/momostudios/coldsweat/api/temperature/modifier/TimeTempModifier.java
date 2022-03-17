package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import dev.momostudios.coldsweat.api.temperature.Temperature;

public class TimeTempModifier extends TempModifier
{
    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        if (!player.level.dimensionType().hasFixedTime())
        {
            float timeTemp = 0;
            Level world = player.level;
            for (BlockPos iterator : WorldHelper.getNearbyPositions(player.blockPosition(), 200, 6))
            {
                ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, world.getBiome(iterator).getRegistryName());
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

            return temp.add(timeTemp / 200);
        }
        else return temp;
    }

    public String getID()
    {
        return "cold_sweat:time";
    }
}