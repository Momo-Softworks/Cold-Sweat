package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.config.WorldSettingsConfig;
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
        if (WorldSettingsConfig.getInstance().chameleonBiomes().contains(event.getName().toString()))
        {
            event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CHAMELEON, 8, 1, 1));
        }
    }
}
