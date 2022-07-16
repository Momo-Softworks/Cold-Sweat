package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.LegacyMethodHelper;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.common.BiomeDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TimeTempModifier extends TempModifier
{
    static Map<Biome, ResourceKey<Biome>> BIOME_KEYS = new HashMap<>();

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        if (!player.level.dimensionType().hasFixedTime())
        {
            float timeTemp = 0;
            Level world = player.level;
            for (BlockPos blockPos : WorldHelper.getNearbyPositions(player.blockPosition(), 200, 6))
            {
                BiomeManager biomeManager = player.level.getBiomeManager();
                Biome biome = LegacyMethodHelper.getBiome(biomeManager, blockPos);

                ResourceKey<Biome> key = BIOME_KEYS.get(biome);

                if (key == null)
                    key = ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName());

                if (BiomeDictionary.hasType(key, BiomeDictionary.Type.HOT)
                && BiomeDictionary.hasType(key, BiomeDictionary.Type.SANDY))
                {
                    timeTemp += Math.sin(world.getDayTime() / 3819.7186342) / 1.5 - 0.7;
                }
                else
                {
                    timeTemp += Math.sin(world.getDayTime() / 3819.7186342) / 4d - 0.125;
                }
            }

            float finalTimeTemp = timeTemp;
            return temp -> temp.add(finalTimeTemp / 200);
        }
        else return temp -> temp;
    }

    public String getID()
    {
        return "cold_sweat:time";
    }
}