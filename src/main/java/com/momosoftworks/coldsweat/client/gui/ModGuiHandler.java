package com.momosoftworks.coldsweat.client.gui;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import com.momosoftworks.coldsweat.common.tileentity.IceboxTileEntity;
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
            case 1:
            {   IceboxTileEntity icebox = (IceboxTileEntity) world.getTileEntity(x, y, z);
                return new IceboxContainer(player.inventory, icebox);
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
            case 1:
            {   IceboxTileEntity icebox = (IceboxTileEntity) world.getTileEntity(x, y, z);
                return new IceboxGui(player.inventory, icebox);
            }
            default: return null;
        }
    }

    public static void register()
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(ColdSweat.INSTANCE, new ModGuiHandler());
    }
}
