package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.registries.ModFeatures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddFeatureGeneration
{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void injectBiomeLoading(BiomeLoadingEvent event)
    {
        if (event.getName() == null) return;

        if (event.getCategory().equals(Biome.Category.NETHER))
        {
            ConfiguredFeature<?, ?> soulStalkFeature;
            if (event.getName().toString().equals("minecraft:soul_sand_valley"))
            {   soulStalkFeature = ModFeatures.SOUL_STALK_COMMON_CONFIG;
            }
            else soulStalkFeature = ModFeatures.SOUL_STALK_CONFIG;
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> soulStalkFeature);
        }
    }
}
