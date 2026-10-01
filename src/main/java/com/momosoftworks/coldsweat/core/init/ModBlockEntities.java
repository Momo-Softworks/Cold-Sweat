package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.common.blockentity.ThermolithBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ColdSweat.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerBlockEntity>> BOILER =
            BLOCK_ENTITY_TYPES.register("boiler", () -> new BlockEntityType<>(BoilerBlockEntity::new, ModBlocks.BOILER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IceboxBlockEntity>> ICEBOX =
            BLOCK_ENTITY_TYPES.register("icebox", () -> new BlockEntityType<>(IceboxBlockEntity::new, ModBlocks.ICEBOX.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HearthBlockEntity>> HEARTH =
            BLOCK_ENTITY_TYPES.register("hearth", () -> new BlockEntityType<>(HearthBlockEntity::new, ModBlocks.HEARTH_BOTTOM.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermolithBlockEntity>> THERMOLITH =
            BLOCK_ENTITY_TYPES.register("thermolith", () -> new BlockEntityType<>(ThermolithBlockEntity::new, ModBlocks.THERMOLITH.get()));
}