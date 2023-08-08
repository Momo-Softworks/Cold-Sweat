package dev.momostudios.coldsweat.util.registries;

import dev.momostudios.coldsweat.core.init.FeatureInit;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

public class ModFeatures
{
    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> SOUL_STALK = FeatureUtils.register("soul_stalk", Feature.RANDOM_PATCH,
            new RandomPatchConfiguration(8, 10, 5, PlacementUtils.onlyWhenEmpty(FeatureInit.SOUL_STALK_FEATURE.get(), NoneFeatureConfiguration.INSTANCE)));

    public static final Holder<PlacedFeature> SOUL_STALK_PLACEMENT = PlacementUtils.register("soul_stalk_placement", SOUL_STALK,
            RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.RANGE_10_10, BiomeFilter.biome(), CountPlacement.of(2));
}
