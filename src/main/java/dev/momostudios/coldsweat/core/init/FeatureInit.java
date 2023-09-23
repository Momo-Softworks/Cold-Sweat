package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeature;
import dev.momostudios.coldsweat.common.world.feature.SoulStalkFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

public class FeatureInit
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, ColdSweat.MOD_ID);

    public static final RegistryObject<Feature<SoulStalkFeatureConfig>> SOUL_STALK_FEATURE = FEATURES.register("soul_stalk", () -> new SoulStalkFeature(SoulStalkFeatureConfig.CODEC));
}
