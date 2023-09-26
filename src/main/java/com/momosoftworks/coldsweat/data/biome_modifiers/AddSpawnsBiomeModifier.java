package com.momosoftworks.coldsweat.data.biome_modifiers;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.core.init.BiomeCodecInit;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;


public record AddSpawnsBiomeModifier(boolean useConfigs) implements BiomeModifier
{
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder)
    {
        if (phase == Phase.ADD && useConfigs)
        {
            biome.unwrapKey().ifPresent(biomeKey ->
            {
                ResourceLocation biomeID = biomeKey.location();
                // Add chameleon spawns
                Integer chameleonWeight = ConfigSettings.CHAMELEON_BIOMES.get().get(biomeID);
                if (chameleonWeight != null)
                {   builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CHAMELEON, chameleonWeight, 1, 1));
                }
                // Add goat spawns
                Integer goatWeight = ConfigSettings.GOAT_BIOMES.get().get(biomeID);
                if (goatWeight != null)
                {   builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE).removeIf(spawnerData -> spawnerData.type == EntityType.GOAT);
                    builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, goatWeight, 1, 3));
                }
            });
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec()
    {   return BiomeCodecInit.ADD_SPAWNS_CODEC.get();
    }
}
