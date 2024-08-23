package com.momosoftworks.coldsweat.core.init;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class MemoryInit
{
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<MemoryModuleType<Integer>> LONG_JUMP_COOLING_DOWN = MEMORIES.register("long_jump_cooling_down", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistryObject<MemoryModuleType<Boolean>> LONG_JUMP_MID_JUMP = MEMORIES.register("long_jump_mid_jump", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
    public static final RegistryObject<MemoryModuleType<Integer>> RAM_COOLDOWN_TICKS = MEMORIES.register("ram_cooldown_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistryObject<MemoryModuleType<Vector3d>> RAM_TARGET = MEMORIES.register("ram_target", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<PlayerEntity>> TEMPTING_PLAYER = MEMORIES.register("tempting_player", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Integer>> TEMPTATION_COOLDOWN_TICKS = MEMORIES.register("temptation_cooldown_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final RegistryObject<MemoryModuleType<Boolean>> IS_TEMPTED = MEMORIES.register("is_tempted", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
}
