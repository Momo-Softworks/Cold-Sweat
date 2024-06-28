package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.world.feature.SoulStalkFeature;
import com.momosoftworks.coldsweat.common.world.feature.SoulStalkFeatureConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ColdSweat.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<SoulStalkFeatureConfig>> SOUL_STALK_FEATURE = FEATURES.register("soul_stalk", () -> new SoulStalkFeature(SoulStalkFeatureConfig.CODEC));
}
