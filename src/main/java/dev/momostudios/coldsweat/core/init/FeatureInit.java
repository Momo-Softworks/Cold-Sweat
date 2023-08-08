package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FeatureInit
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "cold_sweat");

    public static final RegistryObject<Feature<NoFeatureConfig>> SOUL_STALK_FEATURE = FEATURES.register("soul_stalk", () -> new SoulStalkFeature(NoFeatureConfig.CODEC));
}
