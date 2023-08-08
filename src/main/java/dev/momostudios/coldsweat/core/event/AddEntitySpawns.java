package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEntities;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddEntitySpawns
{
    @SubscribeEvent
    public static void onBiomeLoading(BiomeLoadingEvent event)
    {
        if (event.getName() != null)
        {
            Integer chameleonWeight = ConfigSettings.CHAMELEON_BIOMES.get().get(event.getName());
            if (chameleonWeight != null)
            {   event.getSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(ModEntities.CHAMELEON, chameleonWeight, 1, 1));
            }

            Integer llama = ConfigSettings.LLAMA_BIOMES.get().get(event.getName());
            if (llama != null)
            {   event.getSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.LLAMA, llama, 1, 1));
            }
        }
    }
}