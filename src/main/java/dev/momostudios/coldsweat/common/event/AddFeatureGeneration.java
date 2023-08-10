package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.registries.ModFeatures;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddFeatureGeneration
{
    @SubscribeEvent
    public static void injectBiomeLoading(BiomeLoadingEvent event)
    {
        if (event.getName() == null) return;

        if (event.getCategory().equals(Biome.Category.NETHER))
        {   event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> ModFeatures.SOUL_STALK_CONFIG);
        }
    }
}
