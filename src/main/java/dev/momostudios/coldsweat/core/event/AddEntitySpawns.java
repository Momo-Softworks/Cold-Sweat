//package dev.momostudios.coldsweat.core.event;
//
//import dev.momostudios.coldsweat.config.EntitySettingsConfig;
//import dev.momostudios.coldsweat.util.config.ConfigSettings;
//import dev.momostudios.coldsweat.util.registries.ModEntities;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.MobCategory;
//import net.minecraft.world.level.biome.MobSpawnSettings;
//import net.minecraftforge.event.TagsUpdatedEvent;
//import net.minecraftforge.event.world.BiomeLoadingEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Mod.EventBusSubscriber
//public class AddEntitySpawns
//{
//    @SubscribeEvent
//    public static void onBiomeLoading(BiomeLoadingEvent event)
//    {
//        // Add chameleon spawns
//        Integer chameleonWeight = ConfigSettings.CHAMELEON_BIOMES.get().get(event.getName().toString());
//        if (chameleonWeight != null)
//        {
//            event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(ModEntities.CHAMELEON, chameleonWeight, 1, 1));
//        }
//
//        // Increase goat spawns
//        if (EntitySettingsConfig.getInstance().areGoatSpawnsIncreased() && event.getSpawns().getEntityTypes().contains(EntityType.GOAT))
//        {
//            // double the goat's spawn rate
//            List<Integer> goatWeights = new ArrayList<>();
//            event.getSpawns().getSpawner(MobCategory.CREATURE).removeIf(spawnerData ->
//            {
//                if (spawnerData.type == EntityType.GOAT)
//                {
//                    goatWeights.add(spawnerData.getWeight().asInt());
//                    return true;
//                }
//                return false;
//            });
//            goatWeights.forEach(weight1 -> event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, weight1 * 2, 2, 3)));
//        }
//
//        // Add new goat spawns
//        Integer goatWeight = ConfigSettings.GOAT_BIOMES.get().get(event.getName().toString());
//        if (goatWeight != null)
//        {
//            event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, goatWeight, 2, 3));
//        }
//    }
//
//    @SubscribeEvent
//    public static void onBiomeTagsBuild(TagsUpdatedEvent event)
//    {
//        ConfigSettings.GOAT_BIOMES.load();
//        ConfigSettings.CHAMELEON_BIOMES.load();
//    }
//}
