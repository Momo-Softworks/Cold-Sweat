package com.momosoftworks.coldsweat.core.init;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemComponents
{
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.DataComponents.createDataComponents(ColdSweat.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> SOULSPRING_LAMP_FUEL = DATA_COMPONENTS.register("fuel",
                                               () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SOULSPRING_LAMP_LIT = DATA_COMPONENTS.register("lit",
                                               () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> INSULATION_DATA = DATA_COMPONENTS.register("lit",
                                               () -> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> WATER_TEMPERATURE = DATA_COMPONENTS.register("temperature",
                                               () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE).build());
}
