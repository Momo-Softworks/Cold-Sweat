package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import com.momosoftworks.coldsweat.common.tileentity.IceboxTileEntity;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModTileEntities
{
    public static void registerTileEntities()
    {   GameRegistry.registerTileEntity(BoilerTileEntity.class, ColdSweat.getPath("boiler"));
        GameRegistry.registerTileEntity(IceboxTileEntity.class, ColdSweat.getPath("icebox"));
    }
}
