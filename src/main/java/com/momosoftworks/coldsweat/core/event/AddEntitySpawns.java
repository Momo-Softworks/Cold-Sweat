package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.common.entity.GoatEntity;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

@Mod.EventBusSubscriber
public class AddEntitySpawns
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoading(BiomeLoadingEvent event)
    {
        DynamicRegistries registryAccess = RegistryHelper.getDynamicRegistries();
        if (registryAccess == null) return;

        CSMath.doIfNotNull(ConfigSettings.ENTITY_SPAWN_BIOMES.get(registryAccess).get(RegistryHelper.getBiome(event.getName(), registryAccess)), spawns ->
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

    @SubscribeEvent
    public static void registerSpawnPlacements(FMLServerAboutToStartEvent event)
    {
        EntitySpawnPlacementRegistry.register(ModEntities.GOAT, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, GoatEntity::canSpawn);
    }
}