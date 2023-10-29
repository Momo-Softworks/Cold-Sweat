package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.tileentity.BoilerTileEntity;
import com.momosoftworks.coldsweat.util.math.Direction;
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

public class BoilerBlock extends BlockContainer
{
    private IIcon[] sides;
    private final Random random = new Random();
    private final boolean burning;
    static boolean UPDATING = false;

    public BoilerBlock(boolean burning)
    {   super(Material.rock);
        this.burning = burning;
    }

    public boolean isBurning()
    {   return burning;
    }

    @Override
    public int getLightValue()
    {   return burning ? 13 : 0;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {   sides = new IIcon[5];
        sides[0] = iconRegister.registerIcon(ColdSweat.getPath("boiler_top"));
        sides[1] = iconRegister.registerIcon(ColdSweat.getPath("boiler_bottom"));
        sides[2] = iconRegister.registerIcon(ColdSweat.getPath("boiler_front"));
        sides[3] = iconRegister.registerIcon(ColdSweat.getPath("boiler_front_lit"));
        sides[4] = iconRegister.registerIcon(ColdSweat.getPath("boiler_side"));
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
            default   :  return meta == sideInt ? burning ? sides[3] : sides[2] : sides[4];
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {   player.openGui(ColdSweat.INSTANCE, 0, world, x, y, z);
        return true;
    }

    @Override
    public Item getItemDropped(int par1, Random random, int par3)
    {   return Item.getItemFromBlock(ModBlocks.BOILER);
    }

    @Override
    public Item getItem(World world, int par2, int par3, int par4)
    {   return Item.getItemFromBlock(ModBlocks.BOILER);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {   super.onBlockAdded(world, x, y, z);
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
        {   ((BoilerTileEntity) world.getTileEntity(x, y, z)).setName(stack.getDisplayName());
        }
    }

    public static void updateBlockState(boolean burning, World world, int x, int y, int z)
    {   int dirInt = world.getBlockMetadata(x, y, z);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        UPDATING = true;
        if (burning)
        {   world.setBlock(x, y, z, ModBlocks.BOILER_LIT);
        }
        else
        {   world.setBlock(x, y, z, ModBlocks.BOILER);
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
        BoilerTileEntity tileEntity = (BoilerTileEntity) world.getTileEntity(x, y, z);
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
            // Update neighbors
            world.func_147453_f(x, y, z, block);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
        if (this.burning)
        {
            int dirInt = world.getBlockMetadata(x, y, z);
            Direction direction = Direction.from3DDataValue(dirInt);
            double d0 = x + 0.5D;
            double d1 = y;
            double d2 = z + 0.5D;
            Direction.Axis direction$axis = direction.getAxis();

            double d4 = rand.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = rand.nextDouble() * 6.0D / 16.0D + 0.2;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            world.spawnParticle("flame", d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            world.spawnParticle("flame", d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_)
    {   return new BoilerTileEntity();
    }
}
