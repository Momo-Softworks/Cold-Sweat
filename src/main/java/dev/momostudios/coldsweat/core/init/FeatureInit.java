package dev.momostudios.coldsweat.core.init;

import com.google.common.collect.ImmutableList;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeature;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public class FeatureInit
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, ColdSweat.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SOUL_STALK_FEATURE = FEATURES.register("soul_stalk", () -> new SoulStalkFeature(NoneFeatureConfiguration.CODEC));


    public static final class PlacedFeatureInit
    {

        public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, ColdSweat.MOD_ID);

        public static final RegistryObject<PlacedFeature> SOUL_STALK_PLACEMENT = register("soul_stalk_placement", ConfiguredFeatureInit.SOUL_STALK_CONFIGURATION, List.of(RarityFilter.onAverageOnceEvery(5),
                                                                                                                                                                          InSquarePlacement.spread(), PlacementUtils.RANGE_10_10,
                                                                                                                                                                          BiomeFilter.biome(), CountPlacement.of(2)));

        private static RegistryObject<PlacedFeature> register(String name, RegistryObject<? extends ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placementModifiers)
        {   return PLACED_FEATURES.register(name, () -> new PlacedFeature((Holder)feature.getHolder().get(), ImmutableList.copyOf(placementModifiers)));
        }
    }


    public static final class ConfiguredFeatureInit
    {
        private static <FC extends FeatureConfiguration, F extends Feature<FC>> RegistryObject<ConfiguredFeature<FC, ?>> register(String name, Supplier<ConfiguredFeature<FC, F>> feature) {
            return CONFIGURED_FEATURES.register(name, feature);
        }

        public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, ColdSweat.MOD_ID);

        public static final RegistryObject<ConfiguredFeature<RandomPatchConfiguration, ?>> SOUL_STALK_CONFIGURATION = register("soul_stalk_configuration",
                                                                                                                               () -> new ConfiguredFeature<>(Feature.RANDOM_PATCH,
                                                                                                                                        new RandomPatchConfiguration(8, 10, 5,
                                                                                                                                            PlacementUtils.filtered(FeatureInit.SOUL_STALK_FEATURE.get(),
                                                                                                                                            NoneFeatureConfiguration.INSTANCE,
                                                                                                                                            BlockPredicate.not(BlockPredicate.solid())))));

    }
}
