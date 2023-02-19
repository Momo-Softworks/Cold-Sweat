package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEntities;
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
        Integer weight = ConfigSettings.CHAMELEON_BIOMES.get().get(event.getName().toString());
        if (weight != null)
        {
            event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CHAMELEON, weight, 1, 1));
        }
    }
}
