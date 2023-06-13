package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddEntitySpawns
{
    @SubscribeEvent
    public static void onBiomeLoading(BiomeLoadingEvent event)
    {
        // Add chameleon spawns
        Integer chameleonWeight = ConfigSettings.CHAMELEON_BIOMES.get().get(event.getName().toString());
        if (chameleonWeight != null)
        {   event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CHAMELEON, chameleonWeight, 1, 1));
        }

        // Add goat spawns
        Integer goatWeight = ConfigSettings.GOAT_BIOMES.get().get(event.getName().toString());
        if (goatWeight != null)
        {   // Remove vanilla goat spawn settings
            event.getSpawns().getSpawner(MobCategory.CREATURE).removeIf(spawnerData -> spawnerData.type == EntityType.GOAT);
            // Add new goat spawn settings
            event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, goatWeight, 2, 3));
        }
    }
}
