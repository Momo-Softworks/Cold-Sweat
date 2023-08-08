package dev.momostudios.coldsweat.util.registries;

import dev.momostudios.coldsweat.core.init.FeatureInit;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.*;

public class ModFeatures
{
    public static final ConfiguredFeature<?, ?> SOUL_STALK = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                                                                                               "soul_stalk", FeatureInit.SOUL_STALK_FEATURE.get()
                                                                                               .configured(NoFeatureConfig.INSTANCE).decorated(Features.Placements.RANGE_10_20_ROOFED));

    //public static final Holder<PlacedFeature> SOUL_STALK_PLACEMENT = PlacementUtils.register("soul_stalk_placement", SOUL_STALK,
    //        RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome(), CountPlacement.of(2));
}
