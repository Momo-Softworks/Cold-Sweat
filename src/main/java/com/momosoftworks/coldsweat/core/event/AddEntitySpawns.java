package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
                RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITIES, spawnBiomeData.entities())
                .forEach(entityType ->
                {
                    event.getSpawns().getSpawner(MobCategory.CREATURE).removeIf(spawnerData -> spawnerData.type == entityType);
                    event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(entityType, spawnBiomeData.weight(), 1, 3));
                });
            }
        });
    }
}