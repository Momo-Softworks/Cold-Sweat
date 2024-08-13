package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.ThermolithBlockEntity;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import net.minecraft.tileentity.TileEntityType;

public class ModBlockEntities
{
    public static final TileEntityType<HearthBlockEntity> HEARTH = BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get();
    public static final TileEntityType<BoilerBlockEntity> BOILER = BlockEntityInit.BOILER_BLOCK_ENTITY_TYPE.get();
    public static final TileEntityType<IceboxBlockEntity> ICEBOX = BlockEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get();
    public static final TileEntityType<ThermolithBlockEntity> THERMOLITH = BlockEntityInit.THERMOLITH_BLOCK_ENTITY_TYPE.get();
}
