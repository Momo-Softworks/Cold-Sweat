package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.registries.ModFeatures;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
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
        {   event.getGeneration().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION).add(ModFeatures.SOUL_STALK_PLACEMENT);
        }
    }
}
