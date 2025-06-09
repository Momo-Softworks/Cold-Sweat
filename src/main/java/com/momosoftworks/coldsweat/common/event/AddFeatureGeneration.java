package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.registries.ModFeatures;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddFeatureGeneration
{
    @SubscribeEvent
    public static void generateSoulStalk(BiomeLoadingEvent event)
    {
        if (event.getName() == null) return;

        if (event.getCategory().equals(Biome.BiomeCategory.NETHER))
        {
            Holder<PlacedFeature> soulStalkFeature;
            if (event.getName().toString().equals("minecraft:soul_sand_valley"))
            {   soulStalkFeature = ModFeatures.SOUL_STALK_COMMON_PLACEMENT;
            }
            else soulStalkFeature = ModFeatures.SOUL_STALK_PLACEMENT;
            event.getGeneration().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION).add(soulStalkFeature);
        }
    }
}
