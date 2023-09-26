package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import com.momosoftworks.coldsweat.common.tileentity.HearthTileEntity;
import com.momosoftworks.coldsweat.common.tileentity.IceboxTileEntity;
import com.momosoftworks.coldsweat.common.tileentity.ThermolithTileEntity;
import com.momosoftworks.coldsweat.core.init.TileEntityInit;
import net.minecraft.tileentity.TileEntityType;

public class ModBlockEntities
{
    public static final TileEntityType<HearthTileEntity> HEARTH = TileEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get();
    public static final TileEntityType<BoilerTileEntity> BOILER = TileEntityInit.BOILER_BLOCK_ENTITY_TYPE.get();
    public static final TileEntityType<IceboxTileEntity> ICEBOX = TileEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get();
    public static final TileEntityType<ThermolithTileEntity> THERMOLITH = TileEntityInit.THERMOLITH_BLOCK_ENTITY_TYPE.get();
}
