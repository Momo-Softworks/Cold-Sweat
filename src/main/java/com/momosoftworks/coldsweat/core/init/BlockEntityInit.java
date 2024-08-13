package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.ThermolithBlockEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;

public class BlockEntityInit
{
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<TileEntityType<BoilerBlockEntity>> BOILER_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("boiler", () -> TileEntityType.Builder.of(BoilerBlockEntity::new, BlockInit.BOILER.get()).build(null));
    public static final RegistryObject<TileEntityType<IceboxBlockEntity>> ICEBOX_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("icebox", () -> TileEntityType.Builder.of(IceboxBlockEntity::new, BlockInit.ICEBOX.get()).build(null));
    public static final RegistryObject<TileEntityType<HearthBlockEntity>> HEARTH_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("hearth", () -> TileEntityType.Builder.of(HearthBlockEntity::new, BlockInit.HEARTH_BOTTOM.get()).build(null));
    public static final RegistryObject<TileEntityType<ThermolithBlockEntity>> THERMOLITH_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("thermolith", () -> TileEntityType.Builder.of(ThermolithBlockEntity::new, BlockInit.THERMOLITH.get()).build(null));
}