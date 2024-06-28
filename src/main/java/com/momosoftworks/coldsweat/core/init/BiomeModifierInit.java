package com.momosoftworks.coldsweat.core.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.biome_modifier.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BiomeModifierInit
{
    public static DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ColdSweat.MOD_ID);

    public static DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddSpawnsBiomeModifier>> ADD_SPAWNS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () ->
    RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.BOOL.fieldOf("use_configs").forGetter(AddSpawnsBiomeModifier::useConfigs)
      ).apply(builder, AddSpawnsBiomeModifier::new)));
}
