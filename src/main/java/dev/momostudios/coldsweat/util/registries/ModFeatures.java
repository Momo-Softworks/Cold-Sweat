package dev.momostudios.coldsweat.util.registries;

import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeature;
import dev.momostudios.coldsweat.common.world.feature.placement.SoulStalkPlacement;
import dev.momostudios.coldsweat.core.init.FeatureInit;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

public class ModFeatures
{
    public static final Feature<NoFeatureConfig> SOUL_STALK = FeatureInit.SOUL_STALK_FEATURE.get();
    public static final SoulStalkPlacement SOUL_STALK_PLACEMENT = ((SoulStalkPlacement) FeatureInit.SOUL_STALK_PLACEMENT.get());

    public static final ConfiguredFeature<?, ?> SOUL_STALK_CONFIG = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "soul_stalk", SOUL_STALK
                                                                               .configured(NoFeatureConfig.INSTANCE)
                                                                               .chance(5)
                                                                               .decorated(SOUL_STALK_PLACEMENT
                                                                                          .spread(6, 5, 16)
                                                                                          .configured(NoPlacementConfig.INSTANCE)
                                                                                          .decorated(Features.Placements.HEIGHTMAP_DOUBLE)));
}
