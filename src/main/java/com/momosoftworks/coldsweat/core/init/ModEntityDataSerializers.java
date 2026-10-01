package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModEntityDataSerializers
{
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, ColdSweat.MOD_ID);

    // Vanilla's COMPOUND_TAG serializer was removed in 1.21.5.
    // Held directly so that entity data accessors can be defined during class init, before registration
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);

    static
    {   ENTITY_DATA_SERIALIZERS.register("compound_tag", () -> COMPOUND_TAG);
    }
}
