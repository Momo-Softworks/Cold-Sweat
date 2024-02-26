package com.momosoftworks.coldsweat.core.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.biome_modifier.AddSpawnsBiomeModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BiomeCodecInit
{
    public static DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ColdSweat.MOD_ID);

    public static RegistryObject<Codec<AddSpawnsBiomeModifier>> ADD_SPAWNS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () ->
    RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.fieldOf("use_configs").forGetter(AddSpawnsBiomeModifier::useConfigs)
      ).apply(builder, AddSpawnsBiomeModifier::new)));
}
