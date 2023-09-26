package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.common.world.feature.SoulStalkFeatureConfig;
import com.momosoftworks.coldsweat.core.init.FeatureInit;
import com.momosoftworks.coldsweat.data.tags.ModBlockTags;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;

/**
 * Required because configured features need to be lazily called, so they don't reference unloaded registry objects
 */
public class ModFeatures
{
    public static final ConfiguredFeature<?, ?> SOUL_STALK_CONFIG = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "soul_stalk_config",
                                                                                      FeatureInit.SOUL_STALK_FEATURE.get().configured(new SoulStalkFeatureConfig(
                                                                                                      20, 2, 6, 12, 8, 4, 2,
                                                                                                      new SimpleBlockStateProvider(Blocks.SOUL_SAND.defaultBlockState()),
                                                                                                      ModBlockTags.SOUL_SAND_REPLACEABLE)).squared().count(1).chance(16)
                                                                                              .decorated(Features.Placements.HEIGHTMAP_DOUBLE)
                                                                                              .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(10, 10, 256))));
}
