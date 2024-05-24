package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.EntityClassification;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class AddEntitySpawns
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoading(BiomeLoadingEvent event)
    {
        CSMath.doIfNotNull(ConfigSettings.ENTITY_SPAWN_BIOMES.get().get(ForgeRegistries.BIOMES.getValue(event.getName())), spawns ->
        {
            for (SpawnBiomeData spawnBiomeData : spawns)
            {
                RegistryHelper.mapTaggableList(spawnBiomeData.entities)
                .forEach(entityType ->
                {
                    event.getSpawns().getSpawner(EntityClassification.CREATURE).removeIf(spawnerData -> spawnerData.type == entityType);
                    event.getSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(entityType, spawnBiomeData.weight, 1, 3));
                });
            }
        });
    }
}