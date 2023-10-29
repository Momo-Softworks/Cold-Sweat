package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.BoilerBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class ModBlocks
{
    public static Block BOILER;
    public static Block BOILER_LIT;

    public static void registerBlocks()
    {
        BOILER = namedBlock(new BoilerBlock(false), "boiler").setCreativeTab(ColdSweat.TAB_COLD_SWEAT).setHardness(2.0F).setResistance(5.0F);
        BOILER_LIT = namedBlock(new BoilerBlock(true), "boiler_lit").setHardness(2.0F).setResistance(5.0F);

        GameRegistry.registerBlock(BOILER, BOILER.getUnlocalizedName());
        GameRegistry.registerBlock(BOILER_LIT, BOILER_LIT.getUnlocalizedName());
    }

    private static Block namedBlock (Block block, String name)
    {   return block.setBlockName(name).setBlockTextureName(ColdSweat.getPath(name));
    }
}
