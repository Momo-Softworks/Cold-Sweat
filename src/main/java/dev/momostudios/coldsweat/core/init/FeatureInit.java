package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FeatureInit
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "cold_sweat");

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SOUL_STALK_FEATURE = FEATURES.register("soul_stalk", () -> new SoulStalkFeature(NoneFeatureConfiguration.CODEC));
}
