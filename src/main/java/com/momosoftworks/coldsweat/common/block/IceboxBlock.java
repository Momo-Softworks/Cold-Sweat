package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.tileentity.IceboxTileEntity;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class IceboxBlock extends BlockContainer
{
    private IIcon[] sides;
    private final Random random = new Random();
    private final boolean frosted;
    static boolean UPDATING = false;

    public IceboxBlock(boolean frosted)
    {   super(Material.wood);
        this.frosted = frosted;
    }

    public boolean isFrosted()
    {   return frosted;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {   sides = new IIcon[5];
        sides[0] = iconRegister.registerIcon(ColdSweat.getPath("icebox_top"));
        sides[1] = iconRegister.registerIcon(ColdSweat.getPath("icebox_bottom"));
        sides[2] = iconRegister.registerIcon(ColdSweat.getPath("icebox_side"));
        sides[3] = iconRegister.registerIcon(ColdSweat.getPath("icebox_side_frosted"));
        sides[4] = iconRegister.registerIcon(ColdSweat.getPath("icebox_side"));
        this.blockIcon = sides[2];
    }

    @Override
    public IIcon getIcon(int sideInt, int meta)
    {
        ForgeDirection side = ForgeDirection.getOrientation(sideInt);
        switch (side)
        {
            case UP   :  return sides[0];
            case DOWN :  return sides[1];
            default   :  return frosted ? sides[3] : sides[2];
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {   player.openGui(ColdSweat.INSTANCE, 1, world, x, y, z);
        return true;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune)
    {   return Item.getItemFromBlock(ModBlocks.ICEBOX);
    }

    @Override
    public Item getItem(World world, int x, int y, int z)
    {   return Item.getItemFromBlock(ModBlocks.ICEBOX);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
    {
        int facing = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        switch (facing)
        {
            case 0 : world.setBlockMetadataWithNotify(x, y, z, 2, 2); break;
            case 1 : world.setBlockMetadataWithNotify(x, y, z, 5, 2); break;
            case 2 : world.setBlockMetadataWithNotify(x, y, z, 3, 2); break;
            case 3 : world.setBlockMetadataWithNotify(x, y, z, 4, 2); break;
        }

        if (stack.hasDisplayName())
        {   ((IceboxTileEntity) world.getTileEntity(x, y, z)).setName(stack.getDisplayName());
        }
    }

    public static void updateBlockState(boolean frosted, World world, int x, int y, int z)
    {   int dirInt = world.getBlockMetadata(x, y, z);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        UPDATING = true;
        if (frosted)
        {   world.setBlock(x, y, z, ModBlocks.ICEBOX_FROSTED);
        }
        else
        {   world.setBlock(x, y, z, ModBlocks.ICEBOX);
        }
        UPDATING = false;
        world.setBlockMetadataWithNotify(x, y, z, dirInt, 2);
        if (tileEntity != null)
        {   tileEntity.validate();
            world.setTileEntity(x, y, z, tileEntity);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        if (world.isRemote || UPDATING) return;
        IceboxTileEntity tileEntity = (IceboxTileEntity) world.getTileEntity(x, y, z);
        if (tileEntity != null)
        {   for (int i = 0; i < tileEntity.getSizeInventory(); i++)
            {   ItemStack stack = tileEntity.getStackInSlot(i);
                if (stack != null)
                {   float spawnX = random.nextFloat() * 0.8F + 0.1F;
                    float spawnY = random.nextFloat() * 0.8F + 0.1F;
                    float spawnZ = random.nextFloat() * 0.8F + 0.1F;
                    while (stack.stackSize > 0)
                    {   int spawnAmount = random.nextInt(21) + 10;
                        if (spawnAmount > stack.stackSize)
                        {   spawnAmount = stack.stackSize;
                        }
                        stack.stackSize -= spawnAmount;
                        EntityItem item = new EntityItem(world, x + spawnX, y + spawnY, z + spawnZ, new ItemStack(stack.getItem(), spawnAmount, stack.getItemDamage()));
                        if (stack.hasTagCompound())
                        {   item.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                        }
                        float velocity = 0.05F;
                        item.motionX = (float) random.nextGaussian() * velocity;
                        item.motionY = (float) random.nextGaussian() * velocity + 0.2F;
                        item.motionZ = (float) random.nextGaussian() * velocity;
                        world.spawnEntityInWorld(item);
                    }
                }
            }
            world.func_147453_f(x, y, z, block);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {   return new IceboxTileEntity();
    }
}
