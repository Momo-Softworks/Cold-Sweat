package dev.momostudios.coldsweat.util.registries;

import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeatureConfig;
import dev.momostudios.coldsweat.core.init.FeatureInit;
import dev.momostudios.coldsweat.data.tags.ModBlockTags;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;

/**
 * Required because configured features need to be lazily called, so they don't reference unloaded registry objects
 */
public class ModFeatures
{
    public static final Holder<ConfiguredFeature<SoulStalkFeatureConfig, ?>> SOUL_STALK_CONFIG = FeatureUtils.register("soul_stalk", FeatureInit.SOUL_STALK_FEATURE.get(), new SoulStalkFeatureConfig(
            20, 2, 6, 12, 8, 4, 2, BlockStateProvider.simple(Blocks.SOUL_SAND), BlockPredicate.matchesTag(ModBlockTags.SOUL_SAND_REPLACEABLE)));

    public static final Holder<PlacedFeature> SOUL_STALK_PLACEMENT = PlacementUtils.register("soul_stalk_placement", SOUL_STALK_CONFIG,
                                                                                             RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome(), CountPlacement.of(1));
}
