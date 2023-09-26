package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.ThermolithBlockEntity;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities
{
    public static final BlockEntityType<HearthBlockEntity> HEARTH = BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get();
    public static final BlockEntityType<BoilerBlockEntity> BOILER = BlockEntityInit.BOILER_BLOCK_ENTITY_TYPE.get();
    public static final BlockEntityType<IceboxBlockEntity> ICEBOX = BlockEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get();
    public static final BlockEntityType<ThermolithBlockEntity> THERMOLITH = BlockEntityInit.THERMOLITH_BLOCK_ENTITY_TYPE.get();
}
