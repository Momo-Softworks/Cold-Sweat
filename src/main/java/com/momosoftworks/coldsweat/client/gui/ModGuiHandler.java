package com.momosoftworks.coldsweat.client.gui;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ModGuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id)
        {   case 0:
            {   BoilerTileEntity boiler = (BoilerTileEntity) world.getTileEntity(x, y, z);
                return new BoilerContainer(player.inventory, boiler);
            }
            default: return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id)
        {   case 0:
            {   BoilerTileEntity boiler = (BoilerTileEntity) world.getTileEntity(x, y, z);
                return new BoilerGui(player.inventory, boiler);
            }
            default: return null;
        }
    }

    public static void register()
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(ColdSweat.INSTANCE, new ModGuiHandler());
    }
}
